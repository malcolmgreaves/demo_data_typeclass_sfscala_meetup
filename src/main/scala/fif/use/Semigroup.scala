package fif.use

import scala.language.{higherKinds, implicitConversions}

trait Semigroup[A] {
  def combine(a: A, b: A): A
}

object Semigroup {

  def numericSg[N:Numeric]: Semigroup[N] =
    new Semigroup[N] {
      override def combine(a: N, b: N): N =
        implicitly[Numeric[N]].plus(a,b)
    }

  object Implicits {
    implicit val intSg = numericSg[Int]
    implicit val longSg = numericSg[Long]
    implicit val floatSg= numericSg[Float]
    implicit val doubleSg = numericSg[Double]
  }

}