package auktion

import java.util.concurrent.TimeUnit

import akka.actor.{Props, Actor}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AuctionManager extends Actor {
  val auctionsTitles = List("380", "333", "777", "747")
  val buyers = (1 to 3).map(num => context.actorOf(Props[Buyer], "buyer"+num)).toList
  val theOnlySeller = context.actorOf(Seller.props(auctionsTitles))
  val auctionRegistry = context.actorOf(Props[AuctionSearch], "auctionRegistry")

  def receive = {

    case Blabla => {
      theOnlySeller ! Publish
    }

    case BroadcastRequests => {
      buyers.map(_ ! BidRequest)
      context.system.scheduler.scheduleOnce(FiniteDuration(4, TimeUnit.SECONDS), context.self, BroadcastRequests)
    }

  }
}
