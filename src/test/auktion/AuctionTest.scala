package auktion

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class AuctionTest extends TestKit(ActorSystem("AuctionTest")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

}
