package com.danielasfregola.quiz.management



import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import akka.pattern.ask
import scala.concurrent.duration._

/**
  * Created by admaster on 16/5/11.
  */
object MyMain extends App{

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("dmp-third-data-api")

  val api = system.actorOf(Props(new MyRestInterface()),"httpInterface")

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)

  IO(Http).ask(Http.Bind(listener = api,interface = host,port = port))
    .mapTo[Http.Event]
    .map{
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println("REST interface could not bind to " +
          s"$host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }

}
