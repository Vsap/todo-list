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
      case false => throw new Exception("This username already exists")  //fix exeption to program pointer!!!!!!!!
      case _ => {userRepository.insert(User(login, password));UserControl(login, password)}
    }
    def ifExists(login: String): Boolean = Option(userRepository.getByLogin(login)) match {case Some(_) => true; case None => false}
    def signIn(login: String, password: String) = Option(userRepository.ifMatches(User(login,password))) match {
      case Some(_) => UserControl(login, password) //remake!!!!!
      case None => throw new Exception("Wrong username or password!") ///fix exeption
    }
  }
  ////ADD CHANGE STATUS COMMAND (here & in def controller)
  object InputParser{
    val cmdPattern = """(-[a-zA-Z]*?) ([0-9a-zA-Z]*?) ([0-9a-zA-Z]*)""".r
    def input: Option[(String, Option[String], Option[String])] = cmdPattern.findFirstIn(StdIn.readLine()).map{case cmdPattern(command, login, password) => (command, Some(login), Some(password))}
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
        case Some(("-signUn", Some(login), Some(password))) => {controller(Authorization.signUp(login, password))} ///  <--HERE!!!
        case Some(("-help", None, None)) => init
        case Some(("-exit", None, None)) => {println("GoodBye!");()} ///ADD EXIT COMMAND!!
        case Some(_) => {println("Input error! Try again!"); auth}
        case None => {println("Input error! Try again!"); auth}
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
        case Some(("-mark", None, None)) => init //FIX!!
        case Some(("-removeAll", None, None)) => {uc.remove;controller(uc)}
        case Some(("-remove", Some(id), None)) => {uc.removeById(id.toString.toInt);controller(uc)} ///FIX!!!
        case Some(("-removeDone", None, None)) => {uc.removeByStatus(0);controller(uc)}
        case Some(("-add", Some(text), None)) => {uc.add(text.toString);controller(uc)} ///FIX!!!!
        case Some(("-prev", None, None)) => {auth}
        case Some(("-exit", None, None)) => {println("GoodBye!");()} ///ADD EXIT COMMAND!!
        case Some(_) => {println("In control mode \"Unknown command\" exception!"); controller(uc)}
        case None => {println("In control mode \"None Imput\" exception!"); controller(uc)}
      }
    }
  }

}
