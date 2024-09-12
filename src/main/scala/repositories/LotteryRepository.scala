package repositories

import databases.PostgresComponent
import mappings.LotteryTable
import models.Lottery

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future


trait   LotteryRepository extends BaseRepository[Lottery,String]{

}
class LotteryRepositoryImpl extends LotteryRepository with PostgresComponent with LotteryTable{
  import profile.api._
  override def bulkAdd(list: List[Lottery]): Future[List[Lottery]] = {
    db.run(Lotteries++=list) map {
      _ => list
    }
  }

  override def add(data: Lottery): Future[Lottery] = {
      db.run(Lotteries+=data) map {
        _ => data
      }
  }

  override def delete(id: String): Future[Unit] = {
    db.run(Lotteries.filter(_.ticket===id).delete) map {
      _ => ()
    }
  }

  override def find(id: String): Future[Option[Lottery]] = {
    db.run(Lotteries.filter(_.ticket===id).result.headOption) map {
      lottery=>lottery
    }
  }

  override def update(data: Lottery, id: String): Future[Lottery] = {
    db.run(Lotteries.filter(t=>t.ticket===id).update(data)) map {
      _ => data
    }
  }

  override def findAll(): Future[List[Lottery]] = {
    db.run(Lotteries.result) map {
      lotteries=>lotteries.toList
    }
  }


}
