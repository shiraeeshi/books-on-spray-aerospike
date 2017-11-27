package com.example

import scala.concurrent.Future

import com.typesafe.config.ConfigFactory

import com.aerospike.client.policy.WritePolicy
import com.aerospike.client.query.IndexType

import io.tabmo.aerospike.client._
import io.tabmo.aerospike.data._
import io.tabmo.aerospike.converter.key._

import io.tabmo.aerospike.client.ReactiveAerospikeClient

trait AerospikeBooksRepo extends BooksRepo {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val configFactory = ConfigFactory.load()

  val namespace = configFactory.getString("custom.aerospike.namespace")
  val setName = configFactory.getString("custom.aerospike.setName")
  val host = configFactory.getString("custom.aerospike.host")
  val port = configFactory.getInt("custom.aerospike.port")

  private val client = ReactiveAerospikeClient.connect(host, port) // TODO client.close()

  private def aerospikeKey(book: Book): AerospikeKey[String] = AerospikeKey(namespace, setName, book.id)

  override def onInit() {
    client.createIndex(namespace, setName, "serialNumber", IndexType.NUMERIC, Some("serialNumberIdx"))
  }

  override def beforeShutdown() {
    client.close()
  }
  
  def insertBook(book: Book): Future[BookOperationResult] = {
    val writePolicy = {
      val policy = new WritePolicy(client.asyncClient.asyncWritePolicyDefault) // clone default policy
      policy.expiration = 60 * 60 * 24 * 30 // 30 days
      policy.sendKey = true
      policy
    }
    client.put(aerospikeKey(book), book.asBins, Some(writePolicy)) map { result =>
      BookOperationResult(true)
    }
  }
  def updateBook(book: Book): Future[BookOperationResult] = {
    client.put(aerospikeKey(book), book.asBins) map { result => 
      BookOperationResult(true)
    }
  }
  def listBooks(serialNumberFrom: Int, serialNumberTo: Int): Future[Seq[Book]] = {
    val f = client.queryRange[String](
      namespace,
      setName,
      Seq("serialNumber", "title", "author", "pageCount"),
      "serialNumber",
      serialNumberFrom,
      serialNumberTo)
    f map { result =>
      result.toList map { case (key, record) =>
        Book(key, record)
      }
    }
  }
  def deleteBook(id: String): Future[BookOperationResult] = {
    client.delete(AerospikeKey(namespace, setName, id)) map { existed =>
      BookOperationResult(existed)
    }
  }
}
