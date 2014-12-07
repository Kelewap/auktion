package auktion

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, FSM, Props}
import akka.event.LoggingReceive
import akka.persistence.{RecoveryCompleted, PersistentActor}

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
  var lastTick: Long = System.currentTimeMillis()

  var duration: Long = 0
  var data: AuctionData = AuctionData(null, INITIAL_BID)

  def updateState(event: StateChangeEvent): Unit = {
//    println(event)
    data = event.data
    duration = event.duration
    context.become(
      event.state match {
        case Created => created
        case Ignored => ignored
        case Activated => activated
        case Sold => sold
    })
  }

  def updateTimer(): Unit = {
    duration += System.currentTimeMillis() - lastTick
    lastTick = System.currentTimeMillis()
  }

  def created: Receive = LoggingReceive {
    case BidTimerExpired => {
      persist(StateChangeEvent(Ignored, data, duration)) {
        event => {
//          log.debug("EXPIRED")
          updateState(event)
          updateTimer()
        }
      }
    }

    case Bid(value) if value > INITIAL_BID => {
      persist(StateChangeEvent(Activated, AuctionData(sender, value), duration)) {
        event => {
          println(self.path.name + " received valid initial bid: " + value + " from " + sender.path.name)
          updateState(event)
          updateTimer()
        }
      }
    }

    case Bid(value) => {
//      log.debug("received ignored bid: {}", value)
    }

    case RecoveryCompleted => {
      val newBidTimer = FiniteDuration(bidTime.toMillis - duration, TimeUnit.MILLISECONDS)
      context.system.scheduler.scheduleOnce(newBidTimer, context.self, BidTimerExpired)
    }
  }

  def activated: Receive = LoggingReceive {
    case BidTimerExpired => {
      persist(StateChangeEvent(Sold, data, duration)) {
        event => {
          data.bestBidder ! Bought("item of " + self.path.name)
          owner ! AuctionEnded
          context.system.scheduler.scheduleOnce(deleteTime, context.self, DeleteTimerExpired)
          updateState(event)
          updateTimer()
        }
      }
    }

    case Bid(value) if value > data.bestPrice => {
      persist(StateChangeEvent(Activated, AuctionData(sender, value), duration)) {
        event => {
          println(self.path.name + " received valid bid: " + value + " from " + sender.path.name)
          updateState(event)
          updateTimer()
        }
      }
    }

    case Bid(value) => {
//      log.debug("received ignored bid: {}", value)
    }

    case RecoveryCompleted => {
      val newBidTimer = FiniteDuration(bidTime.toMillis - duration, TimeUnit.MILLISECONDS)
      context.system.scheduler.scheduleOnce(newBidTimer, context.self, BidTimerExpired)
    }
  }

  def sold: Receive = LoggingReceive {
    case DeleteTimerExpired => {
      context.stop(self)
    }
  }

  def ignored: Receive = LoggingReceive {
    case Relist => {
      persist(StateChangeEvent(Activated, data, duration)) {
        event => {
          context.system.scheduler.scheduleOnce(bidTime, context.self, BidTimerExpired)
          updateState(event)
          updateTimer()
        }
      }
    }
  }

  override def receiveRecover: Receive = {
    case evt: StateChangeEvent => updateState(evt)

    case RecoveryCompleted => {
      val newBidTimer = FiniteDuration(bidTime.toMillis - duration, TimeUnit.MILLISECONDS)
      context.system.scheduler.scheduleOnce(newBidTimer, context.self, BidTimerExpired)
    }
  }

  override def receiveCommand: Receive = created
}

sealed trait Data
case object Uninitialized extends Data
case class AuctionData(bestBidder:ActorRef, bestPrice:Int) extends Data

case class StateChangeEvent(state: State, data: AuctionData, duration: Long)