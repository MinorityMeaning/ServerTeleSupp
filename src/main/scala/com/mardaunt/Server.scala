package com.mardaunt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.mardaunt.base.{Base, BaseIncoming, BaseOutgoing}

import scala.collection.mutable
// spray (JSON marshalling)
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
// cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.collection.mutable.Queue

object Server extends App{

  //Конфигурация базы и подготовка доступа к таблицам
  val base = Base
  val outgoingTable = new BaseOutgoing(base.getDatabase)
  val incomingTable = new BaseIncoming(base.getDatabase)
  outgoingTable.start
  incomingTable.start
  outgoingTable.printTable

  println(outgoingTable.getUserByPhone("79943453222"))
  implicit val system = ActorSystem(Behaviors.empty, "service-telesupp")
    // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  final case class UserTask(phone: String, message: String, service: String, user: String, status: String)
  final case class IncomingTask(phone: String, message: String, service: String)
  final case class Empty(status: String)
  final case class Receive(notification_text: String)
    // formats for unmarshalling and marshalling
  implicit val itemFormat1 = jsonFormat5(UserTask)
  implicit val itemFormat2 = jsonFormat3(IncomingTask)
  implicit val itemFormat3 = jsonFormat1(Empty)
  implicit val itemFormat4 = jsonFormat1(Receive)

  val queue: mutable.Queue[UserTask] = mutable.Queue()

    val route = {
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Привет ёпта</h1>"))
        }
      }
    }
      //Маршрут, который принимает и добавляет сообщение в очередь для отправки.
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
      //Маршрут отдаёт исполнителю сообщение из очереди. И добавляет её в базу.
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

    // Тест
    val receiveMessage = post {
      path("receive_message"){
        entity(as[Receive]) {
          message => {
            println(message)
            complete("ok")
          }
        }
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

    /*
    val getUser = get {
      path("user" / LongNumber) {
        userId => complete(User(userId, "Андрей", "test@test.com"))
      }
    }

     */

    val routes = cors() {
      concat(route, addMessage, getQueue, receiveMessage, test2)
    }


    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    println(s"Server online at http://localhost:8080/")

}

