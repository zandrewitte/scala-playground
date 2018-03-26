package json

import JSON._
import JSONUtils.{getKeyRec, getKeyRecWithAcc}
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
    def getAsStrings: Vector[String] = jList.values.map{ case JString(string) => string }.toVector
    def getAsInt: Vector[Int] = jList.values.map{ case JInt(int) => int }.toVector
  }

  implicit def JStringToString(jString: JString): String = jString.string
  implicit def StringToJString(string: String): JString = JString(string)
  implicit def JIntToInt(jInt: JInt): Int = jInt.number
  implicit def IntToJInt(int: Int): JInt = JInt(int)
  implicit def JDoubleToDouble(jDouble: JDouble): Double = jDouble.number
  implicit def DoubleToJDouble(double: Double): JDouble = JDouble(double)

}
