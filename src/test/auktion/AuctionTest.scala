package auktion

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class AuctionTest extends TestKit(ActorSystem("AuctionTest")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "An auction" must {

    "as brand-new, accept bid higher than initial" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(15)
      expectMsg(BidAccepted(15))
    }

    "as brand-new, accept bid exact to initial" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(10)
      expectMsg(BidAccepted(10))
    }

    "as brand-new, decline bid lower than initial" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(2)
      expectMsg(BidDenied(2))
    }

    "as once bidded, decline bid lower than last" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(20)
      expectMsg(BidAccepted(20))

      auction ! Bid(5)
      expectMsg(BidDenied(5))
    }

    "as once bidded, decline bid exact as the last" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(20)
      expectMsg(BidAccepted(20))

      auction ! Bid(20)
      expectMsg(BidDenied(20))
    }

    "as once bidded, accept bid higher than last" in {
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, TestProbe().ref, 10))

      auction ! Bid(20)
      expectMsg(BidAccepted(20))

      auction ! Bid(30)
      expectMsg(BidAccepted(30))
    }

    "as finished, should notify both seller an buyer" in {
      val seller = TestProbe()
      val auction = system.actorOf(Auction.props(2 seconds, 2 seconds, seller.ref, 10), "name")

      auction ! Bid(20)
      expectMsg(BidAccepted(20))

      expectMsg(Bought("item of name"))
      seller.expectMsg(AuctionEnded)
    }

  }
}
