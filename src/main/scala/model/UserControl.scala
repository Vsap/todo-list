package model

import Main.db
import com.sun.scenario.effect.impl.state.LinearConvolveRenderState.PassType
import repository._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.Future

object Interface{
  val db = Database.forURL(
    "jdbc:postgresql://127.0.0.1/postgres?user=postgres&password=swordfish"
  )
  val taskRepository = new TaskRepository(db)
  val userRepository = new UserRepository(db)

  case class UserControl(login: String = "data", password: String = "data"){
    def getTasks = taskRepository.
    def getByStatus(status: Int) = TaskTable.table.filter(_.userId === login).filter(_.status === status).result
    def changeStatus(status: Int) = TaskTable.table.filter(_.userId === login).filter(_.status === status).result
    def add(text: String) = TaskTable.table returning TaskTable.table += Task( login, text, 1)
    def removeById(id: Long) = TaskTable.table.filter(_.userId === login).filter(_.id === id).result
    def removeByStatus(status: String) = TaskTable.table.filter(_.userId === login).filter(_.status === status).result
  }
}
