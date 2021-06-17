package com.mardaunt.base

import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol._

import scala.collection.mutable.{Map, Queue}
import scala.concurrent.Future

object Base {
    /** PostgreSQL */
  private val db =            Database.forConfig("mydb")
  private val outgoingTable = new BaseOutgoing(getDatabase)
  private val incomingTable = new BaseIncoming(getDatabase)
    /** Queue for new outgoing message */
  final case class UserTask(phone: String, message: String, service: String, user: String, status: String)
  implicit val itemFormat1 =           jsonFormat5(UserTask)
  private val queue: Queue[UserTask] = Queue()
    /** Map for incoming message */
  final case class IncomingTask(phone: String, message: String, service: String)
  implicit val itemFormat2 = jsonFormat3(IncomingTask)
  private val receiveMap =   Map[String, Queue[IncomingTask]]()


  def getDatabase =      db
  def getQueue =         queue
  def getReceiveMap =    receiveMap
  def getOutgoingTable = outgoingTable
  def getIncomingTable = incomingTable
}
