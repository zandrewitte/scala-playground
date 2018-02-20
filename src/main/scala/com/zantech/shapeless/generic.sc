import shapeless._

/*
  1. convert Cat -> HList -> Book
 */
case class Cat(name: String, livesLeft: Int, female: Boolean)
case class Book(title: String, pages: Int, hardcover: Boolean)

val c = Cat("Cat", 7, female = true)
val genC = Generic[Cat]
val catHList = genC.to(c)

val genB = Generic[Book]
val catBook = genB.from(catHList)