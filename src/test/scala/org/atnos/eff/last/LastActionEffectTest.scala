package org.atnos.eff.last

import cats.Eval
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import scala.collection.mutable.ListBuffer
import org.atnos.eff.last.LastActionEffect._
import scala.concurrent.Await
import scala.concurrent.duration._

class LastActionEffectTest extends AnyFlatSpec with Diagrams {
  "runLast" should "run the last actions in the normal order" in {
    import org.atnos.eff.OptionEffect._

    type R = Fx.fx2[Option, LastAction]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- some[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(4)
      )
      b <- some[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(5)
      )
      c <- some[R, Int] {
        listBuffer.append(3)
        3
      }
      _ <- addLast[R](
        listBuffer.append(6)
      )
    } yield a + b + c

    // Since `Option` is eager
    assert(listBuffer.toList === List(1))

    val actual = Eff.run(runOption(eff).runLast)

    assert(actual === Some(6))
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6))
  }

  it should "run the last actions in the normal order even if use `Eval` together" in {
    import org.atnos.eff.EvalEffect._

    type R = Fx.fx2[Eval, LastAction]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(4)
      )
      b <- delay[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(5)
      )
      c <- delay[R, Int] {
        listBuffer.append(3)
        3
      }
      _ <- addLast[R](
        listBuffer.append(6)
      )
    } yield a + b + c

    // Since `Eval` is lazy
    assert(listBuffer.toList === Nil)

    val actual = Eff.run(runEval(eff).runLast)

    assert(actual === 6)
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6))
  }

  it should "run the last actions in the normal order even if use monix `Task` together" in {
    import org.atnos.eff.addon.monix.task._
    import monix.execution.Scheduler.Implicits.global

    type R = Fx.fx2[LastAction, Task]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- taskDelay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(4)
      )
      b <- taskDelay[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(5)
      )
      c <- taskDelay[R, Int] {
        listBuffer.append(3)
        3
      }
      _ <- addLast[R](
        listBuffer.append(6)
      )
    } yield a + b + c

    assert(listBuffer.toList === Nil)

    val actual = Await.result(
      runSequential(eff.runLast).runToFuture,
      100.microseconds
    )

    assert(actual === 6)
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6))
  }

  it should "run from inner action rather than outer one" in {
    import org.atnos.eff.EvalEffect._
    type R = Fx.fx2[Eval, LastAction]

    val listBuffer = new ListBuffer[Int]

    val eff = (for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(2)
      )
    } yield a).flatMap { a =>
      addLast[R](
        listBuffer.append(3)
      ).map(_ => a)
    }

    val actual = Eff.run(runEval(eff).runLast)

    assert(actual === 1)
    assert(listBuffer.toList === List(1, 2, 3))
  }

  it should "run last actions at the end of Eff even if `runLast` is not the last execution" in {
    import org.atnos.eff.EvalEffect._
    type R = Fx.fx2[LastAction, Eval]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(3)
      )
      b <- delay[R, Int] {
        listBuffer.append(2)
        2
      }
    } yield a + b

    val actual = Eff.run(runEval(eff).runLast)

    assert(actual === 3)
    assert(listBuffer.toList === List(1, 2, 3))
  }

  it should "run last actions which are applicative composed" in {
    import org.atnos.eff.EvalEffect._
    type R = Fx.fx2[LastAction, Eval]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(3)
      ) <* addLast[R](
        listBuffer.append(4)
      ) <* addLast[R](
        listBuffer.append(5)
      )
      b <- delay[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(6)
      ) *> addLast[R](
        listBuffer.append(7)
      )
    } yield a + b

    val actual = Eff.run(runEval(eff).runLast)

    assert(actual === 3)
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6, 7))
  }

  "runLastDefer" should "execute effects by reverse order" in {
    import org.atnos.eff.EvalEffect._

    type R = Fx.fx2[Eval, LastAction]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(6)
      )
      b <- delay[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(5)
      )
      c <- delay[R, Int] {
        listBuffer.append(3)
        3
      }
      _ <- addLast[R](
        listBuffer.append(4)
      )
    } yield a + b + c

    assert(listBuffer.toList === Nil)

    val actual = Eff.run(runEval(eff).runLastDefer)

    assert(actual === 6)
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6))
  }

  it should "run by reverse order even if last actions are added by applicative composed" in {
    import org.atnos.eff.EvalEffect._
    type R = Fx.fx2[LastAction, Eval]

    val listBuffer = new ListBuffer[Int]

    val eff = for {
      a <- delay[R, Int] {
        listBuffer.append(1)
        1
      }
      _ <- addLast[R](
        listBuffer.append(7)
      ) <* addLast[R](
        listBuffer.append(6)
      ) <* addLast[R](
        listBuffer.append(5)
      )
      b <- delay[R, Int] {
        listBuffer.append(2)
        2
      }
      _ <- addLast[R](
        listBuffer.append(4)
      ) *> addLast[R](
        listBuffer.append(3)
      )
    } yield a + b

    val actual = Eff.run(runEval(eff).runLastDefer)

    assert(actual === 3)
    assert(listBuffer.toList === List(1, 2, 3, 4, 5, 6, 7))
  }
}
