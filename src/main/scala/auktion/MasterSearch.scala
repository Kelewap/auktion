package auktion

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{RoutingLogic, ActorRefRoutee, Router}

object MasterSearch {
  def props(poolSize: Int, registerProperties: RouterProperties, lookupProperties: RouterProperties): Props = Props(new MasterSearch(poolSize, registerProperties, lookupProperties))

  case class Terminated(a: ActorRef)
}

class MasterSearch(poolSize: Int, registerProperties: RouterProperties, lookupProperties: RouterProperties) extends Actor{

  val routees = Vector.fill(poolSize) {
    val routee = context.actorOf(Props[AuctionSearch])
    context.watch(routee)
    ActorRefRoutee(routee)
  }

  var registerRouter = Router(registerProperties.getLogic, routees.take(registerProperties.getPoolSize))
  var lookupRouter = Router(lookupProperties.getLogic, routees.take(lookupProperties.getPoolSize))

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
