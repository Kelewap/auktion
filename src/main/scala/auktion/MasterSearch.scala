package auktion

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing._

object MasterSearch {
  def props(poolSize: Int, registerLogic: RoutingLogic, lookupLogic: RoutingLogic): Props = Props(new MasterSearch(poolSize, registerLogic, lookupLogic))

  case class Terminated(a: ActorRef)
}

class MasterSearch(poolSize: Int, registerLogic: RoutingLogic, lookupLogic: RoutingLogic) extends Actor{

  val routees = Vector.fill(poolSize) {
    val routee = context.actorOf(Props[AuctionSearch])
    context.watch(routee)
    ActorRefRoutee(routee)
  }

  val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)

  var registerRouter = BroadcastPool( poolSize, Some(resizer) ).createRouter(context.system).withRoutees(routees)
  var lookupRouter  = RoundRobinPool( poolSize, Some(resizer) ).createRouter(context.system).withRoutees(routees)

  override def receive = LoggingReceive {
    case register: Register => {
      registerRouter.route(register, sender())
    }

    case loopkup: AuctionLookup => {
      lookupRouter.route(loopkup, sender())
    }

  }

}

class RouterProperties(logic: RoutingLogic, poolSize: Int) {
  def getLogic: RoutingLogic = logic
  def getPoolSize: Int = poolSize
}
