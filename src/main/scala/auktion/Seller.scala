package auktion

import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging

import scala.concurrent.duration.FiniteDuration

object Seller {
  def props(auctionTitles: List[String]): Props = Props(new Seller(auctionTitles))
}

class Seller(auctionTitles: List[String]) extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {

    case Publish => {
      log.debug("publishing")
      val registry = context.actorSelection("../auctionRegistry")
      auctionTitles.map(title => registry ! Register(title))
    }

    case AuctionEnded => {
      println("sold: " + sender)
    }
  }
}


