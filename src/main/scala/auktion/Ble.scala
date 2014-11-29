package auktion

import akka.actor.{Props, ActorSystem}
import akka.event.Logging

object Ble {

  val system = ActorSystem()

  val log = Logging(system, Ble.getClass().getName())

  def main(args: Array[String]): Unit = run()

  def run() = {
    log.debug("Initializing auction system.")
    val manager = system.actorOf(Props[AuctionManager], "manager")
    manager ! BroadcastRequests
  }
}