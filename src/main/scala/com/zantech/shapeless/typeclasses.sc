import scala.language._
/*
  2.
    a.  create the type class EncodeCsv[A]
        that does:
          def encode(a: A): List[String]

    b.  create the sugar methods:
          summoner (apply)
          constructor (instance/pure)
          "syntax" (encode)

    c.  create basic instances for:
          Int
          String
          Boolean (make it return "yes" or "no")

    d.  prove that it works:
          encode(true) should return List("yes")
 */

trait EncodeCSV[A] {
  def encode(a: A): List[String]
}

object EncodeCSV {
  def apply[A](implicit ev: EncodeCSV[A]): EncodeCSV[A] = ev
  def pure[A](ev: A => String): EncodeCSV[A] = (a: A) => List(ev(a))
}

def encode[A](a: A)(implicit ev: EncodeCSV[A]): List[String] = ev.encode(a)

// Summoner
EncodeCSV[Int]
EncodeCSV[String]
EncodeCSV[Boolean]

//constructor
new EncodeCSV[Int] {
  override def encode(a: Int): List[String] = List(a.toString)
}
EncodeCSV.pure[Int](int => int.toString)
EncodeCSV.pure[String](identity)
EncodeCSV.pure[Boolean](b => b.toString)

//type class instances
implicit def intInstance: EncodeCSV[Int] = EncodeCSV.pure(_.toString)
implicit def stringInstance: EncodeCSV[String] = EncodeCSV.pure(identity)
implicit def booleanInstance: EncodeCSV[Boolean] = EncodeCSV.pure{b =>
  if(b) "yes" else "no"
}

//syntax
EncodeCSV[Int].encode(10)
EncodeCSV[String].encode("Some String")
EncodeCSV[Boolean].encode(false)

encode(10)
encode("string")
encode(true)