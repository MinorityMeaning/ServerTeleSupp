package com.mardaunt.base

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

class BaseIncoming(var database: Database) {

  private val db = database

  final case class Message(
                            id: Long = 0L,
                            phone: String,
                            message: String,
                            service: String,
                            user: String,
                            status: Boolean,
                          )

    // схема таблицы
  final class IncomingTable(tag: Tag) extends Table[Message](tag, "incoming") {
    def id =      column[Long]("id", O.PrimaryKey, O.AutoInc)
    def phone =   column[String]("phone")
    def message = column[String]("message")
    def service = column[String]("service")
    def user =    column[String]("user")
    def status =  column[Boolean]("status")

    override def * = (id, phone, message, service, user, status).mapTo[Message] // two-way map
  }

    //Базовый запрос для создания запросов // select * from
  lazy val incoming = TableQuery[IncomingTable]
  def start: Unit   = db.run(incoming.schema.create)

    //Непотребство
  def printTable: Unit = db.run(incoming.result).foreach(x => {x.foreach(println)})

    //Добавим в базу переданное исполнителю сообщение
  def addMassage(message: List[String], outgoingTable: BaseOutgoing):Unit =
    db.run(incoming += Message(0, message(0), //phone
                                  message(1), //message
                                  "WhatsApp", //Заглушка!
                                  outgoingTable.getUserByPhone(message(0)),
                                  status = false))

}
