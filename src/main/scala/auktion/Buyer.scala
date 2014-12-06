package auktion

import akka.actor.{ActorRef, Props, Actor}
import akka.event.Logging

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Buyer {
  def props(keyword: String): Props = Props(new Buyer(keyword))
}

class Buyer(keywordOfInterest: String) extends Actor{

  val log = Logging(context.system, this)

  override def receive = {

    case MatchingAuctions(auctions: List[ActorRef]) => {
      log.debug("got matching auctions: {}", auctions)

      if (auctions.length > 0) {
        val auctionToBid = auctions(Random.nextInt(auctions.length))
        log.debug("gonna bid: {}", auctionToBid)
        auctionToBid ! Bid(Random.nextInt(100))
      }
    }

    case BidRequest => {
      log.debug("Buddy {} received request for bid", self.path.name)
      context.actorSelection("../auctionRegistry") ! AuctionLookup(keywordOfInterest)
    }

    case Bought(itemName) => {
      println(self.path.name + " bought cool item: " + itemName)
    }

  }
}
