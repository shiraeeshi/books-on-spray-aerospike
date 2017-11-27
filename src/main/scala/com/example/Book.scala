package com.example

import spray.json._
import DefaultJsonProtocol._

import io.tabmo.aerospike.data._
import io.tabmo.aerospike.data.{ Bin => TabmoBin }
import com.aerospike.client.Bin

object Book {

  implicit val bookFormat = jsonFormat5(Book.apply)

  /*
  def apply(key: AerospikeKey[String], record: AerospikeRecord): Book = key.userKey match {
    case Some(userKeyValue) =>
      apply(userKeyValue,
        record.getLong("serialNumber").toInt,
        record.getString("title"),
        record.getString("author"),
        record.getLong("pageCount").toInt)
    case None =>
      throw new Error(s"empty userKey in AerospikeKey $key, record: $record")
  }
  */
  def apply(key: AerospikeKey[String], record: AerospikeRecord): Book = 
    apply(key.inner.userKey.toString,
      record.getLong("serialNumber").toInt,
      record.getString("title"),
      record.getString("author"),
      record.getLong("pageCount").toInt)

}

case class Book(id: String, serialNumber: Int, title: String, author: String, pageCount: Int) {
  def asBins: Seq[Bin] = List(
    TabmoBin("serialNumber", this.serialNumber.toLong),
    TabmoBin("title", this.title),
    TabmoBin("author", this.author),
    TabmoBin("pageCount", this.pageCount.toLong))
}
