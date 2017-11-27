package com.example

import scala.concurrent.Future

trait InMemoryBooksRepo extends BooksRepo {
  var books: Set[Book] = _

  def setInitialState(): Unit = {
    books = Set(
      Book("1", 1, "Good Book 1", "Good Author 1", 100),
      Book("2", 2, "Good Book 2", "Good Author 2", 101),
      Book("3", 3, "Good Book 3", "Good Author 3", 102),
      Book("4", 4, "Good Book 4", "Good Author 4", 103))
  }

  setInitialState()

  def insertBook(book: Book): Future[BookOperationResult] = {
    books += book
    Future.successful(BookOperationResult(true))
  }

  def updateBook(book: Book): Future[BookOperationResult] = {
    books = books map {
      case b if b.id == book.id => book
      case b => b
    }
    Future.successful(BookOperationResult(true))
  }

  def listBooks(serialNumberFrom: Int, serialNumberTo: Int): Future[Seq[Book]] = {
    val filtered = books filter { b: Book =>
      b.serialNumber >= serialNumberFrom && b.serialNumber < serialNumberTo
    }
    Future.successful(filtered.toList)
  }

  def deleteBook(id: String): Future[BookOperationResult] = {
    books = books filter { b =>
      b.id != id
    }
    Future.successful(BookOperationResult(true))
  }
}

