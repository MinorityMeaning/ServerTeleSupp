package com.mardaunt.utils

import com.mardaunt.base.Base
import com.mardaunt.base.Base.IncomingTask
import scala.collection.mutable.Queue

object Receive {
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private val pattern = """([\S\s]+)\n\n\n([\S\s]+)\n\n""".r

  def getPhoneAndMessage(receive: String): List[String] = receive match {
      // Примите строку
      // Верните список из номера и сообщения.
      case pattern(phone, text) => List(phone.replaceAll("[()\\s|-]+", ""), text)
      case _ => List("emptyPhone", "emptyMessage")
  }
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private val outgoingTable = Base.getOutgoingTable
  private val receiveMap =    Base.getReceiveMap

  def addMessage(string: String):Unit ={
    val list =         getPhoneAndMessage(string)
    val user =         outgoingTable.getUserByPhone(list(0))
    val incomingTask = IncomingTask(list(0), list(1), "WhatsApp")

    if(!receiveMap.contains(user)) {
      val queue = Queue[IncomingTask]()
      queue.enqueue(incomingTask)
      receiveMap.put(user, queue)
    }
    else {
      receiveMap(user).enqueue(incomingTask)
    }
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  def checkIncoming(user: String): Boolean = if (receiveMap.contains(user))
                                                receiveMap(user).nonEmpty
                                             else false
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  def getIncoming(user: String):IncomingTask = receiveMap(user).dequeue()
  //////////////////////////////////////////////////////////////////////////////////////////////////
}
