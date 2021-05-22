package com.mardaunt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.io.StdIn
// spray (JSON marshalling)
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
// cors
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.collection.mutable.Queue

object Server extends App{

  implicit val system = ActorSystem(Behaviors.empty, "service-telesupp")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  final case class User(id: Long, name: String, email: String)
  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat3(User)

  final case class Message(phone: String, message: String, service: String)
  implicit val item = jsonFormat3(Message)
  val queue: Queue[Message] = Queue()

    val route = cors() {
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Привет ёпта</h1>"))
        }
      }
    }

    val addMessage = post {
      path("add_message") {
        entity(as[Message]) {
          message => {
            queue.enqueue(message)
            complete("ok")
          }
        }
      }
    }

    // Тест
    val test = post {
      path("test"){
        entity(as[Message]) {
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
          queue.enqueue(Message(phone, message, "WhatsApp"))
          complete("ok")
        }
      }
    }
  }

    val getQueue = get {
      path("get_message"){
        complete(queue.dequeue())
      }
    }

    val getUser = get {
      path("user" / LongNumber) {
        userId => complete(User(userId, "Андрей", "test@test.com"))
      }
    }

    val createUser = post {
      path("user"){
        entity(as[User]) {
          user => complete(user)
        }
      }
    }

    val routes = cors() {
      concat(route, getUser, createUser, addMessage, getQueue, test, test2)
    }


    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

}

