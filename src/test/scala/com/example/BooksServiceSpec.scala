package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import StatusCodes._

class BooksServiceSpec extends Specification with Specs2RouteTest with BooksService with InMemoryBooksRepo {
  def actorRefFactory = system

  sequential
  
  "BooksService" should {

    "return a list of books for GET requests to the root path" in {
      Get("/api/books?serialNumberFrom=0&serialNumberTo=1000") ~> booksRoute ~> check {
        responseAs[List[Book]] must equalTo(books.toList)
      }
    }

    "filter a list of books for GET requests to the root path by serial number range" in {
      Get("/api/books?serialNumberFrom=0&serialNumberTo=102") ~> booksRoute ~> check {
        responseAs[List[Book]] must equalTo(books.filter(b => b.id != "102" && b.id != "103").toList)
      }
    }

    "insert a book for POST requests to the root path" in {
      val newBook = Book("1001", 1001, "New book", "New author", 1001)
      Post("/api/books", newBook) ~> booksRoute ~> check {
        responseAs[BookOperationResult] must equalTo(BookOperationResult(true))
      }
      var result = Get("/api/books?serialNumberFrom=0&serialNumberTo=2000") ~> booksRoute ~> check {
        responseAs[List[Book]].toSet must equalTo(books + newBook)
      }
      setInitialState()
      result
    }

    "update a book for PUT requests by id" in {
      val newBook = Book("102", 102, "New book", "New author", 102)
      Post("/api/books", newBook) ~> booksRoute ~> check {
        responseAs[BookOperationResult] must equalTo(BookOperationResult(true))
      }
      var result = Get("/api/books?serialNumberFrom=0&serialNumberTo=2000") ~> booksRoute ~> check {
        responseAs[List[Book]].toSet must equalTo(books map {
          case b if b.id == newBook.id => newBook
          case b => b
        })
      }
      setInitialState()
      result
    }

    "delete a book for DELETE requests by id" in {
      Delete("/api/books/101") ~> booksRoute ~> check {
        responseAs[BookOperationResult] must equalTo(BookOperationResult(true))
      }
      val result = Get("/api/books?serialNumberFrom=0&serialNumberTo=1000") ~> booksRoute ~> check {
        responseAs[List[Book]] must equalTo(books.filter(b => b.id != "101").toList)
      }
      setInitialState()
      result
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> booksRoute ~> check {
        handled must beFalse
      }
    }

    "return 404 error for PUT requests to the root path" in {
      Put() ~> sealRoute(booksRoute) ~> check {
        status === NotFound
      }
    }
  }
}
