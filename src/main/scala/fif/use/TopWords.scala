package fif.use

import algebra.{ Eq, Semigroup }
import fif.{ DataOps, Data }

import scala.collection.mutable.ArrayBuffer
import scala.language.higherKinds
import scala.reflect.ClassTag

case object TopWords {

  type Id = Long
  type Text = Traversable[String]
  type Document = (Id, Text)

  import DataOps.syntax._

  val intSg: Semigroup[Int] =
    new Semigroup[Int] {
      override def combine(x: Int, y: Int): Int =
        x + y
    }

  def numericSemigroup[N: Numeric]: Semigroup[N] =
    new Semigroup[N] {
      private val num = implicitly[Numeric[N]]
      override def combine(x: N, y: N): N =
        num.plus(x, y)
    }

  def setSemigroup[A]: Semigroup[Set[A]] =
    new Semigroup[Set[A]] {
      override def combine(x: Set[A], y: Set[A]): Set[A] =
        x ++ y
    }

  def wordcount[D[_]: Data](data: D[Document]): Map[String, Long] = {
    implicit val _ = numericSemigroup[Long]
    ToMap {
      data.flatMap { doc =>
        doc._2.map { word => (word, 1l) }
      }
    }
  }

  def termFrequency[D[_]: Data](data: D[Document]) = {
    val wc = wordcount(data)

    import fif.TravData.Implicits.t
    val totalCounts = Sum(wc.values.toTraversable).toDouble

    (word: String) =>
      if (wc contains word)
        wc(word) / totalCounts
      else
        0.0
  }

  def inverseDocumentFrequency[D[_]: Data](data: D[Document]) = {

    val totalDocuments = data.size.toDouble

    val wordDocFreq = {

      val dc: Map[Id, Set[String]] = {
        implicit val _ = setSemigroup[String]
        ToMap(data.map { doc => (doc._1, doc._2.toSet) })
      }

      implicit val _ = numericSemigroup[Double]
      dc
        .foldLeft(Map.empty[String, Double]) {
          case (m, (docId, uniqWords)) =>
            uniqWords.foldLeft(m) {
              case (updating, word) =>
                ToMap.addToMap(updating)(word, 1.0)
            }
        }
    }

    (word: String) =>
      if (wordDocFreq contains word)
        math.log(totalDocuments / wordDocFreq(word))
      else
        0.0
  }

  def termFrequencyInverseDocumentFrequency[D[_]: Data](data: D[Document]) = {

    val tf = termFrequency(data)
    val idf = inverseDocumentFrequency(data)

    (word: String) =>
      tf(word) * idf(word)
  }

  def apply[D[_]: Data](data: D[Document], top: Int, tfidf: Boolean = true): Seq[String] = {

    val scorer =
      if (tfidf)
        termFrequencyInverseDocumentFrequency(data)

      else {
        val wc = wordcount(data)
        (word: String) =>
          if (wc contains word)
            wc(word).toDouble
          else
            0.0
      }

    val priorityQueueModule = {

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

      ListPriorityQueue[(String, Double)](Some(top))
    }

    implicit val ct: ClassTag[priorityQueueModule.Structure] =
      ClassTag(priorityQueueModule.empty.getClass)

    val finalPq =
      data
        .flatMap(_._2)
        .map { word =>
          (word, scorer(word))
        }
        .aggregate(priorityQueueModule.empty)(
          {
            case (pq, wordAndScore) =>
              priorityQueueModule.insert(wordAndScore)(pq)._1
          },
          {
            case (pq1, pq2) =>
              priorityQueueModule.merge(pq1, pq2)._1
          }
        )

    {
      val buffer = new ArrayBuffer[String]
      var draining = finalPq
      while (priorityQueueModule.peekMin(draining).isDefined) {

        priorityQueueModule.takeMin(draining) match {
          case Some(((word, _), newPq)) =>
            draining = newPq
            buffer.append(word)

          case None =>
            ()
        }
      }
      buffer.toSeq
    }
  }

}