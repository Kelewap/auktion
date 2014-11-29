package auktion

sealed trait AuctionMessage
sealed trait ManagementMessage
sealed trait BuyerMessage

case class BidRequest extends AuctionMessage
case class Bought extends AuctionMessage

case class BroadcastRequests extends ManagementMessage

case class Bid(value:Int) extends BuyerMessage
case class BidTimerExpired extends BuyerMessage
case class DeleteTimerExpired extends BuyerMessage
case class Relist extends BuyerMessage
