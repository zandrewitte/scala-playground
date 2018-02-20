import shapeless._

/*
  4. derive EncodeCsv[A] from smaller parts, for any A

      String
         Int    ->    HList     ->        A
        (...)                 Generic

   we'll need:
      EncodeCsv[HList]
         EncodeCsv[HNil]
         EncodeCsv[String]
         EncodeCsv[Int]
         EncodeCsv[...]
      Generic[A]

   goal:
      encode(cat) == List(Gatarys, 7, yes)
      encode(person) == ...
      encode(aeroplane) == ...
      ...
 */
case class Cat(name: String, livesLeft: Int, female: Boolean)
trait EncodeCsv[A] {
  def encode(a: A): List[String]
}
object EncodeCsv {
  def apply[A](implicit e: EncodeCsv[A]): EncodeCsv[A] = e
  def instance[A](f: A => List[String]) = new EncodeCsv[A] {
    override def encode(a: A): List[String] = f(a)
  }
}
implicit val int: EncodeCsv[Int] = EncodeCsv.instance{
  v: Int => List(v.toString)
}
implicit val string: EncodeCsv[String] = EncodeCsv.instance{
  v: String => List(v)
}
implicit val bool: EncodeCsv[Boolean] = EncodeCsv.instance{
  v: Boolean => List(if(v) "yes" else "no")
}
implicit def hNilInstance: EncodeCsv[HNil] = EncodeCsv.instance {
  _: HNil => List()
}

implicit def hListInstance[H, T <: HList](implicit hEv: EncodeCsv[H],
                                          tEv: EncodeCsv[T]): EncodeCsv[H :: T] = {
  EncodeCsv.instance { value: ::[H, T] =>
    hEv.encode(value.head) ::: tEv.encode(value.tail)
  }
}

EncodeCsv[Int :: String :: HNil]
EncodeCsv[Int]
EncodeCsv[String :: HNil]
EncodeCsv[String]
EncodeCsv[HNil]

implicit def caseClassInstance[A, Gen <: HList](implicit
                                               gen: Generic.Aux[A, Gen],
                                                e: Lazy[EncodeCsv[Gen]]
                                               ): EncodeCsv[A] =
  EncodeCsv.instance { instanceA: A => e.value.encode(gen.to(instanceA)) }

def encode[A](a: A)(implicit e: EncodeCsv[A]): List[String] = e.encode(a)

encode(Cat("Gatarys", 7, female = true))