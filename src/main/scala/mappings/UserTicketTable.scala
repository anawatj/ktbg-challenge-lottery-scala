package mappings

import databases.DatabaseComponent
import models.UserTicket
trait UserTicketTable extends DatabaseComponent {
  import profile.api._
  class UserTicketTable(tag:Tag) extends Table[UserTicket](tag,"user_tickets"){
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc)
    def user_id = column[Int]("user_id")
    def ticket = column[String]("ticket")
    def * = (id, user_id,ticket)<>(UserTicket.tupled, UserTicket.unapply)
  }
  val UserTickets = TableQuery[UserTicketTable]
}

