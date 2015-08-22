package fif.use

import algebra.Eq

/**
 * Removes duplicates according to Eq[A]. Orders on priority according to Cmp[A]
 */
abstract class PriorityQueue[A: Cmp: Eq] extends SortableContainer[A] {

  def peekMin(existing: Structure): Option[A]

  def takeMin(existing: Structure): Option[(A, Structure)]
}