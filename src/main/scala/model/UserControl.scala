package model

import repository._

import scala.io._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import sun.security.util.Password

import scala.concurrent.ExecutionContext.Implicits.global._
import scala.concurrent.Future

object Interface{
  val db = Database.forURL("jdbc:postgresql://127.0.0.1/postgres?user=postgres&password=root")
  val taskRepository = new TaskRepository(db)
  val userRepository = new UserRepository(db)

  case class UserControl(login: String = "data", password: String = "data"){
    def getTasks = taskRepository.getAll(login)
    def getByStatus(status: Int) = taskRepository.getByStatus(login, 1)
    //def changeStatus(status: Int) = taskRepository.update()
    def add(text: String) =
      taskRepository.insert(Task(Some(0), login, text, 1))
    def remove = taskRepository.deleteAll(login)
    def removeById(id: Long) =
      taskRepository.deleteById(login, id)
    def removeByStatus(status: Int) =
      taskRepository.deleteByStatus(login, status)
  }

  //REMAKE Authorization!!!!!!!!!!!
  object Authorization{
    def signUp(login: String, password: String) = ifExists(login) match {
      case false => {userRepository.insert(User(login, password));UserControl(login, password)}
      case _ => {println("This username already exists");Menu.init}  //fix exeption to program pointer!!!!!!!!
    }
    def ifExists(login: String): Boolean = Option(userRepository.getByLogin(login)) match {case Some(_) => true; case _ => false}
    def ifEquals(login: String, password: String): Boolean = Option(userRepository.getByLogin(login).flatten()) match {case Some(User(_,p)) => p == password; case _ => false}
    def signIn(login: String, password: String):Unit = ifEquals(login, password) match {
      case true => UserControl(login, password)
      case _ => {println("This username already exists");Menu.init}
    }
  }
  ////ADD CHANGE STATUS COMMAND (here & in def controller)
  object InputParser{
    val cmdPattern = """(-[a-zA-Z]*) ?([0-9a-zA-Z]*) ?([0-9a-zA-Z]*)""".r
    def input: Option[(String, Option[String], Option[String])] = cmdPattern.findFirstIn(StdIn.readLine())
      .map{case cmdPattern(command, login, password) => (command, Option(login), Option(password))}
    val authorizationCommands: List[String] = List("--help", "-signIn", "-signUp","-exit")
    val authCmdsDescription: List[String] = List(
      "input \"--help\" with no args to get all the commands with the description",
      "input \"-signIn [login] [password]\" to log in if already registered",
      "input \"-signUp [login] [password]\" to register",
      "input \"-exit\" to quit the program")
    val userControlCommands: List[String] = List("-getAll", "-todo", "-done", "-removeAll",
      "-removeDone", "-remove", "-mark", "-add")
    val usrCtrlCmdsDescription: List[String] = List(
      "input \"-getAll\" to look through all the tasks",
      "input \"-todo\" to look through the tasks to do",
      "input \"-done\" to look through the done tasks",
      "input \"-mark [task_id]\" to mark task as done",
      "input \"-removeAll\" to delete all your tasks",
      "input \"-removeDone\" to delete all your done tasks",
      "input \"-remove [task_id]\" to delete a specific task",
      "input \"-add\" to add a new task")
  }
  /////FIX INPUT PARSER  & UNCONTROLED INPUT
  object Menu{
    def init: Unit = {
      println("Welcome to Thomson's TODO List program.")
      println("Use commands for exploration: ")
      InputParser.authCmdsDescription.map(println)
      InputParser.usrCtrlCmdsDescription.map(println)
      auth
    }
    def auth: Unit = {
      InputParser.input match {
        case Some(("-signIn", Some(login), Some(password))) => {controller(Authorization.signIn(login, password))} ///  <--HERE!!!
        case Some(("-signUp", Some(login), Some(password))) => {controller(Authorization.signUp(login, password))} ///  <--HERE!!!
        case Some(("-help", Some(""), Some(""))) => init
        case Some(("-exit", Some(""), Some(""))) => {println("GoodBye!");()} ///ADD EXIT COMMAND!!
        case Some(_) => {println("In authorization mode \"Unknown command\" exception!"); auth}
        case None => {println("In authorization mode \"None Input\" exception!"); auth}
      }
    }
    def controller(uc: UserControl):Unit = {
      println("U are logged in as "+uc.login+ ". Welcome!")
      println("Control mode: ")
      println("Input the commands to manipulate the tasks")
      InputParser.input match {
//        case Some("-getAll") => {uc.getTasks.foreach(println);()}
//        case Some("-todo") => uc.getByStatus(1).map(println(_))
//        case Some("-done") => uc.getByStatus(0).map(println(_))
        case Some(("-mark", Some(""), Some(""))) => init //FIX!!
        case Some(("-removeAll", Some(""), Some(""))) => {uc.remove;controller(uc)}
        case Some(("-remove", Some(id), Some(""))) => {uc.removeById(id.toString.toInt);controller(uc)} ///FIX!!!
        case Some(("-removeDone", Some(""), Some(""))) => {uc.removeByStatus(0);controller(uc)}
        case Some(("-add", Some(text), Some(""))) => {uc.add(text.toString);controller(uc)} ///FIX!!!!
        case Some(("-prev", Some(""), Some(""))) => {auth}
        case Some(("-exit", Some(""), Some(""))) => {println("GoodBye!");()} ///ADD EXIT COMMAND!!
        case Some(_) => {println("In control mode \"Unknown command\" exception!"); controller(uc)}
        case None => {println("In control mode \"None Input\" exception!"); controller(uc)}
      }
    }
  }

}
