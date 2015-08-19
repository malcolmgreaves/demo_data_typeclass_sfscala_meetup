package fif

import scala.language.higherKinds
import scala.reflect.ClassTag

import simulacrum._

/**
 * Trait that abstractly represents operations that can be performed on a dataset.
 * The implementation of Data is suitable for both large-scale, distributed data
 * or in-memory structures.
 */
@typeclass trait Data[D[_]] {

  /** Transform a dataset by applying f to each element. */
  def map[A, B: ClassTag](d: D[A])(f: A => B): D[B]

  def mapParition[A, B: ClassTag](d: D[A])(f: Iterable[A] => Iterable[B]): D[B]

  /** Apply a side-effecting function to each element. */
  def foreach[A](d: D[A])(f: A => Any): Unit

  def foreachPartition[A](d: D[A])(f: Iterable[A] => Any): Unit

  def filter[A](d: D[A])(f: A => Boolean): D[A]

  /**
   * Starting from a defined zero value, perform an operation seqOp on each element
   * of a dataset. Combine results of seqOp using combOp for a final value.
   */
  def aggregate[A, B: ClassTag](d: D[A])(zero: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B

  /** Sort the dataset using a function f that evaluates each element to an orderable type */
  def sortBy[A, B: ClassTag](d: D[A])(f: (A) => B)(implicit ord: math.Ordering[B]): D[A]

  /** Construct a traversable for the first k elements of a dataset. Will load into main mem. */
  def take[A](d: D[A])(k: Int): Traversable[A]

  def headOption[A](d: D[A]): Option[A]

  /** Load all elements of the dataset into an array in main memory. */
  def toSeq[A](d: D[A]): Seq[A]

  def flatMap[A, B: ClassTag](d: D[A])(f: A => TraversableOnce[B]): D[B]

  def flatten[A, B: ClassTag](d: D[A])(implicit asTraversable: A => TraversableOnce[B]): D[B]

  def groupBy[A, B: ClassTag](d: D[A])(f: A => B): D[(B, Iterator[A])]

  //  def as[A](d:D[A]): D[A] = d

  def reduce[A](d: D[A])(op: (A, A) => A): A

  def toMap[A, T, U](d: D[A])(implicit ev: A <:< (T, U)): Map[T, U]

  def size[A](d: D[A]): Long

  def isEmpty[A](d: D[A]): Boolean

  def sum[N: ClassTag: Numeric](d: D[N]): N

  def zip[A, B: ClassTag](d: D[A])(that: D[B]): D[(A, B)]

  def zipWithIndex[A](d: D[A]): D[(A, Long)]

}

/**
 * Allows importation of compile-time generated infix notation for the Data
 * typeclass. To bring into scope implicits that will convert methods on types
 * that obey the Data typeclass abstraction, do the following:
 *
 * ```
 * import fif.Data.syntax._
 * ```
 *
 * Now, in any method with the folowing type constraint on a generic, higher
 * kinded type D, D[_] : Data , we will be able to use the map (without loss
 * of generalization, any Data method) as:
 *
 * ```
 * import fif.Data
 * import Data.syntax._
 * def foo[D[_] : Data](data: D[Int]): Int =
 *  data.map(_ * 10).sum
 * ```
 */
object DataOps {

  val syntax = Data.ops
}