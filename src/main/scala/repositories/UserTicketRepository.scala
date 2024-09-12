package repositories

import databases.PostgresComponent
import mappings.{LotteryTable, UserTable, UserTicketTable}
import models.{User, UserTicket}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserTicketRepository extends BaseRepository[UserTicket,Int]{
  def findByUser(user_id:Int):Future[List[UserTicket]]
  def findByPK(user_id:Int,ticket:String):Future[Option[UserTicket]]
  def deleteByPk(user_id:Int,ticket:String):Future[Unit]
}
class UserTicketRepositoryImpl extends UserTicketRepository with PostgresComponent with UserTicketTable with UserTable with LotteryTable{
  import profile.api._

  override def add(data: UserTicket): Future[UserTicket] = {
    db.run(UserTickets+=data) map {
      _ => data
    }
  }

  override def bulkAdd(list: List[UserTicket]): Future[List[UserTicket]] = {
    db.run(UserTickets++=list) map {
      _ => list
    }
  }

  override def findAll(): Future[List[UserTicket]] = {
    db.run(UserTickets.result) map {
      userTickets => userTickets.toList
    }
  }

  override def find(id: Int): Future[Option[UserTicket]] = {
    db.run(UserTickets.filter(_.id===id).result.headOption) map {
      userTicket => userTicket
    }
  }

  override def update(data: UserTicket, id: Int): Future[UserTicket] = {
    db.run(UserTickets.filter(_.id===id).update(data)) map {
      _ => data
    }
  }



  override def delete(id: Int): Future[Unit] = {
    db.run(UserTickets.filter(_.id===id).delete) map {
      _ => ()
    }
  }

  override def findByUser(user_id: Int): Future[List[UserTicket]] = {
    db.run(UserTickets.filter(_.user_id===user_id).result) map {
      tickets=>tickets.toList
    }
  }

  override def deleteByPk(user_id: Int, ticket: String): Future[Unit] = {
    db.run(UserTickets.filter(t=>t.user_id===user_id && t.ticket===ticket).delete) map {
      _ =>()
    }
  }

  override def findByPK(user_id: Int, ticket: String): Future[Option[UserTicket]] = {
    db.run(UserTickets.filter(t=>t.user_id===user_id && t.ticket===ticket).result.headOption) map {
      userTicket=> userTicket
    }
  }
}
