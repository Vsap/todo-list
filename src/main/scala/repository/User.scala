package repository

import model._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.concurrent.Future

case class User(login: String, password: String){
}

class UserTable(tag: Tag)  extends Table[User](tag, "users"){
  val login = column[String]("login", O.PrimaryKey)
  val password = column[String]("password")

  def * = (login, password) <> (User.apply _ tupled, User.unapply)
}

object UserTable{val table = TableQuery[UserTable]}


class UserRepository(db: Database){
  def insert(user: User): Future[User] =
    db.run(UserTable.table returning UserTable.table += user)
  def delete(user: User): Future[Int] =
    db.run(UserTable.table.filter(_.login === user.login).delete)
}