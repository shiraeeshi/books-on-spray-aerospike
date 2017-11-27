package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.json._
import DefaultJsonProtocol._
import MediaTypes._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class BooksServiceActor extends Actor with AerospikeBooksRepo with BooksService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  override def preStart(): Unit = {
    onInit()
  }

  override def postStop(): Unit = {
    beforeShutdown()
  }

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(booksRoute)
}


// this trait defines our service behavior independently from the service actor
trait BooksService extends HttpService with DefaultJsonProtocol { this: BooksRepo =>
  import spray.httpx.SprayJsonSupport._

  import scala.concurrent.ExecutionContext.Implicits.global

  val booksRoute = pathPrefix("api" / "books") {
    path(Segment) { id =>
      delete {
        onSuccess(deleteBook(id)) { case result =>
          complete(result)
        }
      } ~
      put {
        entity(as[Book]) { book =>
          onSuccess(updateBook(book)) { case result =>
            complete(result)
          }
        }
      }
    } ~ 
    pathEnd {
      get {
        parameters('serialNumberFrom.as[Int], 'serialNumberTo.as[Int]) { (numberFrom, numberTo) =>
          onSuccess(listBooks(numberFrom, numberTo)) { case result =>
            complete(result)
          }
        }
      } ~
      post {
        entity(as[Book]) { book =>
          onSuccess(insertBook(book)) { case result =>
            complete(result)
          }
        }
      }
    }
  }
}
