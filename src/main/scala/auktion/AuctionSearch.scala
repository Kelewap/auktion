package auktion

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Actor}
import akka.event.Logging

import scala.concurrent.duration.FiniteDuration


class AuctionSearch() extends Actor {

  val log = Logging(context.system, this)
  var knownAuctions : Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case Register(title) => {
      log.debug("got register request for auction: {} from {}", title, sender)
      val auction = context.actorOf(Auction.props(FiniteDuration(10, TimeUnit.SECONDS), FiniteDuration(10, TimeUnit.SECONDS), sender))
      knownAuctions = knownAuctions + (title -> auction)
    }

    case AuctionLookup(keyword) => {
      log.debug("lookup for keyword: {}", keyword)
      //TODO: extract precondition to a fking val
      sender ! MatchingAuctions(knownAuctions.filter({case (title:String, auction:ActorRef) => title.contains(keyword)}).values.toList)
    }
  }
}


