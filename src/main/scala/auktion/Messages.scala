package auktion

sealed trait BuyerMessage
sealed trait ManagementMessage
sealed trait AuctionMessage

case class BidRequest extends BuyerMessage
case class Bought(itemName:String) extends BuyerMessage

case class BroadcastRequests extends ManagementMessage

case class Bid(value:Int) extends AuctionMessage
case class BidTimerExpired extends AuctionMessage
case class DeleteTimerExpired extends AuctionMessage
case class Relist extends AuctionMessage
