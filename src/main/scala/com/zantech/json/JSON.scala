package com.zantech.json

object JSON {

  sealed abstract class JsValue
  sealed abstract class JsRootValue extends JsValue {
    type T
    def ++(t: T): JsRootValue
  }
  case object JNil extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: T): JsRootValue = JNil
  }
  case class JString(string: String) extends JsValue
  case class JInt(number: Int) extends JsValue
  case class JDouble(number: Double) extends JsValue
  case class JObject(values: JPair*) extends JsRootValue {
    override type T = JPair
    override def ++(jPair: JPair): JObject = {
      val allValues = values :+ jPair
      JObject(allValues:_*)
    }

    def ::(jObject: JObject): JObject = {
      val allValues =  jObject.values ++: values
      JObject(allValues:_*)
    }
  }
  case class JList(values: JsValue*) extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: JsValue): JList = {
      val allValues = values :+ jsValue
      JList(allValues:_*)
    }
  }

  type JPair = (String, JsValue)

}