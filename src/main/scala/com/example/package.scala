package com

import spray.json._
import DefaultJsonProtocol._

import com.example._

package object example {
  implicit val BookOperationResultFormat = jsonFormat1(BookOperationResult.apply)
}
