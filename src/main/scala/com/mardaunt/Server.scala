package com.mardaunt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.mardaunt.base.Base.UserTask
import com.mardaunt.base.Base
import com.mardaunt.utils.Receive
// spray (JSON marshalling)
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
// cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object Server extends App{

    /** Конфигурация базы и подготовка доступа к таблицам */
  val outgoingTable =     Base.getOutgoingTable
  val incomingTable =     Base.getIncomingTable
  val queue =             Base.getQueue
  val receiveMap =        Base.getReceiveMap

  outgoingTable.start
  incomingTable.start
  //outgoingTable.clearTable
  //incomingTable.clearTable
  outgoingTable.printTable
  incomingTable.printTable

  implicit val system =           ActorSystem(Behaviors.empty, "service-telesupp")
    // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  final case class Empty(status: String)
  final case class ReceiveMessage(message: String)

  implicit val itemFormat3 = jsonFormat1(Empty)
  implicit val itemFormat4 = jsonFormat1(ReceiveMessage)

   /** //////////////////////////////////////////////////////////////////////////////////////////////////////// */
    val route = {
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Я родился</h1>"))
        }
      }
    }

      /** Маршрут который принимает и добавляет сообщение в очередь для отправки */
    val addMessage = post {
      path("add_message") {
        entity(as[UserTask]) {
          message => {
            queue.enqueue(message)
            complete("ok")
          }
        }
      }
    }

      /** Маршрут отдаёт исполнителю сообщение из очереди. И добавляет её в базу */
    val getQueue = get {
      path("get_message"){
        if (queue.nonEmpty) {
          val message = queue.dequeue()
          outgoingTable.addMassage(message)
          complete(message)
        }
        else complete(Empty("empty"))
      }
    }

      /**
       * Маршрут для исполнителя, который принимает входящие из уведомлений,
       * и добавляет в Map для входящих.
       */
    val receiveMessage = post {
      path("receive_message"){
        entity(as[ReceiveMessage]) {
          message => {
            Receive.addMessage(message.message)
            complete("ok")
          }
        }
      }
    }

      /** Маршрут для клиента, который будет спрашивать, есть ли для него новые входящие сообщения */
    val clientTukTuk = get {
      path("tuk_tuk" / """\d+""".r){ //Регекс будет извлекать userId
        userId =>
          if (Receive.checkIncoming(userId)) {
                val message = Receive.getIncoming(userId)
                incomingTable.addMassage(userId, message.phone, message.message)
                      complete(message)
          } else      complete("Nothing")
      }
    }

  val test2 = post {
    path("test2"){
      parameters("phone", "message"){
        (phone, message) => {
          println(s"Пришли данные $phone $message")
          queue.enqueue(UserTask(phone, message, "WhatsApp", "Юзернейм", "ok"))
          complete("ok")
        }
      }
    }
  }

    val routes = cors() {
      concat(route, addMessage, getQueue, receiveMessage, clientTukTuk, test2)
    }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    println(s"Server online at http://localhost:8080/")

}