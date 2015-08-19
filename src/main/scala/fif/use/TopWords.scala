package fif.use

import fif.{ DataOps, Data }

import scala.language.higherKinds
import scala.reflect.ClassTag

object TopWords {

  import DataOps.syntax._

  type Document = Traversable[String]

  def wordcount[D[_]: Data](data: D[Document]): Map[String, Long] = {
    import Semigroup.Implicits.longSg
    ToMap {
      data.flatMap { doc =>
        doc.map { word => (word, 1l) }
      }
    }
  }

  def tfidf[D[_]: Data](data: D[Document]) = {

    val wc = wordcount(data)

  }

}