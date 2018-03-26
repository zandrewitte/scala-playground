package json

import JSON._

import scala.annotation.tailrec

private[json] object JSONUtils {

  @tailrec
  def getKeyRec(pairs: List[JPair], key: String): JsValue = pairs match {
    case Nil => JNil
    case (`key`, jList: JList)::_ => jList
    case (`key`, jObject: JObject)::_ => jObject
    case (`key`, jsValue: JsValue)::_ => jsValue
    case pair::tail =>
      val restPairs = (pair match {
        case (_, jList: JList) => jList.values.flatMap {
          case jObject: JObject => jObject.values
          case _ => Seq()
        }
        case (_, jObject: JObject) => jObject.values
        case _ => Seq()
      }).toList
      getKeyRec(restPairs ::: tail, key)

  }

  @tailrec
  def getKeyRecWithAcc(pairs: List[JPair], key: String, acc: List[JsValue]): List[JsValue] = pairs match {
    case Nil => acc
    case (`key`, jList: JList)::tail => getKeyRecWithAcc(tail, key, jList :: acc)
    case (`key`, jObject: JObject)::tail => getKeyRecWithAcc(tail, key, jObject :: acc)
    case (`key`, jsValue: JsValue)::tail => getKeyRecWithAcc(tail, key, jsValue :: acc)
    case pair::tail =>
      val restPairs = (pair match {
        case (_, jList: JList) => jList.values.flatMap {
          case jObject: JObject => jObject.values
          case _ => Seq()
        }
        case (_, jObject: JObject) => jObject.values
        case _ => Seq()
      }).toList
      getKeyRecWithAcc(restPairs ::: tail, key, acc)
  }

}
