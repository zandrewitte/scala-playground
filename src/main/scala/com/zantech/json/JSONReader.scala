package com.zantech.json

import com.zantech.json.JSON._

object JSONReader {

  def read(string: String): JsRootValue = {

    val KeyWithValuePattern = """^\"([\w\s\d]+)\"[\:\s?]+(\"?[\w\s\d]+\"?)(.*)""".r
    val KeyWithObjectPattern = """^\"([\w\s\d]+)\"[\:\s?]+(\{[\w\s\d\"\,\:\{\}\[\]]+\})(.*)""".r
    val KeyWithListPattern = """^\"([\w\s\d]+)\"[\:\s?]+(\[.+\])(.*)""".r
    val ListObjectsPattern = """^\[(\{.+\})\,?\]$""".r
    val ListValuesPattern = """([\"?\w\s\d\"?]+),?\s?""".r

//    @tailrec
    def readStringRec(string: String, acc: JsRootValue): JsRootValue = {
//      println(s"string: $string")
//      println(s"acc: $acc")
      string match {
        case ListObjectsPattern(listObjects) =>
          listObjects
            .split("""\},\s?\{""")
            .map(objectString => readStringRec(objectString.replaceAll("""^\{|\}$""", ""), JObject()))
            .toList
            .fold(JList()){
              case (jList: JList, jObject: JObject) => jList ++ jObject
              case (jList: JList, _) => jList ++ JObject()
              case _ => JList()
            }

        case list if list.startsWith("[") && list.endsWith("]") =>
          val values = ListValuesPattern.findAllMatchIn(list).map(matched => processValue(matched.group(1))).toSeq
          JList(values:_*)

        case KeyWithValuePattern(key, value, rest) =>
          val newAcc = acc match {
            case jObject: JObject => jObject ++ (key, processValue(value))
            case _ => JNil
          }

          readStringRec(rest, newAcc)

        case KeyWithObjectPattern(key, obj, rest) =>
          val innerObject = readStringRec(obj.tail.dropRight(1), JObject())

          val newAcc = acc match {
            case jObject: JObject =>
              jObject ++ (key, innerObject)
            case jList: JList =>
              jList ++ innerObject
            case _ => innerObject
          }

          readStringRec(rest, newAcc)
        case KeyWithListPattern(key, list, rest) =>
          val innerList = readStringRec(list, JList())

          val newAcc = acc match {
            case jObject: JObject =>
              jObject ++ (key, innerList)
            case jList: JList =>
              jList ++ innerList
            case _ => innerList
          }

          readStringRec(rest, newAcc)
        case next if next.startsWith(",") => readStringRec(next.tail, acc)
        case done if done.isEmpty => acc
        case _ => JNil
      }
    }

    val jsonString = string.replaceAll("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)", "")

    (
      for {
        head <- jsonString.headOption
        tail <- jsonString.lastOption
      } yield (head, tail) match {
        case ('{', '}') => readStringRec(jsonString.tail.dropRight(1), JObject())
        case ('[', ']') => readStringRec(jsonString.tail.dropRight(1), JList())
        case _ => JNil
      }
    ).getOrElse(JNil)

  }

  private val StringPattern = """^\"([\w\d?\s?]*)\"""".r
  private val NumberPattern = """([\d]*)""".r

  private def processValue(value: String): JsValue = {
    value match {
      case StringPattern(stringVal) => JString(stringVal)
      case NumberPattern(numberVal) => JInt(numberVal.toInt)
      case _ => JNil
    }
  }

}
