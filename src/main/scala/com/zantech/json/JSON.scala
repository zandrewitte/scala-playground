package com.zantech.json

import JSONWriter.Syntax._

object JSON extends App {

  sealed abstract class JsValue
  sealed abstract class JsRootValue extends JsValue {
    type T
    def ++(t: T): JsRootValue
  }
  case object JNil extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: JsValue): JsRootValue = JNil
  }
  case class JString(string: String) extends JsValue
  case class JInt(number: BigInt) extends JsValue
  case class JObject(values: JPair*) extends JsRootValue {
    override type T = JPair
    override def ++(jPair: JPair): JsRootValue = {
      val allValues = values :+ jPair
      JObject(allValues:_*)
    }

    def ::(jObject: JObject): JsRootValue = {
      val allValues =  jObject.values ++: values
      JObject(allValues:_*)
    }
  }
  case class JList(values: JsValue*) extends JsRootValue {
    override type T = JsValue
    override def ++(jsValue: JsValue): JsRootValue = {
      val allValues = values :+ jsValue
      JList(allValues:_*)
    }
  }

  type JPair = (String, JsValue)

  val jsonString =
    """
      |{
      |  "graph": {
      |    "components": [
      |      {
      |        "id": "app",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |          "RAM usage": "no_data"
      |        },
      |        "depends_on": [
      |          "db"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      },
      |      {
      |        "id": "db",
      |        "own_state": "no_data",
      |        "derived_state": "no_data",
      |        "check_states": {
      |          "CPU load": "no_data",
      |
      |          "RAM usage": "no_data"
      |        },
      |        "dependency_of": [
      |          "app"
      |        ]
      |      }
      |    ]
      |  }
      |}
    """.stripMargin

  val jsonString2 =
    """
      |{
      |  "events": [
      |    {
      |      "timestamp": "1",
      |      "component": "db",
      |      "check_state": "CPU load",
      |      "state": "warning"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    },
      |    {
      |      "timestamp": "2",
      |      "component": "app",
      |      "check_state": "CPU load",
      |      "state": "clear"
      |    }
      |  ]
      |}
    """.stripMargin

  println(s"${JSONReader.read(jsonString).write}")
  println(s"${JSONReader.read(jsonString2).write}")

  val a = JObject(
    ("graph",
      JObject(("components", JList(
        JObject(
          ("id", JString("app")),
          ("own_state", JString("no_data")),
          ("derived_state", JString("no_data")),
          ("check_states", JObject(
            ("CPU load", JString("no_data")),
            ("RAM usage", JString("no_data"))
          )),
          ("depends_on", JList(JString("db")))
        ),
        JObject(
          ("id", JString("db")),
          ("own_state", JString("no_data")),
          ("derived_state", JString("no_data")),
          ("check_states", JObject(
            ("CPU load", JString("no_data")),
            ("RAM usage", JString("no_data"))
          )),
          ("dependency_of", JList(JString("app")))
        )
      )))
    )
  )

//  println(a.write)

}
