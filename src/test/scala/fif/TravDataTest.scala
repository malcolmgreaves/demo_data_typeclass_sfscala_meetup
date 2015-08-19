package fif

import org.scalatest.FunSuite

import scala.language.higherKinds
import scala.reflect.ClassTag

class TravDataTest extends FunSuite {

  // for infix syntax
  import DataOps.syntax._

  // Datatypeclass evidence for all kinds of Traversables
  implicit val t = TravData

  import t.Implicits._

  val data = Seq(1, 2, 3).toTraversable

  test("test map") {

      def addElementwise10[D[_]: Data](data: D[Int]): D[Int] =
        data.map(_ + 10)

      def addElementwise10_tc[D[_]](data: D[Int])(implicit ev: Data[D]): D[Int] =
        ev.map(data)(_ + 10)

    {
      val changed = addElementwise10(data)

      assert(changed != data)
      assert(changed == Seq(11, 12, 13))
    }

    {
      val changed = addElementwise10_tc(data)

      assert(changed != data)
      assert(changed == Seq(11, 12, 13))
    }
  }

  test("mapPartition") {

      def mapParition10[D[_]: Data](data: D[Int]): D[Int] =
        data.mapParition { elements => elements.map(_ + 10) }

    val changed = mapParition10(data)
    assert(changed != data)
    assert(changed == Seq(11, 12, 13))
  }

  test("foreach") {

      def testForeach[D[_]: Data](data: D[Int]): Unit =
        data.foreach(x => assert(x >= 1 && x <= 3))

    testForeach(data)
  }

  test("foreachPartition") {

      def testForeachPart[D[_]: Data](data: D[Int]): Unit =
        data.foreachPartition(_.foreach(x => assert(x >= 1 && x <= 3)))

    testForeachPart(data)
  }

  test("aggregate") {

      def aggregateTest[D[_]: Data](data: D[Int]): Int =
        data.aggregate(0)(_ + _, _ + _)

    assert(aggregateTest(data) == 6)
  }

  test("sortBy") {

      def reverseSort[D[_]: Data](data: D[Int]): D[Int] =
        data.sortBy(x => -x)

    assert(reverseSort(data) == Seq(3, 2, 1))
  }

  test("take") {

      def testTake[D[_]: Data](data: D[Int]): Boolean =
        data.take(1) == Seq(1) && data.take(2) == Seq(1, 2) && data.take(3) == Seq(1, 2, 3)

    assert(testTake(data))
  }

  test("toSeq") {

      def testToSeqIs123[D[_]: Data](data: D[Int]): Boolean =
        data.toSeq == Seq(1, 2, 3)

    assert(testToSeqIs123(data))
  }

  test("flatMap") {

      def testFlat[D[_]: Data](data: D[Int]): D[Int] =
        data.flatMap { number =>
          (0 until number).map(_ => number)
        }

    val result = testFlat(data)
    assert(result == Seq(1, 2, 2, 3, 3, 3))
  }

  test("flatten") {

      def flattenTest[D[_]: Data](data: D[Seq[Int]]): D[Int] =
        data.flatten

    val expanded = data.map(x => Seq(x))
    val flattened = flattenTest(expanded)
    assert(flattened == data)
  }

  test("groupBy") {

      def groupIt[D[_]: Data](data: D[Int]): D[(Boolean, Iterator[Int])] =
        data.groupBy { _ % 2 == 0 }

    val evenGroup = groupIt(data).toMap

    val evens = evenGroup(true).toSet
    assert(evens.size == 1)
    assert(evens == Set(2))

    val odds = evenGroup(false).toSet
    assert(odds.size == 2)
    assert(odds == Set(1, 3))
  }

  test("size") {

      def sizeIs3[D[_]: Data](data: D[Int]): Boolean =
        data.size == 3

    assert(sizeIs3(data))
  }

  test("reduce") {

      def foo[D[_]: Data](data: D[Int]): Int =
        data.reduce {
          case (a, b) => 1 + a + b
        }

    val result = foo(data)
    assert(result == 8)
  }

  test("sum") {
      def s[D[_]: Data](data: D[Int]): Int =
        data.sum

    assert(s(data) == 6)
  }

  test("filter") {
      def f[D[_]: Data](data: D[Int]): D[Int] =
        data.filter(_ % 2 == 0)

    assert(f(data) == Seq(2))
  }

  test("headOption") {
      def h[D[_]: Data](data: D[Int]): Option[Int] =
        data.headOption

    assert(h(data) == Some(1))
    val empty = Traversable.empty[Int]
    assert(h(empty) == None)
  }

  test("isEmpty") {
      def e[D[_]: Data](data: D[_]): Boolean =
        data.isEmpty

    assert(!e(data))
    val empty = Traversable.empty[Int]
    assert(e(empty))
  }

  test("toMap") {
      def toM[D[_]: Data](data: D[Int]): Map[Int, Int] =
        data.map(x => (x, x)).toMap

    assert(toM(data) == Map(1 -> 1, 2 -> 2, 3 -> 3))
  }

  test("zipWithIndex") {
      def foo[D[_]: Data](data: D[Int]): Unit =
        assert(data.zipWithIndex == Seq((1, 0), (2, 1), (3, 2)))

    foo(data)
  }

  test("zip") {
      def foo[D[_]: Data](data: D[Int]): D[(Int, Int)] =
        data.zip(data)

    assert(foo(data) == Seq((1, 1), (2, 2), (3, 3)))
  }

  test("implicits to Traversable") {
    import t.Implicits._
    val ignore0: Traversable[Int] = seq2data(Seq(1))
    val ignore1: Traversable[Int] = array2Data(Array(1))
  }

}