import repository._
import slick.jdbc.PostgresProfile.api._
import model._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  val db = Database.forConfig("connect")
  val taskRepository = new TaskRepository(db)
  val userRepository = new UserRepository(db)

  def main(args: Array[String]): Unit = {Interface.Menu.init}
  def clear = {
    Await.result(taskRepository.deleteByStatus("vlad", 1), Duration.Inf)
    Await.result(userRepository.delete(User("vlad", "vlad")), Duration.Inf)

  }
  def init = {Await.result(db.run(UserTable.table.schema.create), Duration.Inf)
    Await.result(db.run(TaskTable.table.schema.create), Duration.Inf)}
  def fill = {  Await.result(userRepository.insert(User("data", "data")), Duration.Inf)
    Await.result(userRepository.insert(User("root", "root")), Duration.Inf)
    Await.result(userRepository.insert(User("vlad", "vlad")), Duration.Inf)
    Await.result(taskRepository.insert(Task(Some(1),"data", "make a present", 1)), Duration.Inf)
    Await.result(taskRepository.insert(Task(Some(1),"vlad", "make a present to Alinka", 1)), Duration.Inf)}
}
