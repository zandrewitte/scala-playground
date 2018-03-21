package com.zantech.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, ClosedShape, Outlet, SourceShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val responseFuture = Http().singleRequest(HttpRequest(uri = "https://data.sfgov.org/resource/cuks-n6tp.json"))

  val repeatedFetch = Source.tick(1 second, 5 seconds, responseFuture)

  repeatedFetch
    .via(Flow[Future[HttpResponse]].mapAsync(1)(identity))
    .via(Flow[HttpResponse].mapAsync(1)(_.entity.dataBytes.runFold(ByteString(""))(_ ++ _)))
    .via(Flow[ByteString].map(_.utf8String))


  val g = RunnableGraph.fromGraph(GraphDSL.create(Sink.seq[Int]) { implicit builder => out =>
    import GraphDSL.Implicits._
    val in = Source.repeat(1)

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    val getResponse = Flow[Future[HttpResponse]].mapAsync(1)(identity)
    val extractBytes = Flow[HttpResponse].mapAsync(1)(_.entity.dataBytes.runFold(ByteString(""))(_ ++ _))
    val toByteString = Flow[ByteString].map(_.utf8String)

    repeatedFetch ~> getResponse ~> extractBytes ~> toByteString

    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
    bcast ~> f4 ~> merge

    ClosedShape
  })

  g.run()


}
