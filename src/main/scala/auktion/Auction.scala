package auktion

import akka.actor.{Actor, ActorRef, FSM, Props}

import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait State
case object Created extends State
case object Ignored extends State
case object Activated extends State
case object Sold extends State

object Auction {
  def props(bidTime: FiniteDuration, deleteTime: FiniteDuration): Props = Props(new Auction(bidTime, deleteTime))
}

class Auction(bidTime: FiniteDuration, deleteTime: FiniteDuration) extends Actor with FSM[State, Data] {
  context.system.scheduler.scheduleOnce(bidTime, context.self, BidTimerExpired)

  val INITIAL_BID = 10

  startWith(Created, Uninitialized)

  when(Created) {
    case Event(BidTimerExpired, _) => {
      log.debug("EXPIRED")
      goto(Ignored)
    }

    case Event(Bid(value), _) if value > INITIAL_BID => {
      println(self.path.name + " received valid initial bid: " + value + " from " + sender.path.name)
      goto(Activated) using AuctionData(sender, value)
    }

    case Event(Bid(value), _) => {
      log.debug("received ignored bid: {}", value)
      stay()
    }
  }

  when(Activated) {
    case Event(Bid(value), AuctionData(bestBidder, bestPrice)) if value > bestPrice => {
      println(self.path.name + " received valid bid: " + value + " from " + sender.path.name)
      goto(Activated) using AuctionData(sender, value)
    }

    case Event(Bid(value), _) => {
      log.debug("received ignored bid: {}", value)
      stay()
    }

    case Event(BidTimerExpired, AuctionData(bestBidder, bestPrice)) => {
      bestBidder ! Bought("item of " + self.path.name)
      context.system.scheduler.scheduleOnce(deleteTime, context.self, DeleteTimerExpired)
      goto(Sold) using AuctionData(bestBidder, bestPrice)
    }
  }

  when(Sold) {
    case Event(DeleteTimerExpired, _) => {
      stop
    }
  }


  when(Ignored) {
    case Event(Relist, _) => {
      context.system.scheduler.scheduleOnce(bidTime, context.self, BidTimerExpired)
      goto(Created) using Uninitialized
    }

    case Event(DeleteTimerExpired, _) => {
      stop
    }
  }

}

sealed trait Data
case object Uninitialized extends Data
case class AuctionData(bestBidder:ActorRef, bestPrice:Int) extends Data