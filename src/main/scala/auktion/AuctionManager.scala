package auktion

import java.util.concurrent.TimeUnit

import akka.actor.{Props, Actor}
import akka.routing.{BroadcastRoutingLogic, RoundRobinRoutingLogic}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class AuctionManager extends Actor {
  val POOL_SIZE = 5
  val PARTITIONING_REGISTRY = new RouterProperties(RoundRobinRoutingLogic(), 1)
  val PARTITIONING_LOOKUP = new RouterProperties(BroadcastRoutingLogic(), POOL_SIZE)
  val REPLICATING_REGISTRY = new RouterProperties(BroadcastRoutingLogic(), POOL_SIZE)
  val REPLICATING_LOOKUP = new RouterProperties(RoundRobinRoutingLogic(), 1)

  val FOR_REGISTER = REPLICATING_REGISTRY
  val FOR_LOOKUP = REPLICATING_LOOKUP

  val auctionsTitles = List("380", "333", "777", "747", "777a", "777b", "777c", "777d", "777e", "767a", "767b", "767c", "767d", "767e", "767f", "767g")
  val buyers = (1 to 30).map(num => context.actorOf(Buyer.props("7"), "buyer"+num)).toList
  val theOnlySeller = context.actorOf(Seller.props(auctionsTitles))
  val auctionRegistry = context.actorOf(MasterSearch.props(POOL_SIZE, FOR_REGISTER, FOR_LOOKUP), "auctionRegistry")

  def receive = {

    case Blabla => {
      theOnlySeller ! Publish
    }

    case BroadcastRequests => {
      buyers.map(_ ! BidRequest)
      context.system.scheduler.scheduleOnce(FiniteDuration(1, TimeUnit.SECONDS), context.self, BroadcastRequests)
    }

  }
}
