package datastructures

import scala.annotation.tailrec

sealed trait List[+A] // `List` data type, parameterized on a type, `A`
case object Nil extends List[Nothing] // A `List` data constructor representing the empty list
/* Another data constructor, representing nonempty lists. Note that `tail` is another `List[A]`,
which may be `Nil` or another `Cons`.
 */
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List { // `List` companion object. Contains functions for creating and working with lists.
  def sum(ints: List[Int]): Int = ints match { // A function that uses pattern matching to add up a list of integers
    case Nil => 0 // The sum of the empty list is 0.
    case Cons(head, tail) => head + sum(tail) // The sum of a list starting with `x` is `x` plus the sum of the rest of the list.
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x, xs) => x * product(xs)
  }

  def apply[A](as: A*): List[A] = // Variadic function syntax
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))


  //  Cons(1, Cons(2, Cons(3, Cons(4, Cons(5, Nil)))))
  val x = List(1, 2, 3, 4, 5) match {
    case Cons(x, Cons(2, Cons(4, _))) => x
    case Nil => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
    case Cons(h, t) => h + sum(t)
    case _ => 101
  }

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil => a2
      case Cons(h,t) => Cons(h, append(t, a2))
    }

  //  def foldRight[A,B](as: List[A], z: B)(f: (A, B) => B): B = // Utility functions
  //    as match {
  //      case Nil => z
  //      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
  //    }

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = foldLeft(reverse(as), z)((b, a) => f(a, b))

  def sum2(ns: List[Int]) =
    foldRight(ns, 0)((x,y) => x + y)

  def product2(ns: List[Double]) =
    foldRight(ns, 1.0)(_ * _) // `_ * _` is more concise notation for `(x,y) => x * y`; see sidebar


  def tail[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(h, t) => t
  }

  def setHead[A](l: List[A], h: A): List[A] = l match {
    case Nil => Nil
    case Cons(_, tail) => Cons(h, tail)
  }

  def drop[A](l: List[A], n: Int): List[A] =
    if (n <= 0) l
    else l match {
      case Cons(h, t) => drop(t, n-1)
      case _ => Nil
    }

  def takeWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Cons(a, as) if f(a) => Cons(a, takeWhile(as, f))
    case _ => Nil
  }

  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Cons(a, as) =>
      if (f(a)) dropWhile(as, f)
      else l
    case _ => Nil
  }

  //  def reverse[A](l: List[A]): List[A] = l match {
  //    case Cons(a, as) => append(reverse(as), List(a))
  //    case _ => Nil
  //  }

  def reverse[A](l: List[A]): List[A] = foldLeft(l, List[A]())((acc, h) => Cons(h, acc))

  def init[A](l: List[A]): List[A] = reverse(tail(reverse(l)))

  @tailrec
  def foldLeft[A, B](l: List[A], z: B)(f: (B, A) => B): B = l match {
    case Cons(a, as) => foldLeft(as, f(z, a))(f)
    case Nil => z
  }

  def reduce[A](l: List[A], z: A)(f: (A, A) => A): A = foldLeft[A, A](l, z)(f)

  def length[A](l: List[A]): Int = foldLeft[A, Int](l, 0)((aggrLength, _) => aggrLength + 1)

  def filter[A](l: List[A], f: A => Boolean): List[A] = flatMap[A, A](l)(a =>
    if (f(a)) List(a) else List()
  )

  //  def filter[A](l: List[A], f: A => Boolean): List[A] = foldLeft[A, List[A]](l, Nil)((b, a) =>
  //    if (f(a)) append(b, List(a))
  //    else b
  //  )

  def map[A, B](l: List[A])(f: A => B): List[B] = foldRight[A, List[B]](l, Nil)((a, bs) => Cons(f(a), bs))

  def flatten[A](l: List[List[A]]): List[A] = foldLeft[List[A], List[A]](l, Nil)((b, a) =>
    append(b, a)
  )

  def flatMap[A, B](l: List[A])(f: A => List[B]): List[B] = flatten(map(l)(f))

  def partition[A](l: List[A], p: A => Boolean): (List[A], List[A]) = foldRight[A, (List[A], List[A])](l, (Nil, Nil)){
    (elem, aggr) => aggr match { case (right, left) =>
      if (p(elem))(
          Cons(elem, right),
          left
        )
      else (
          right,
        Cons(elem, left)
        )
    }
  }

  def zipWith[A, B](list: List[A], other: List[B]): List[(A, B)] = list match {
    case Nil => Nil
    case Cons(head, tail) => other match {
      case Nil => Nil
      case Cons(headOther, tailOther) => Cons((head, headOther), zipWith(tail, tailOther))
    }
  }

  @tailrec
  def hasSubsequence[A](list: List[A], subList: List[A]): Boolean = list match {
    case Nil => subList.equals(Nil)
    case Cons(head, tail) => isPrefix(list, subList) || hasSubsequence(tail, subList)
  }

  @tailrec
  def isPrefix[A](list: List[A], prefix: List[A]): Boolean = list match {
    case Nil => Nil.equals(prefix)
    case Cons(head, tail) => prefix match {
      case Nil => true
      case Cons(prefixHead, prefixTail) => prefixHead.equals(head) && isPrefix(tail, prefixTail)
    }
  }
}
