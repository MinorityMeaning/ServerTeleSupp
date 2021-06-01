package com.mardaunt

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object DBobject {

  val db = Database.forConfig("mydb")

  // Запись в таблице
  final case class Message(
             id: Long = 0L,
             phone: String,
             message: String,
             service: String,
             user: String,
             status:Boolean,
             )

  // схема таблицы
  final class MessagesTable(tag: Tag) extends Table[Message](tag, "messages"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def phone = column[String]("phone")
    def message = column[String]("message")
    def service = column[String]("service")
    def user = column[String]("user")
    def status = column[Boolean]("status")

    override def * = (id, phone, message, service, user, status).mapTo[Message] // two-way map
  }

  //Базовый запрос для создания запросов // select * from
  lazy val messages = TableQuery[MessagesTable]

  def start:Unit = db.run(messages.schema.create)
  def addRow1:Unit = db.run(messages += Message(0,"79943453222", "Привет, как дела?", "WhatsApp", "User01", status = false))
  //db.run(messages += Message("79943423423", "Нормас так то", "WhatsApp", "User04", status = true))

  def printTable:Unit = db.run(messages.result).foreach(x =>{
                          x.foreach(println)
                        })

  //db.run(messages.filter(_.status == true).result).foreach(x => {
  //  x.foreach(println)
  //})

}
