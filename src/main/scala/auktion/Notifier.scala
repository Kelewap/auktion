package auktion

import spray.client.pipelining._
import spray.http.HttpRequest
import spray.http.HttpResponse
import akka.actor.Actor
import akka.event.Logging
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class Notifier extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {

    case Notify(event) => {
//      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
//
//      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/"))
//
//      response onComplete {
//        case Success(r) => println(r)
//        case Failure(e) => println("An error has occured: " + e.getMessage)
//      }
//      Await.result(response, 5 seconds)
    }

  }
}


