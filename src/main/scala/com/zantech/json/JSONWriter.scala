package json

import JSON._

object JSONWriter {

  def write(jObject: JObject, indent: Int = 0): String = {
    s"""${writeWithIndent("{", indent)}
       |${writeObjRec(jObject, indent + 2)}
       |${writeWithIndent("}", indent)}""".stripMargin
  }

  def writeList(jList: JList, indent: Int = 0): String = {
    s"""${writeWithIndent("[", indent)}
       |${writeListRec(jList, indent + 2)}
       |${writeWithIndent("]", indent)}""".stripMargin
  }

  private def writeWithIndent(s: String, indent: Int): String = {
    if(indent > 0)
      s"${(1 to indent).map(_ => " ").mkString}$s"
    else s
  }

  private def writeObjRec(obj: JObject, indent: Int): String = {
    obj.values.map {
      case (key, JString(stringVal)) =>
        writeWithIndent(s""""$key": "$stringVal"""", indent)
      case (key, JInt(intVal)) =>
        writeWithIndent(s""""$key": $intVal""", indent)
      case (key, jObject: JObject) =>
        s"""${writeWithIndent(s""""$key": {""", indent)}
           |${writeObjRec(jObject, indent + 2)}
           |${writeWithIndent("}", indent)}""".stripMargin
      case (key, jList: JList) =>
        s"""${writeWithIndent(s""""$key": [""", indent)}
           |${writeListRec(jList, indent + 2)}
           |${writeWithIndent("]", indent)}""".stripMargin
      case _ => ""
    }.mkString(",\n")
  }

  private def writeListRec(list: JList, indent: Int): String = {
    list.values.map {
      case JString(stringVal) => writeWithIndent(s""""$stringVal"""", indent)
      case JInt(intVal) => writeWithIndent(s"$intVal", indent)
      case JDouble(doubleVal) => writeWithIndent(s"$doubleVal", indent)
      case jObject: JObject => write(jObject, indent)
      case jList: JList => writeListRec(jList, indent)
      case _ => ""
    }.mkString(",\n")
  }

}
