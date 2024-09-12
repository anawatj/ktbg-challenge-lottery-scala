package mappings
import databases.DatabaseComponent
import models.Lottery
trait LotteryTable extends DatabaseComponent {
  import profile.api._
  class LotteryTable(tag:Tag) extends Table[Lottery](tag,"lotteries"){
    def ticket = column[String]("ticket", O.PrimaryKey)
    def price = column[Double]("price")
    def amount = column[Int]("amount")
    def * = (ticket, price,amount)<>(Lottery.tupled, Lottery.unapply)
  }
  val Lotteries = TableQuery[LotteryTable]
}
