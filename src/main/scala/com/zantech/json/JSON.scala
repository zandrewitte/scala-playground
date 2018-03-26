package json

object JSON {

  sealed abstract class JsValue
  sealed abstract class JsRootValue extends JsValue {
    type T
    def ++(t: T): JsRootValue
  }
  case object JNil extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: T): JsRootValue = JNil
  }
  case class JString(string: String) extends JsValue
  case class JInt(number: Int) extends JsValue
  case class JDouble(number: Double) extends JsValue
  case class JObject(values: JPair*) extends JsRootValue {
    override type T = JPair
    override def ++(jPair: JPair): JObject = {
      val allValues = values :+ jPair
      JObject(allValues:_*)
    }

    def ::(jObject: JObject): JObject = {
      val allValues =  jObject.values ++: values
      JObject(allValues:_*)
    }
  }
  case class JList(values: JsValue*) extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: JsValue): JList = {
      val allValues = values :+ jsValue
      JList(allValues:_*)
    }
  }

  type JPair = (String, JsValue)

}


//  val events = JSONReader.read(Source.fromFile(eventsJSON).mkString) \ "events" match {
//    case jList: JList => jList.values.toVector map {
//      case JObject(("timestamp", timestamp: JString), ("component", component: JString),
//      ("check_state", checkState: JString), ("state", state: JString)) =>
//        Event(timestamp, component, checkState, State.withName(state))
//    }
//    case _ => Vector()
//  }
//
//  val graph = (JSONReader.read(Source.fromFile(graphJSON).mkString) \ "components" match {
//    case jList: JList => jList.values map {
//
//      case JObject(("id", id: JString), ("own_state", ownState: JString), ("derived_state", derivedState: JString),
//       ("check_states", checkStates: JObject), ("depends_on", dependsOn: JList)) =>
//        id.string -> Component(id, State.withName(ownState), State.withName(derivedState), extractCheckStates(checkStates),
//          Vector(), dependsOn.getAsStrings)
//
//      case JObject(("id", id: JString), ("own_state", ownState: JString), ("derived_state", derivedState: JString),
//       ("check_states", checkStates: JObject), ("dependency_of", dependencyOf: JList)) =>
//        id.string -> Component(id, State.withName(ownState), State.withName(derivedState), extractCheckStates(checkStates),
//          dependencyOf.getAsStrings, Vector())
//
//      case JObject(("id", id: JString), ("own_state", ownState: JString), ("derived_state", derivedState: JString),
//       ("check_states", checkStates: JObject), ("depends_on", dependsOn: JList), ("dependency_of", dependencyOf: JList)) =>
//        id.string -> Component(id, State.withName(ownState), State.withName(derivedState), extractCheckStates(checkStates),
//          dependencyOf.getAsStrings, dependsOn.getAsStrings)
//    }
//    case _ => Vector()
//  }).toMap
//
//  println(JObject(
//    ("graph", JObject(
//      ("components", JList(
//        applyEvents(graph, events).map {
//          case Component(id, ownState, derivedState, checkStates, Vector(), dependsOn) =>
//            JObject(
//              ("id", JString(id)),
//              ("own_state", JString(ownState.toString)),
//              ("derived_state", JString(derivedState.toString)),
//              ("check_state", JObject(checkStates.map{ case (key, state) => (key, JString(state.toString)) }.toVector:_*)),
//              ("depends_on", JList(dependsOn.map(JString):_*))
//            )
//          case Component(id, ownState, derivedState, checkStates, dependencyOf, Vector()) =>
//            JObject(
//              ("id", JString(id)),
//              ("own_state", JString(ownState.toString)),
//              ("derived_state", JString(derivedState.toString)),
//              ("check_state", JObject(checkStates.map{ case (key, state) => (key, JString(state.toString)) }.toVector:_*)),
//              ("dependency_of", JList(dependencyOf.map(JString):_*))
//            )
//          case Component(id, ownState, derivedState, checkStates, dependencyOf, dependsOn) =>
//            JObject(
//              ("id", JString(id)),
//              ("own_state", JString(ownState.toString)),
//              ("derived_state", JString(derivedState.toString)),
//              ("check_state", JObject(checkStates.map{ case (key, state) => (key, JString(state.toString)) }.toVector:_*)),
//              ("depends_on", JList(dependsOn.map(JString):_*)),
//              ("dependency_of", JList(dependencyOf.map(JString):_*))
//            )
//        }:_*
//      ))
//    ))
//  ).write)

//  def applyEvents(graph: GraphT, events: Vector[Event]): Vector[Component] = {
//
//    @tailrec
//    def applyEventsRec(events: Vector[Event], acc: GraphT): GraphT = events match {
//      case Vector() => acc
//      case event +: tail =>
//        val (allEvents, accGraph) = acc.get(event.component).fold((tail, acc)) { component =>
//          val depDerivedState = component.`depends_on`
//            .flatMap(dependency => acc.get(dependency))
//            .foldLeft(State.NoData){ (accState, dependency) =>
//              if (accState < dependency.`derived_state`) dependency.`derived_state`  else accState
//            }
//
//          val (addedEvents, newComponent) = calculateState(component, event, depDerivedState)
//          (addedEvents ++: tail, acc + (component.id -> newComponent))
//        }
//
//        applyEventsRec(allEvents, accGraph)
//    }
//
//    applyEventsRec(events, graph).values.toVector
//  }
//
//  private def calculateState(component: Component, event: Event, highestDepDerivedState: State): (Vector[Event], Component) = {
//    val newState = determineStateUpdate(component, event, highestDepDerivedState)
//
//    (newState.dependentEvents, component.copy(`check_states` = newState.checkStates, `own_state` = newState.ownState,
//      `derived_state` = newState.derivedState))
//  }
//
//  private def determineStateUpdate(component: Component, event: Event, highestDepDerivedState: State): StateUpdate = {
//    event.`check_state` match {
//      case PROPAGATED =>
//
//        val (derivedState, dependentEvents) = if(component.`own_state` < event.state && event.state > State.Clear) {
//          (event.state,
//            component.`dependency_of`.map( componentId => Event(event.timestamp, componentId, PROPAGATED, event.state))
//          )
//        } else (State.NoData, Vector.empty[Event])
//
//        StateUpdate(
//          checkStates = component.`check_states`,
//          ownState = component.`own_state`,
//          derivedState = derivedState,
//          dependentEvents = dependentEvents
//        )
//
//      case checkState @ _ =>
//        val checkStates = component.`check_states` + (checkState -> event.state)
//        val ownState = checkStates.values.fold(State.NoData)((acc, state) => if(acc > state) acc else state)
//        val (derivedState, dependentEvents) = if(ownState >= State.Warning && component.`derived_state` <= State.Clear) {
//          (ownState, component.`dependency_of`.map(componentId => Event(event.timestamp, componentId, PROPAGATED, ownState)))
//        } else if (highestDepDerivedState >= State.Clear) {
//          (highestDepDerivedState, Vector.empty[Event])
//        } else if (ownState <= State.Clear && component.`derived_state` >= State.Warning) {
//          (State.NoData,
//            component.`dependency_of`.map( componentId => Event(event.timestamp, componentId, PROPAGATED, ownState))
//          )
//        } else (State.NoData, Vector.empty[Event])
//
//        StateUpdate(
//          checkStates = checkStates,
//          ownState = ownState,
//          derivedState = derivedState,
//          dependentEvents = dependentEvents
//        )
//
//    }
//  }