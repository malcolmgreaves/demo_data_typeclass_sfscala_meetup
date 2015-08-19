package fif.use

import fif.{Data, DataOps }

import scala.language.higherKinds
import scala.reflect.ClassTag

object ToMap {

  import DataOps.syntax._

  def addToMap[K,V:Semigroup](m: Map[K,V])(key: K, value: V): Map[K,V] =
    if (m.contains(key))
      (m - key) + (key -> implicitly[Semigroup[V]].combine(m(key), value))
    else
      m + (key -> value)

  def apply[A, T : ClassTag, U : ClassTag : Semigroup, D[_] : Data](data: D[A])(implicit ev: A <:< (T, U)): Map[T, U] = {

    val sg = implicitly[Semigroup[U]]

    data.aggregate(Map.empty[T, U])(
      {
        case (m, a) =>
          val (t, u) = ev(a)
          addToMap(m)(t,u)
      },
      {
        case (m1, m2) =>
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
    )
  }
}

