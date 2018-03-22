package com.zantech.json

import com.zantech.json.JSON._
import com.zantech.json.JSONUtils._

import scala.language._

object Syntax {

  implicit class JObjectOps(jsRootValue: JsRootValue) {
    def \(key: String): JsValue = jsRootValue match {
      case jObject: JObject => getKeyRec(jObject.values.toList, key)
      case _ => JNil
    }

    def \\(key: String): List[JsValue] = jsRootValue match {
      case jObject: JObject => getKeyRecWithAcc(jObject.values.toList, key, List())
      case _ => List()
    }
  }

  implicit class JsRootValueOps(jsRootValue: JsRootValue) {
    def write: String = jsRootValue match {
      case jObject: JObject => JSONWriter.write(jObject)
      case jList: JList => JSONWriter.writeList(jList)
      case _ => ""
    }
  }

  implicit class JsonObjectOps(jObject: JObject) {
    def write: String = JSONWriter.write(jObject)
  }

  implicit class JsonListOps(jList: JList) {
    def write: String = JSONWriter.writeList(jList)
  }

  implicit def JStringToString(jString: JString): String = jString.string
  implicit def JIntToInt(jInt: JInt): Int = jInt.number

}
