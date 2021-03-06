package scalaz

import scala.annotation._
import Id.Id
import Leibniz.{===, refl}

/**
 * Represents a type `MA` that has been destructured into as a type constructor `M[_]`
 * applied to type `A`, along with a corresponding type class instance `TC[M]`.
 *
 * The implicit conversions in the companion object provide a means to obtain type class
 * instances for partially applied type constructors, in lieu of direct compiler support
 * as described in [[https://issues.scala-lang.org/browse/SI-2712 SI-2712]].
 *
 * {{{
 * // Directly depending on Applicative[G]
 * def traverse[G[_], B](f: A => G[B])(implicit G: Applicative[G]): G[F[B]] =
 *   G.traverse(self)(f)
 *
 * // Indirect lookup of the Applicative instance
 * // Requires the -Ydep-method-types flag to scalac with scala 2.9.x (not required with 2.10)
 * def traverseI[GB](f: A => GB)(implicit G: Unapply[Applicative, GB]): G.M[F[G.A]] /*G[F[B]*/ = {
 *   G.TC.traverse(self)(a => G(f(a)))
 * }
 *
 * // Deforested version of traverseI
 * def traverseI2[GB](f: A => GB)(implicit G: Unapply[Applicative, GB]): G.M[F[G.A]] /*G[F[B]*/ = {
 *   G.TC.traverse(self)(G.leibniz.onF(f))
 * }
 *
 * // Old usage
 * def stateTraverse1 {
 *   import scalaz._, Scalaz._
 *   import State.{State, stateMonad}
 *   val ls = List(1, 2, 3)
 *   val traverseOpt: Option[List[Int]] = ls.traverse(a => Some(a))
 *   val traverseState: State[Int, List[Int]] = ls.traverse[({type λ[α]=State[Int, α]})#λ, Int](a => State((x: Int) => (x + 1, a)))
 * }
 *
 * // New usage
 * def stateTraverse2 {
 *   import scalaz._, Scalaz._
 *   val ls = List(1, 2, 3)
 *   val traverseOpt: Option[List[Int]] = ls.traverseI(a => some(a))
 *   val traverseState = ls.traverseI(a => State((x: Int) => (x + 1, a)))
 * }
 *
 * }}}
 *
 * Credits to Miles Sabin.
 */
@implicitNotFound("Implicit not found: scalaz.Unapply[${TC}, ${MA}]. Unable to unapply type `${MA}` into a type constructor of kind `M[_]` that is classified by the type class `${TC}`. Check that the type class is defined by compiling `implicitly[${TC}[type constructor]]` and review the implicits in object Unapply, which only cover common type 'shapes.'")
trait Unapply[TC[_[_]], MA] {

  /** The type constructor */
  type M[_]

  /** The type that `M` was applied to */
  type A

  /** The instance of the type class */
  def TC: TC[M]

  /** Evidence that MA =:= M[A] */
  def leibniz: MA === M[A]

  /** Compatibility. */
  @inline final def apply(ma: MA): M[A] = leibniz(ma)
}

