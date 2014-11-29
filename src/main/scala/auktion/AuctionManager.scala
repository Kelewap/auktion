package auktion

import java.util.concurrent.TimeUnit

import akka.actor.Actor

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AuctionManager extends Actor {
  val auctions = (1 to 2).map(num => context.actorOf(Auction.props(FiniteDuration(10, TimeUnit.SECONDS), FiniteDuration(10, TimeUnit.SECONDS)), "auction"+num)).toList
  val buyers = (1 to 2).map(num => context.actorOf(Buyer.props(auctions), "buyer"+num)).toList

  def receive = {
    case BroadcastRequests =>
      buyers.map(_ ! BidRequest)
      context.system.scheduler.scheduleOnce(FiniteDuration(2, TimeUnit.SECONDS), context.self, BroadcastRequests)
  }
}
