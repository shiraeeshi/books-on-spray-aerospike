package com.example

import scala.concurrent.Future

trait BooksRepo {
  def insertBook(book: Book): Future[BookOperationResult]
  def updateBook(book: Book): Future[BookOperationResult]
  def listBooks(serialNumberFrom: Int, serialNumberTo: Int): Future[Seq[Book]]
  def deleteBook(id: String): Future[BookOperationResult]

  def onInit(): Unit = {}
  def beforeShutdown(): Unit = {}
}
