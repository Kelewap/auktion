package auktion

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{RoutingLogic, ActorRefRoutee, Router}

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

  var registerRouter = Router(registerLogic, routees)
  var lookupRouter = Router(lookupLogic, routees)

  override def receive = LoggingReceive {
    case register: Register => {
      registerRouter.route(register, sender())
    }

    case loopkup: AuctionLookup => {
      lookupRouter.route(loopkup, sender())
    }
//
//    case MasterSearch.Terminated(terminatedRoutee) => {
//      registerRouter = registerRouter.removeRoutee(terminatedRoutee)
//      val newRoutee = context.actorOf(Props[AuctionSearch])
//      context.watch(newRoutee)
//      registerRouter = registerRouter.addRoutee(newRoutee)
//    }
  }

}

class RouterProperties(logic: RoutingLogic, poolSize: Int) {
  def getLogic: RoutingLogic = logic
  def getPoolSize: Int = poolSize
}
