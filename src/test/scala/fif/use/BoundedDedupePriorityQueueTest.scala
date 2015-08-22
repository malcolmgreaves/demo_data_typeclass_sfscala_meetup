package fif.use

import algebra.Eq
import org.scalatest.FunSuite

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.reflect.ClassTag

class BoundedDedupePriorityQueueTest extends FunSuite {

  def makePriorityQueueModule(limit: Option[Int]) = {

    implicit val doubleCmp: Cmp[(String, Double)] =
      new Cmp[(String, Double)] {
        override def compare(a: (String, Double), b: (String, Double)): Comparision =
          if (a._2 > b._2) // backwards for max-priority queue!
            Less
          else if (a._2 < b._2) // backwards for max-priority queue!
            Greater
          else
            Equivalent
      }

    implicit val stringEq: Eq[(String, Double)] =
      Eq.instance[(String, Double)] {
        case ((a, _), (b, _)) =>
          a == b
      }

    ListPriorityQueue[(String, Double)](limit)
  }

  val values = Seq(
    ("hello", 100.0),
    ("[n] world", 1.0),
    ("[n] world", 1.0), // should be discarded
    ("second_place", 77.0),
    ("second_place", 1.0), // should be discarded
    ("[n] today", 1.0),
    ("sixth_place", 1.1),
    ("sixth_place", 1.1),
    ("[n] went", 1.0),
    ("hello", 101.0), // should replace the first "hello"
    ("[n] softly", 1.0),
    ("fourth_place", 44.0),
    ("[n] dude", 1.0),
    ("fifth_place", 1.2),
    ("third_place", 66.0)
  )

  test("[list] PQ of size 6 with (String,Double) items") {

    val limit = 6

    val priorityQueueModule = makePriorityQueueModule(Some(limit))

    val (finalPqInstance, removed) = SortableContainer.insert(priorityQueueModule, values)
    assert(removed.isDefined && removed.get.size == 5)

    val sorted = priorityQueueModule.sort(finalPqInstance)
    assert(sorted.size == limit)

    val expected = Seq("hello", "second_place", "third_place", "fourth_place", "fifth_place", "sixth_place")
    sorted.map(_._1).zip(expected)
      .foreach {
        case (x, e) => assert(x == e)
      }
  }

  test("[list] unbounded, use as de-duplicating sorter") {

    val module = makePriorityQueueModule(None)

    val (pq, removed) = SortableContainer.insert(module, values)
    assert(removed.isEmpty)

    val expecting = Seq(
      ("hello", 101.0), // should replace the first "hello"
      ("second_place", 77.0),
      ("third_place", 66.0),
      ("fourth_place", 44.0),
      ("fifth_place", 1.2),
      ("sixth_place", 1.1),
      ("[n] world", 1.0),
      ("[n] today", 1.0),
      ("[n] went", 1.0),
      ("[n] softly", 1.0),
      ("[n] dude", 1.0)
    )

    val sorted = module.sort(pq)

    assert(expecting.size == sorted.size)

    expecting.zip(sorted)
      .foreach {
        case ((cK, _), (mK, _)) => assert(cK == mK)
      }
  }

}