package com.zantech.streams

import java.nio.file.Paths

import State.State
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, SourceShape}
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Keep, Merge, Sink, Source, Zip}
import akka.util.ByteString
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.generic.auto._
import io.circe.syntax._
import cats.syntax.either._
import io.circe.{Decoder, Encoder, Printer}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object Main extends App {

  implicit val sys:ActorSystem = ActorSystem("test")
  implicit val mat:ActorMaterializer = ActorMaterializer()
  import sys.dispatcher

  implicit val encodeState: Encoder[State] = Encoder.encodeString.contramap[State](_.toString)
  implicit val decodeState: Decoder[State] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(State.withName(str)).leftMap(_ => "State")
  }

  type GraphMap = Map[String, Component]

  val printer = Printer.spaces2.copy(colonLeft = "", dropNullValues = true)
  val PROPAGATED = "propagated"

  Try( (args(0), args(1)) ) match {
    case Success((graphJSON, eventsJSON)) =>

      val finalGraph = Source.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val eventSource = FileIO.fromPath(Paths.get(eventsJSON))
        val graphSource = FileIO.fromPath(Paths.get(graphJSON))

        val computeBroadCast = builder.add(Broadcast[Graph](2))
        val graphMerge = builder.add(Merge[Graph](2))
        val zip = builder.add(Zip[GraphMap, Event]())

        val delimiterFlow = Framing.delimiter(ByteString("\n"), 8192, allowTruncation = true)
        def decodeAs[A](implicit decoder: Decoder[A]): Flow[ByteString, A, NotUsed] = CirceStreamSupport.decode[A]

        val eventsSortFlow = Flow[Events].mapConcat(_.events.sortBy(_.timestamp))
        val graphMapFlow = Flow[Graph].map(_.graph.components.map(component => component.id -> component).toMap)

        val computeFlow = Flow[(GraphMap, Event)].map{
          case ((graph, event)) => Graph(ComponentGraph(applyEvents(graph, Vector(event))))
        }

        graphSource ~> delimiterFlow ~> decodeAs[Graph] ~> graphMerge

        eventSource ~> delimiterFlow ~> decodeAs[Events] ~> eventsSortFlow ~> zip.in1

        graphMerge ~> graphMapFlow ~> zip.in0

        graphMerge <~ computeBroadCast <~ computeFlow <~ zip.out

        SourceShape(computeBroadCast.out(1))
      }).toMat(Sink.last)(Keep.right).run()

      finalGraph.foreach { graph =>
        sys.terminate()
        print(graph.asJson.pretty(printer))
      }

    case Failure(ex) => println(ex.getMessage)
      sys.terminate()
  }

  def applyEvents(graph: GraphMap, events: Vector[Event]): Vector[Component] = {

    @tailrec
    def applyEventsRec(events: Vector[Event], acc: GraphMap): GraphMap = events match {
      case Vector() => acc
      case event +: tail =>
        val (allEvents, accGraph) = acc.get(event.component).fold((tail, acc)) { component =>
          val depDerivedState = component.`depends_on`.getOrElse(Vector())
            .flatMap(dependency => acc.get(dependency))
            .foldLeft(State.NoData){ (accState, dependency) =>
              if (accState < dependency.`derived_state`) dependency.`derived_state`  else accState
            }

          val (addedEvents, newComponent) = calculateState(component, event, depDerivedState)
          (addedEvents ++: tail, acc + (component.id -> newComponent))
        }

        applyEventsRec(allEvents, accGraph)
    }

    applyEventsRec(events, graph).values.toVector
  }

  private def calculateState(component: Component, event: Event, highestDepDerivedState: State): (Vector[Event], Component) = {
    val newState = determineStateUpdate(component, event, highestDepDerivedState)

    (newState.dependentEvents, component.copy(`check_states` = newState.checkStates, `own_state` = newState.ownState,
      `derived_state` = newState.derivedState))
  }

  private def determineStateUpdate(component: Component, event: Event, highestDepDerivedState: State): StateUpdate = {
    event.`check_state` match {
      case PROPAGATED =>

        val (derivedState, dependentEvents) = if(component.`own_state` < event.state && event.state > State.Clear) {
          (event.state,
            component.`dependency_of`
              .getOrElse(Vector())
              .map( componentId => Event(event.timestamp, componentId, PROPAGATED, event.state))
          )
        } else (State.NoData, Vector.empty[Event])

        StateUpdate(
          checkStates = component.`check_states`,
          ownState = component.`own_state`,
          derivedState = derivedState,
          dependentEvents = dependentEvents
        )

      case checkState @ _ =>
        val checkStates = component.`check_states` + (checkState -> event.state)
        val ownState = checkStates.values.fold(State.NoData)((acc, state) => if(acc > state) acc else state)
        val (derivedState, dependentEvents) = if(ownState >= State.Warning && component.`derived_state` <= State.Clear) {
          (ownState, component.`dependency_of`
            .getOrElse(Vector())
            .map(componentId => Event(event.timestamp, componentId, PROPAGATED, ownState)))
        } else if (highestDepDerivedState >= State.Clear) {
          (highestDepDerivedState, Vector.empty[Event])
        } else if (ownState <= State.Clear && component.`derived_state` >= State.Warning) {
          (State.NoData,
            component.`dependency_of`
              .getOrElse(Vector())
              .map( componentId => Event(event.timestamp, componentId, PROPAGATED, ownState))
          )
        } else (State.NoData, Vector.empty[Event])

        StateUpdate(
          checkStates = checkStates,
          ownState = ownState,
          derivedState = derivedState,
          dependentEvents = dependentEvents
        )

    }
  }

  /*
  1. The check states. Each components has zero, one or multiple of these. (In StackState
  they represent states that are calculated by running checks on monitoring data, which is
  outside the scope of this assessment so you can just change them yourself.) These check
  states can be expected to change all the time. The check states influence the own state of
  the component.
  2. The own state, singular, is the highest state of all check states. It is not affected by
  anything other then the check states. If the component has no check states the own state
  is no_data . (In StackState the own state represents the state that the component itself
  reports.) The own state of a component influences the derived state of the component

  3. The derived state, singular, is either A) the own state from warning and up or if higher B)
  the highest of the derived states of the components it depends on. Clear state does not
  propagate, so if the own state of a component is clear or no_data and its dependent

  derived states are clear or no_data then the derived state is set to no_data.

  Take a dependency graph with two nodes. A component named "db" and a component
  named "app". The app is dependent on the db. The db has two check states. One of the
  check states named "CPU load" of the db goes to the warning state. The own state of the db
  becomes warning. The derived state of the db and app become warning.
   */
}

case class Events(events: Vector[Event])
case class Event(timestamp: String, component: String, `check_state`: String, state: State)

case class Graph(graph: ComponentGraph)
case class ComponentGraph(components: Vector[Component])
case class Component(id: String, `own_state`: State, `derived_state`: State, `check_states`: Map[String, State],
                     `dependency_of`: Option[Vector[String]], `depends_on`: Option[Vector[String]])

case class StateUpdate(checkStates: Map[String, State], ownState: State, derivedState: State, dependentEvents: Vector[Event])

object State extends Enumeration {
  type State = Value
  val NoData: State = Value(1, "no_data")
  val Clear: State = Value(2, "clear")
  val Warning: State = Value(3, "warning")
  val Alert: State = Value(4, "alert")
}