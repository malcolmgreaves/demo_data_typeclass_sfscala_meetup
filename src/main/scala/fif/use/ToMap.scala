package fif.use

import algebra.Semigroup
import fif.{ Data, DataOps }

import scala.language.higherKinds
import scala.reflect.ClassTag

object ToMap extends Serializable {

  import DataOps.syntax._

  def addToMap[K, V: Semigroup](m: Map[K, V])(key: K, value: V): Map[K, V] =
    if (m.contains(key))
      (m - key) + (key -> implicitly[Semigroup[V]].combine(m(key), value))
    else
      m + (key -> value)

  def combine[K, V: Semigroup](m1: Map[K, V], m2: Map[K, V]): Map[K, V] = {
    val (larger, smaller) =
      if (m1.size > m2.size)
        (m1, m2)
      else
        (m2, m1)

    smaller.foldLeft(larger) {
      case (m, (key, value)) =>
        addToMap(m)(key, value)
    }
  }

  def apply[T: ClassTag, U: ClassTag: Semigroup, D[_]: Data](data: D[(T, U)]): Map[T, U] = {
    implicit val _ = identity[(T, U)] _
    apply[(T, U), T, U, D](data)
  }

  def apply[A, T: ClassTag, U: ClassTag: Semigroup, D[_]: Data](data: D[A])(implicit ev: A <:< (T, U)): Map[T, U] = {

    val sg = implicitly[Semigroup[U]]

    data.aggregate(Map.empty[T, U])(
      {
        case (m, a) =>
          val (t, u) = ev(a)
          addToMap(m)(t, u)
      },
      combine
    )
  }
}

