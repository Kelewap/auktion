package auktion

import akka.actor.ActorRef

sealed trait BuyerMessage
sealed trait ManagementMessage
sealed trait AuctionMessage
sealed trait RegistryMessage
sealed trait SellerMessage
sealed trait NotifierMessage

case class BidRequest extends BuyerMessage
case class Bought(itemName: String) extends BuyerMessage
case class MatchingAuctions(auctions: List[ActorRef]) extends BuyerMessage
case class StartBidding() extends BuyerMessage

case class BroadcastRequests extends ManagementMessage
case class Blabla extends ManagementMessage

case class Bid(value:Int) extends AuctionMessage
case class BidTimerExpired extends AuctionMessage
case class DeleteTimerExpired extends AuctionMessage
case class Relist extends AuctionMessage
case class Tickk extends AuctionMessage

case class Register(title: String, auction: ActorRef) extends RegistryMessage
case class AuctionLookup(keyword: String) extends RegistryMessage

case class Publish extends SellerMessage
case class AuctionEnded extends SellerMessage

case class Notify(event: StateChangeEvent) extends NotifierMessage