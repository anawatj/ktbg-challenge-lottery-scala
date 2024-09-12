package mappings

import databases.DatabaseComponent
import models.User


trait UserTable extends DatabaseComponent {
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "users") {


    def id = column[Int]("id", O.PrimaryKey,O.AutoInc)

    def username = column[String]("username")

    def password = column[String]("password")

    def first_name = column[String]("first_name")

    def last_name = column[String]("last_name")
    def role = column[String]("role")

    def * = (id, username, password,first_name,last_name,role)<>(User.tupled, User.unapply)
  }
  val Users = TableQuery[UserTable]


}

