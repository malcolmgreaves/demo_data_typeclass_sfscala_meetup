package fif.use

import algebra.Semigroup
import fif.{ DataOps, Data }

import scala.language.higherKinds
import scala.reflect.ClassTag

object TopWords {

  type Id = Long
  type Text = Traversable[String]
  type Document = (Id, Text)

  import DataOps.syntax._

  val intSg: Semigroup[Int] =
    new Semigroup[Int] {
      override def combine(x: Int, y: Int): Int =
        x + y
    }

  val longSg: Semigroup[Long] =
    new Semigroup[Long] {
      override def combine(x: Long, y: Long): Long =
        x + y
    }

  val stringSetSg: Semigroup[Set[String]] =
    new Semigroup[Set[String]] {
      override def combine(x: Set[String], y: Set[String]): Set[String] =
        x ++ y
    }

  def wordcount[D[_]: Data](data: D[Document]): Map[String, Long] = {
    implicit val _ = longSg
    ToMap {
      data.flatMap { doc =>
        doc._2.map { word => (word, 1l) }
      }
    }
  }

  def termFrequency[D[_]: Data](data: D[Document]) = {
    val wc = wordcount(data)

    import fif.TravData.Implicits.t
    val totalCounts = Sum(wc.values.toTraversable)

    (word: String) =>
      if (wc contains word)
        wc(word) / totalCounts
      else
        0.0
  }

  def inverseDocumentFrequency[D[_]: Data](data: D[Document]) = {

    val totalDocuments = data.size.toDouble

    val dc: Map[Id, Set[String]] = {
      implicit val _ = stringSetSg
      ToMap(data.map { doc => (doc._1, doc._2.toSet) })
    }

    implicit val _ = longSg
    val idf =
      dc.foldLeft(Map.empty[String, Long]) {
        case (m, (docId, uniqWords)) =>
          uniqWords.foldLeft(m) {
            case (updating, word) =>
              ToMap.addToMap(updating)(word, 1l)
          }
      }
        .map {
          case (k, v) => (k, v.toDouble)
        }

    (word: String) =>
      if (idf contains word)
        math.log(totalDocuments / idf(word))
      else
        0.0
  }

  def termFrequencyInverseDocumentFrequency[D[_]: Data](data: D[Document]) = {

    val tf = termFrequency(data)
    val idf = inverseDocumentFrequency(data)

    (word: String) =>
      tf(word) * idf(word)
  }

  def apply[D[_]: Data](data: D[Document], top: Int): Seq[String] = {

    val tfidf = termFrequencyInverseDocumentFrequency(data)

    data
      .flatMap(_._2)
      .map { word =>

      }

    ???
  }

}