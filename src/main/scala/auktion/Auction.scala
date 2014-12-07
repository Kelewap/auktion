package auktion

import akka.actor.{Actor, ActorRef, FSM, Props}
import akka.event.LoggingReceive
import akka.persistence.PersistentActor

import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait State
case object Created extends State
case object Ignored extends State
case object Activated extends State
case object Sold extends State

object Auction {
  def props(bidTime: FiniteDuration, deleteTime: FiniteDuration, owner: ActorRef): Props = Props(new Auction(bidTime, deleteTime, owner))
}

class Auction(bidTime: FiniteDuration, deleteTime: FiniteDuration, owner: ActorRef) extends PersistentActor {
  context.system.scheduler.scheduleOnce(bidTime, context.self, BidTimerExpired)

  val INITIAL_BID = 10
  override def persistenceId = "persistent-auction-001"

  var data: AuctionData = AuctionData(null, INITIAL_BID)

  def updateState(event: StateChangeEvent): Unit = {
    data = event.data
    context.become(
      event.state match {
        case Created => created
        case Ignored => ignored
        case Activated => activated
        case Sold => sold
    })
  }

  def created: Receive = LoggingReceive {
    case BidTimerExpired => {
      persist(StateChangeEvent(Ignored, data)) {
        event => {
//          log.debug("EXPIRED")
          updateState(event)
        }
      }
    }

    case Bid(value) if value > INITIAL_BID => {
      persist(StateChangeEvent(Activated, AuctionData(sender, value))) {
        event => {
          println(self.path.name + " received valid initial bid: " + value + " from " + sender.path.name)
          updateState(event)
        }
      }
    }

    case Bid(value) => {
//      log.debug("received ignored bid: {}", value)
    }
  }

  def activated: Receive = LoggingReceive {
    case BidTimerExpired => {
      persist(StateChangeEvent(Sold, data)) {
        event => {
          data.bestBidder ! Bought("item of " + self.path.name)
          owner ! AuctionEnded
          context.system.scheduler.scheduleOnce(deleteTime, context.self, DeleteTimerExpired)
          updateState(event)
        }
      }
    }

    case Bid(value) if value > data.bestPrice => {
      persist(StateChangeEvent(Activated, AuctionData(sender, value))) {
        event => {
          println(self.path.name + " received valid bid: " + value + " from " + sender.path.name)
          updateState(event)
        }
      }
    }

    case Bid(value) => {
//      log.debug("received ignored bid: {}", value)
    }
  }

  def sold: Receive = LoggingReceive {
    case DeleteTimerExpired => {
      context.stop(self)
    }
  }

  def ignored: Receive = LoggingReceive {
    case Relist => {
      persist(StateChangeEvent(Activated, data)) {
        event => {
          context.system.scheduler.scheduleOnce(bidTime, context.self, BidTimerExpired)
          updateState(event)
        }
      }
    }
  }

  override def receiveRecover: Receive = {
    case evt: StateChangeEvent => updateState(evt)
  }

  override def receiveCommand: Receive = created
}

sealed trait Data
case object Uninitialized extends Data
case class AuctionData(bestBidder:ActorRef, bestPrice:Int) extends Data

case class StateChangeEvent(state: State, data: AuctionData)