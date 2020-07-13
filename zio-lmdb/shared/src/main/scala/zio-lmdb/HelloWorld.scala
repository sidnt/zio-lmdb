package zio.lmdb

import zio._
import zio.App
import zio.console._

object HelloWorld extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => ExitCode(1), _ => ExitCode(0))

  val myAppLogic =
    for {
      _    <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hello, $name, welcome to ZIO!")
    } yield ()
}


object HelloScala extends scala.App {
  val x = println("hi")
  println(x)
}
