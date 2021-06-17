package com.mardaunt.base

import com.mardaunt.base.Base.UserTask
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class BaseOutgoing(var database: Database) {

  private val db = database

    /** Запись в таблице */
  final case class Message(
                            id: Long = 0L,
                            phone: String,
                            message: String,
                            service: String,
                            user: String,
                            status: Boolean,
                          )

  /** схема таблицы */
  final class OutgoingTable(tag: Tag) extends Table[Message](tag, "outgoing") {
    def id =      column[Long]("id", O.PrimaryKey, O.AutoInc)
    def phone =   column[String]("phone")
    def message = column[String]("message")
    def service = column[String]("service")
    def user =    column[String]("user")
    def status =  column[Boolean]("status")

    override def * = (id, phone, message, service, user, status).mapTo[Message] // two-way map
  }

  /** Базовый запрос для создания запросов // select * from */
  lazy val outgoing = TableQuery[OutgoingTable]
  def start: Unit =   db.run(outgoing.schema.create)

    //Непотребство
  def printTable: Unit = db.run(outgoing.result).foreach(x => {x.foreach(println)})

    /** Добавим в базу переданное исполнителю сообщение */
  def addMassage(message: UserTask): Unit =
    db.run(outgoing += Message(0, message.phone, message.message, message.service, message.user, status = false))

    /** Возвращает имя отправителя из последнего отправленного сообщения адресату phone */
  def getUserByPhone(phone: String):String = {
      val query = outgoing.filter(_.phone === phone)
                          .sortBy(_.id.desc)
                          .map(_.user)
                          .result.headOption
      val result = Await.result(db.run(query), Duration.Inf)
      if (result.value.isEmpty) return "None"
      result.value.get
  }

  def clearTable: Unit = db.run(outgoing.delete)

/*
  NoSuchElementException
  val q3 = for {
    c <- messages if c.status === false

  } yield (c)
///////////////////////////////////////////////////////
//////////////////////////////////////////////////////
*/
}
