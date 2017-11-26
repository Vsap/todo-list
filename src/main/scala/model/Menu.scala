package model


import repository._
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.concurrent.Future


case class Menu(login: String, password: String){
  def getTasks = ()
  def getByStatus(status: Int) = TaskTable.table.filter(_.userId === login).filter(_.status === status).result
  def add(text: String) = TaskTable.table returning TaskTable.table += Task( login, text, 1)
  def removeById =()
  def removeByStatus = ()
}