package repositories

import databases.PostgresComponent
import mappings.UserTable
import models.User

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepository extends BaseRepository[User,Int] {

    def findByUserName(username:String):Future[Option[User]]

}

class UserRepositoryImpl extends UserRepository with PostgresComponent with UserTable {
  import profile.api._
  override def add(data: User): Future[User] = {

    db.run(Users+=data) map {
      _ => data
    }

  }

  override def findByUserName(username: String): Future[Option[User]] = {
    db.run(Users.filter(s=>s.username===username).result.headOption) map {
      user=>user
    }
  }

  override def bulkAdd(list: List[User]): Future[List[User]] = {
    db.run(Users++=list) map {
      _ => list
    }
  }


  override def delete(id: Int): Future[Unit] = {
    db.run(Users.filter(_.id===id).delete) map {
      _ =>()
    }
  }

  override def find(id: Int): Future[Option[User]] = {
    db.run(Users.filter(_.id===id).result.headOption) map{
      user=>user
    }
  }

  override def update(data: User, id: Int): Future[User] = {
    db.run(Users.filter(_.id===id).update(data)) map  {
      _ => data
    }
  }

  override def findAll(): Future[List[User]] = {
    db.run(Users.result) map {
      users=>users.toList
    }
  }

}
