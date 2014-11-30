package auktion

import akka.actor.{ActorRef, Props, Actor}
import akka.event.Logging

import scala.util.Random

object Buyer {
  def props(auctions: List[ActorRef]): Props = Props(new Buyer(auctions))
}

class Buyer(auctions: List[ActorRef]) extends Actor{

  val log = Logging(context.system, this)

  override def receive = {
    case BidRequest => {
      log.debug("Buddy {} received request for bid", self.path.name)
      val auctionToBid = auctions(Random.nextInt(auctions.length))
      auctionToBid ! Bid(Random.nextInt(100))
    }
    case Bought(itemName) => {
      println(self.path.name + " bought cool item: " + itemName)
    }
  }
}
