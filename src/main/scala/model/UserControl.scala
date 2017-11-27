package model

import repository._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import sun.security.util.Password

import scala.concurrent.Future

object Interface{
  val db = Database.forURL(
    "jdbc:postgresql://127.0.0.1/postgres?user=postgres&password=swordfish"
  )
  val taskRepository = new TaskRepository(db)
  val userRepository = new UserRepository(db)

  case class UserControl(login: String = "data", password: String = "data"){
    def getTasks = taskRepository.getAll(login)
    def getByStatus(status: Int) = taskRepository.getByStatus(login, 1)
    //def changeStatus(status: Int) = taskRepository.update()
    def add(text: String) =
      taskRepository.insert(Task(Some(0), login, text, 1))
    def removeById(id: Long) =
      taskRepository.deleteById(login, id)
    def removeByStatus(status: Int) =
      taskRepository.deleteByStatus(login, status)
  }

  //REMAKE Authorization!!!!!!!!!!!
  class Authorization{
    def signUp(login: String, password: String) = ifExists(login) match {
      case false => throw new Exception("This username already exists")  //fix exeption to program pointer!!!!!!!!
      case _ => {userRepository.insert(User(login, password));UserControl(login, password)}
    }
    def ifExists(login: String): Boolean = Option(userRepository.getByLogin(login)) match {case Some(_) => true; case None => false}
    def signIn(login: String, password: String) = Option(userRepository.ifMatches(User(login,password))) match {
      case Some(_) => UserControl(login, password) //remake!!!!!
      case None => throw new Exception("Wrong username or password!") ///fix exeption
    }
  }

  class Menu{

  }

}
