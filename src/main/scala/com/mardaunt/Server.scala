package com.mardaunt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.mardaunt.base.BaseOutgoing
// spray (JSON marshalling)
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
// cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.collection.mutable.Queue

object Server extends App{

  //Подключаемся к базе
  val dataBase = BaseOutgoing
  dataBase.start
  dataBase.printTable
  //dataBase.addMassage(UserData("79943453222", "Собрали все детали в заказ", "WhatsApp", "Biznesman"))

  println(dataBase.getUserByPhone("79943453222"))
  implicit val system = ActorSystem(Behaviors.empty, "service-telesupp")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  final case class UserData(phone: String, message: String, service: String, user: String)
  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat4(UserData)
  val queue: Queue[UserData] = Queue()

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
        entity(as[UserData]) {
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
        val message = queue.dequeue()
        complete(message)
      }
    }

    // Тест
    val test = post {
      path("test"){
        entity(as[UserData]) {
          message => {
            queue.enqueue(message)
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
          queue.enqueue(UserData(phone, message, "WhatsApp", "Юзернейм"))
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
      concat(route, addMessage, getQueue, test, test2)
    }


    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    println(s"Server online at http://localhost:8080/")

}

