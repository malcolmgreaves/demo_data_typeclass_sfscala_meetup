package fif.use

import algebra.Eq

object ListPriorityQueue {

  def apply[A: Cmp: Eq](maximumHeapSize: Option[Int]): PriorityQueue[A] =
    new PriorityQueue[A] {

      override val maxSize = maximumHeapSize.map { v => math.max(0, v) }
      // we unpack here to use it internally, if applicable
      private val isMaxSizeDefined = maxSize.isDefined
      private val maxSizeIfDefined = maxSize.getOrElse(-1)

      override type Structure = List[A]

      override val empty: Structure =
        List.empty[A]

      override def peekMin(existing: Structure): Option[A] =
        existing.headOption

      private val sortFn = {
        val cmp = implicitly[Cmp[A]].compare _
        (a: A, b: A) => cmp(a, b) == Less
      }

      private val equality =
        implicitly[Eq[A]].eqv _

      private def contains(item: A, existing: Structure): Boolean =
        existing.exists(x => equality(x, item))

      override def insert(item: A)(existing: Structure): (Structure, Option[A]) =
        if (contains(item, existing))
          (existing, None)

        else {
          val newList = (existing :+ item).sortWith(sortFn)
          if (isMaxSizeDefined && newList.size > maxSizeIfDefined) {
            val end = math.min(newList.size, maxSizeIfDefined)
            (newList.slice(0, end), Some(newList(end)))

          } else
            (newList, None)
        }

      override def merge(one: Structure, two: Structure): (Structure, Option[Iterable[A]]) =
        if (one.isEmpty && two.isEmpty)
          (empty, None)

        else {

          val merged = (one ++ two).sortWith(sortFn)
          if (isMaxSizeDefined && merged.size > maxSizeIfDefined) {
            val end = math.min(merged.size, maxSizeIfDefined)
            (merged.slice(0, end), Some(merged.slice(end, merged.size)))

          } else
            (merged, None)
        }

      override def takeMin(existing: List[A]): Option[(A, Structure)] =
        existing.headOption
          .map { head =>
            (head, existing.slice(1, existing.size))
          }

      override def sort(existing: Structure): Iterable[A] =
        existing.sortWith(sortFn)
    }

}
