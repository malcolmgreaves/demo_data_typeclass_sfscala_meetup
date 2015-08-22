package fif.use

import scala.language.postfixOps

abstract class SortableContainer[A: Cmp] extends Serializable {

  type Structure

  val maxSize: Option[Int]

  def empty: Structure

  def merge(a: Structure, b: Structure): (Structure, Option[Iterable[A]])

  def insert(item: A)(existing: Structure): (Structure, Option[A])

  def sort(existing: Structure): Iterable[A]

}

object SortableContainer {

  def insert[A](module: SortableContainer[A])(
    existing: module.Structure,
    elements: Iterable[A]
  ): (module.Structure, Option[Iterable[A]]) = {

    val (newPq, kickedOut) =
      elements.foldLeft((existing, Seq.empty[A])) {
        case ((pq, removing), aItem) =>
          val (resulting, maybeRemoved) = module.insert(aItem)(pq)
          (
            resulting,
            maybeRemoved match {
              case None =>
                removing
              case Some(removed) =>
                removing :+ removed
            }
          )
      }

    (
      newPq,
      if (kickedOut isEmpty)
        None
      else
        Some(kickedOut)
    )
  }

  def insert[A](module: SortableContainer[A], elements: Iterable[A]): (module.Structure, Option[Iterable[A]]) =
    insert(module)(module.empty, elements)

}