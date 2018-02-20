import shapeless._
import shapeless.ops.hlist._

//more operations on HList
val hlist = Seq.empty[Int] :: "bla" :: true :: "ble" :: 123 :: HNil

hlist.filter[String]
hlist.reverse
hlist.tupled
hlist.length.toInt

trait LastOfType[L, T] {
  type Out = T
  def apply(hlist: L): Out
}

object LastOfType {
  type Aux[L, T] = LastOfType[L, T]
}

implicit def hlistFilterLastType[L <: HList, T, F <: HList](implicit
                                                partition: Partition.Aux[L, T, F, _ <: HList],
                                                last: Last.Aux[F, T]
                                               ): LastOfType.Aux[L, T] = (hlist: L) => last(partition(hlist)._1)

implicit class CustomHListOps[L <: HList](hlist: L) {
  def lastOfType[T](implicit l: LastOfType.Aux[L, T]): T = l(hlist)
}



hlist.lastOfType[Int]
hlist.lastOfType[String]

/*
  3. define LastOfType type class by combining:
      - Partition (filter)
      - Last

  goal:
    hlist.lastOfType[String] == ble
    hlist.lastOfType[Boolean] == true

  type classes used:
    Last.Aux[L, O]
      L: input hlist
      O: output (type of the last element)
    Partition.Aux[L, T, Filtered, Rest]
      L: input hlist
      T: type to filter on
      Filtered: output hlist of elements matching the filter
      Rest: output hlist of elements not matching

  steps:
    a. define the type class
    b. define the companion, with Aux
    c. define the implicit def (skeleton with ???s)
    d. include the lemmas: Partition & Last
        use their Aux (with ???s as types params)
    e. place the type params properly (so they restrict between the lemmas)
        implement the proof
    f. create the syntax sugar
        show that it works
 */
