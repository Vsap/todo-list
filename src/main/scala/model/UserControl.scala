package model

import repository._
import scala.io._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Await, Future}
import implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

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
    def signUp(login: String, password: String):Option[UserControl] = Await.result(userRepository.ifMatches(User(login, password)), Duration.Inf) == 1 match {
      case false => {userRepository.insert(User(login, password));Some(UserControl(login, password))}
      case _ => {println("This username already exists");None}  //fix exception to program pointer!!!!!!!!
    }
    //def ifExists(login: String): Boolean = userRepository.getByLogin(login) match {case Some(_) => true; case _ => false}
    def signIn(login: String, password: String): Option[UserControl] =
      Await.result(userRepository.ifMatches(User(login, password)) ,Duration.Inf) == 1 match{
        case true => Some(UserControl(login,password))
        case _ => None
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
    def controller(Ouc: Option[UserControl]):Unit = Ouc match{
      case None => {println("Incorrect user data!");auth}
      case Some(uc) => {
        println("U are logged in as "+ uc.login + ". Welcome!")
        println("Control mode: ")
        println("Input the commands to manipulate the tasks")
        InputParser.input match {
          case Some(("-getAll", Some(""), Some(""))) => {Await.result(uc.getTasks, Duration.Inf).map(p => println(p));controller(Some(uc))}
          case Some(("-todo", Some(""), Some(""))) => {Await.result(uc.getByStatus(1), Duration.Inf).map(p => println(p));controller(Some(uc))}
          case Some(("-done", Some(""), Some(""))) => {Await.result(uc.getByStatus(0),Duration.Inf).map(println(_));controller(Some(uc))}
          case Some(("-mark", Some(""), Some(""))) => init //FIX!!
          case Some(("-removeAll", Some(""), Some(""))) => {uc.remove;controller(Some(uc))}
          case Some(("-remove", Some(id), Some(""))) => {uc.removeById(id.toString.toInt);controller(Some(uc))}
          case Some(("-removeDone", Some(""), Some(""))) => {uc.removeByStatus(0);controller(Some(uc))}
          case Some(("-add", Some(""), Some(""))) => {uc.add(StdIn.readLine());controller(Some(uc))}
          case Some(("-prev", Some(""), Some(""))) => auth
          case Some(("-exit", Some(""), Some(""))) => {println("GoodBye!");()}
          case Some(_) => {println("In control mode \"Unknown command\" exception!"); controller(Some(uc))}
          case None => {println("In control mode \"None Input\" exception!"); controller(Some(uc))}
        }
      }
    }
  }

}