sealed trait Unapply_4 {
  // /** Unpack a value of type `A0` into type `[a]A0`, given a instance of `TC` */
  implicit def unapplyA[TC[_[_]], A0](implicit TC0: TC[({type λ[α] = A0})#λ]): Unapply[TC, A0] {
    type M[X] = A0
    type A = A0
  } = new Unapply[TC, A0] {
    type M[X] = A0
    type A = A0
    def TC = TC0
    def leibniz = refl
  }
}

sealed trait Unapply_3 extends Unapply_4 {
  /**Unpack a value of type `M0[F[_], A0, A0, B0]` into types `[a]M0[F, a, a, B0]` and `A0`, given an instance of `TC` */
  implicit def unapplyMFABC1and2[TC[_[_]], F[_], M0[F[_], _, _, _], A0, B0](implicit TC0: TC[({type λ[α] = M0[F, α, α, B0]})#λ]): Unapply[TC, M0[F, A0, A0, B0]] {
    type M[X] = M0[F, X, X, B0]
    type A = A0
  } = new Unapply[TC, M0[F, A0, A0, B0]] {
    type M[X] = M0[F, X, X, B0]
    type A = A0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[F[_], A0, B0, C0]` into types `[c]M0[F, A0, B0, c]` and `C0`, given an instance of `TC` */
  implicit def unapplyMFABC3[TC[_[_]], F[_], M0[F[_], _, _, _], A0, B0, C0](implicit TC0: TC[({type λ[ɣ] = M0[F, A0, B0, ɣ]})#λ]): Unapply[TC, M0[F, A0, B0, C0]] {
    type M[X] = M0[F, A0, B0, X]
    type A = C0
  } = new Unapply[TC, M0[F, A0, B0, C0]] {
    type M[X] = M0[F, A0, B0, X]
    type A = C0
    def TC = TC0
    def leibniz = refl
  }
}

sealed trait Unapply_2 extends Unapply_3 {
  // Things get tricky with type State[S, A] = StateT[Id, S, A], both unapplyMAB2 and unapplyMFAB2 are applicable
  // Without characterizing this fully, I'm using the standard implicit prioritization to avoid this.

  /**Unpack a value of type `M0[F[_], A0, B0]` into types `[a]M0[F, a, B0]` and `A0`, given an instance of `TC` */
  implicit def unapplyMFAB1[TC[_[_]], F[_], M0[F[_], _, _], A0, B0](implicit TC0: TC[({type λ[α] = M0[F, α, B0]})#λ]): Unapply[TC, M0[F, A0, B0]] {
    type M[X] = M0[F, X, B0]
    type A = A0
  } = new Unapply[TC, M0[F, A0, B0]] {
    type M[X] = M0[F, X, B0]
    type A = A0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[F[_], A0, B0]` into types `[b]M0[F, A0, b]` and `B0`, given an instance of `TC` */
  implicit def unapplyMFAB2[TC[_[_]], F[_], M0[F[_], _, _], A0, B0](implicit TC0: TC[({type λ[β] = M0[F, A0, β]})#λ]): Unapply[TC, M0[F, A0, B0]] {
    type M[X] = M0[F, A0, X]
    type A = B0
  } = new Unapply[TC, M0[F, A0, B0]] {
    type M[X] = M0[F, A0, X]
    type A = B0
    def TC = TC0
    def leibniz = refl
  }
}

sealed trait Unapply_1 extends Unapply_2 {
  /**Unpack a value of type `M0[A0, B0, C0, D0, E0, F0, G0]` into types `[g]M0[A0, B0, C0, D0, E0, F0, g]` and `G0`, given an instance of `TC` */
  implicit def unapplyMABCDEFG7[TC[_[_]], M0[_, _, _, _, _, _, _], A0, B0, C0, D0, E0, F0, G0](implicit TC0: TC[({type λ[α] = M0[A0, B0, C0, D0, E0, F0, α]})#λ]): Unapply[TC, M0[A0, B0, C0, D0, E0, F0, G0]] {
    type M[X] = M0[A0, B0, C0, D0, E0, F0, X]
    type A = G0
  } = new Unapply[TC, M0[A0, B0, C0, D0, E0, F0, G0]] {
    type M[X] = M0[A0, B0, C0, D0, E0, F0, X]
    type A = G0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0, C0, D0, E0, F0]` into types `[f]M0[A0, B0, C0, D0, E0, f]` and `F0`, given an instance of `TC` */
  implicit def unapplyMABCDEF6[TC[_[_]], M0[_, _, _, _, _, _], A0, B0, C0, D0, E0, F0](implicit TC0: TC[({type λ[α] = M0[A0, B0, C0, D0, E0, α]})#λ]): Unapply[TC, M0[A0, B0, C0, D0, E0, F0]] {
    type M[X] = M0[A0, B0, C0, D0, E0, X]
    type A = F0
  } = new Unapply[TC, M0[A0, B0, C0, D0, E0, F0]] {
    type M[X] = M0[A0, B0, C0, D0, E0, X]
    type A = F0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0, C0, D0, E0]` into types `[e]M0[A0, B0, C0, D0, e]` and `E0`, given an instance of `TC` */
  implicit def unapplyMABCDE5[TC[_[_]], M0[_, _, _, _, _], A0, B0, C0, D0, E0](implicit TC0: TC[({type λ[α] = M0[A0, B0, C0, D0, α]})#λ]): Unapply[TC, M0[A0, B0, C0, D0, E0]] {
    type M[X] = M0[A0, B0, C0, D0, X]
    type A = E0
  } = new Unapply[TC, M0[A0, B0, C0, D0, E0]] {
    type M[X] = M0[A0, B0, C0, D0, X]
    type A = E0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0, C0, D0]` into types `[d]M0[A0, B0, C0, d]` and `D0`, given an instance of `TC` */
  implicit def unapplyMABCD4[TC[_[_]], M0[_, _, _, _], A0, B0, C0, D0](implicit TC0: TC[({type λ[α] = M0[A0, B0, C0, α]})#λ]): Unapply[TC, M0[A0, B0, C0, D0]] {
    type M[X] = M0[A0, B0, C0, X]
    type A = D0
  } = new Unapply[TC, M0[A0, B0, C0, D0]] {
    type M[X] = M0[A0, B0, C0, X]
    type A = D0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0, C0]` into types `[c]M0[A0, B0, c]` and `C0`, given an instance of `TC` */
  implicit def unapplyMABC3[TC[_[_]], M0[_, _, _], A0, B0, C0](implicit TC0: TC[({type λ[α] = M0[A0, B0, α]})#λ]): Unapply[TC, M0[A0, B0, C0]] {
    type M[X] = M0[A0, B0, X]
    type A = C0
  } = new Unapply[TC, M0[A0, B0, C0]] {
    type M[X] = M0[A0, B0, X]
    type A = C0
    def TC = TC0
    def leibniz = refl
  }
}

sealed trait Unapply_0 extends Unapply_1 {
  /** Unpack a value of type `M0[F0, A0]` where `F0: * -> *` into
    * types `[a]M0[F0, a]` and `A`, given an instance of `TC`
    */
  implicit def unapplyMFA[TC[_[_]], M0[_[_], _], F0[_], A0](implicit TC0: TC[({type λ[α] = M0[F0, α]})#λ]): Unapply[TC, M0[F0, A0]] {
    type M[X] = M0[F0, X]
    type A = A0
  } = new Unapply[TC, M0[F0, A0]] {
    type M[X] = M0[F0, X]
    type A = A0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0]` into types `[a]M0[a, B0]` and `A`, given an instance of `TC` */
  implicit def unapplyMAB1[TC[_[_]], M0[_, _], A0, B0](implicit TC0: TC[({type λ[α] = M0[α, B0]})#λ]): Unapply[TC, M0[A0, B0]] {
    type M[X] = M0[X, B0]
    type A = A0
  } = new Unapply[TC, M0[A0, B0]] {
    type M[X] = M0[X, B0]
    type A = A0
    def TC = TC0
    def leibniz = refl
  }

  /**Unpack a value of type `M0[A0, B0]` into types `[b]M0[A0, b]` and `B`, given an instance of `TC` */
  implicit def unapplyMAB2[TC[_[_]], M0[_, _], A0, B0](implicit TC0: TC[({type λ[α] = M0[A0, α]})#λ]): Unapply[TC, M0[A0, B0]] {
    type M[X] = M0[A0, X]
    type A = B0
  } = new Unapply[TC, M0[A0, B0]] {
    type M[X] = M0[A0, X]
    type A = B0
    def TC = TC0
    def leibniz = refl
  }
}

object Unapply extends Unapply_0 {
  type AuxA[TC[_[_]], MA, A0] = Unapply[TC, MA] {
    type A = A0
  }

  /** Fetch a well-typed `Unapply` for the given typeclass and type. */
  def apply[TC[_[_]], MA](implicit U: Unapply[TC, MA]): U.type {
    type M[A] = U.M[A]
    type A = U.A
  } = U

  /** Unpack a value of type `M0[A0]` into types `M0` and `A0`, given a instance of `TC` */
  implicit def unapplyMA[TC[_[_]], M0[_], A0](implicit TC0: TC[M0]): Unapply[TC, M0[A0]] {
    type M[X] = M0[X]
    type A = A0
  } = new Unapply[TC, M0[A0]] {
    type M[X] = M0[X]
    type A = A0
    def TC = TC0
    def leibniz = refl
  }

  // TODO More!
}

trait Unapply2[TC[_[_, _]], MAB] {

  /** The type constructor */
  type M[_, _]

  /** The first type that `M` was applied to */
  type A

  /** The second type that `M` was applied to */
  type B

  /** The instance of the type class */
  def TC: TC[M]

  /** Evidence that MAB =:= M[A, B] */
  def leibniz: MAB === M[A, B]

  /** Compatibility. */
  @inline final def apply(ma: MAB): M[A, B] = leibniz(ma)
}

sealed trait Unapply2_0 {
  /**Unpack a value of type `M0[F[_], A0, B0]` into types `[a, b]=M0[F, a, b]`, `A0`, and 'B9', given an instance of `TC` */
  implicit def unapplyMFAB[TC[_[_, _]], F[_], M0[F[_], _, _], A0, B0](implicit TC0: TC[({type λ[α, β] = M0[F, α, β]})#λ]): Unapply2[TC, M0[F, A0, B0]] {
    type M[X, Y] = M0[F, X, Y]
    type A = A0
    type B = B0
  } = new Unapply2[TC, M0[F, A0, B0]] {
    type M[X, Y] = M0[F, X, Y]
    type A = A0
    type B = B0
    def TC = TC0
    def leibniz = refl
  }
}

object Unapply2 extends Unapply2_0 {
  /** Fetch a well-typed `Unapply2` for the given typeclass and type. */
  def apply[TC[_[_, _]], MAB](implicit U: Unapply2[TC, MAB]): U.type {
    type M[X, Y] = U.M[X, Y]
    type A = U.A
    type B = U.B
  } = U

  /**Unpack a value of type `M0[A0, B0]` into types `M0`, `A`, and 'B', given an instance of `TC` */
  implicit def unapplyMAB[TC[_[_, _]], M0[_, _], A0, B0](implicit TC0: TC[M0]): Unapply2[TC, M0[A0, B0]] {
    type M[X, Y] = M0[X, Y]
    type A = A0
    type B = B0
  } = new Unapply2[TC, M0[A0, B0]] {
    type M[X, Y] = M0[X, Y]
    type A = A0
    type B = B0
    def TC = TC0
    def leibniz = refl
  }
}

trait Unapply21[TC[_[_, _], _], MAB]{
  type M[_, _]
  type A
  type B
  def TC: TC[M, A]

  def leibniz: MAB === M[A, B]
  @inline final def apply(mabc: MAB): M[A, B] = leibniz(mabc)
}

object Unapply21 {
  /** Fetch a well-typed `Unapply21` for the given typeclass and type. */
  def apply[TC[_[_, _], _], MAB](implicit U: Unapply21[TC, MAB]): U.type {
    type M[X, Y] = U.M[X, Y]
    type A = U.A
    type B = U.B
  } = U

  implicit def unapply210MFABC[TC[_[_, _], _], F[_,_], M0[_[_], _, _], A0, B0, C](implicit TC0: TC[({type f[a, b] = M0[({type m[x] = F[a, x]})#m, C, b]})#f, A0]): Unapply21[TC, M0[({type f[x] = F[A0, x]})#f, C, B0]]{
    type M[X, Y] = M0[({type f[a] = F[X, a]})#f, C, Y]
    type A = A0
    type B = B0
  } = new Unapply21[TC, M0[({type f[x] = F[A0, x]})#f, C, B0]]{
    type M[X, Y] = M0[({type f[a] = F[X, a]})#f, C, Y]
    type A = A0
    type B = B0

    def TC = TC0
    def leibniz = refl
  }
}

trait UnapplyProduct[TC[_[_]], MA, MB] {
  type M[X]
  type A
  type B
  def TC: TC[M]
  type MA_ = MA
  def _1(ma: MA): M[A]
  def _2(mb: MB): M[B]
}

object UnapplyProduct {
  import Isomorphism.<~>
  // This seems to motivate multiple implicit parameter sections. Is there another way?
  // Currently, a type annotation in a parameter declaration may be path-dependent on a
  // parameter from a previous parameter section, hence `iso` can't be in the first parameter
  // section; which itself can't be implicit.
  //
  // There are two possible changes to Scalac that could help:
  //
  // 1. Allow multiple implicit parameter sections
  // 2. Allow path-dependent parameter types to refer to the current (or even subsequent)
  //    parameter sections.
  //
  //    A motivating example for #2 is in neg/depmet_try_implicit.scala
  //
  //    def foo[T, T2](a: T, x: T2)(implicit w: ComputeT2[T, T2]) // awkward, if you provide T you must also provide T2
  //    def foo[T](a: T, x: w.T2)(implicit w: ComputeT2[T])       // more compact, and allows you to provide T1 and infer T2.
  //
  /*implicit */ def unapply[TC[_[_]], MA0, MB0](/*implicit */U1: Unapply[TC, MA0], U2: Unapply[TC, MB0])(implicit iso: U1.M <~> U2.M) = new UnapplyProduct[TC, MA0, MB0] {
    type M[X] = U1.M[X]
    type A = U1.A
    type B = U2.A
    def TC = U1.TC
    def _1(ma: MA0) = U1(ma)
    def _2(mb: MB0) = iso.from(U2(mb))
  }
}
