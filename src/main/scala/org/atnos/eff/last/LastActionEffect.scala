package org.atnos.eff.last

import cats.Eval
import cats.Traverse
import cats.syntax.all._
import org.atnos.eff.Continuation
import org.atnos.eff.Eff
import org.atnos.eff.Interpret
import org.atnos.eff.Interpreter
import org.atnos.eff.Member
import org.atnos.eff.|=

object LastActionEffect
  extends LastActionCreation with LastActionInterpretation

trait LastActionTypes {
  sealed trait LastAction[A]

  case class SideEffectLastAction(
    value: Eval[Unit]
  ) extends LastAction[Unit]

  type _last[R] = LastAction |= R
}

trait LastActionCreation extends LastActionTypes{
  def addLast[R: _last](a: => Unit): Eff[R, Unit] =
    Eff.send(SideEffectLastAction(cats.Eval.later(a)))
}

trait LastActionInterpretation extends LastActionTypes {

  implicit class LastOps[R, A](private val eff: Eff[R, A]) {
    def runLast[U](implicit
      m: Member.Aux[LastAction, R, U]
    ): Eff[U, A] =
      Interpret
        .runInterpreter(eff)(new Interpreter[LastAction, U, A, Eval[A]] {
          override def onPure(a: A): Eff[U, Eval[A]] = {
            Eff.pure(Eval.now(a))
          }

          override def onEffect[X](
            x: LastAction[X],
            continuation: Continuation[U, X, Eval[A]]
          ): Eff[U, Eval[A]] = {
            x match {
              case SideEffectLastAction(value) =>
                continuation(())
                  .map { eval =>
                    value >> eval
                  }
            }
          }

          override def onLastEffect[X](
            x: LastAction[X],
            continuation: Continuation[U, X, Unit]
          ): Eff[U, Unit] =
            x match {
              case SideEffectLastAction(value) =>
                continuation(()).map(_ => value.value)
            }

          override def onApplicativeEffect[X, T[_]: Traverse](
            xs: T[LastAction[X]],
            continuation: Continuation[U, T[X], Eval[A]]
          ): Eff[U, Eval[A]] = {

            continuation
              .asInstanceOf[Continuation[U, T[Unit], Eval[A]]](xs.map(_ => ()))
              .map { eval =>
                xs.foldLeft(Eval.Unit) {
                  case (x, SideEffectLastAction(value)) =>
                    value >> x
                } >> eval
              }
          }
        })
        .map(_.value)
  }
}
