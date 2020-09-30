package zio.lmdb

import zio._
import console._
import clock._
object try3 extends App {

  val p0 = for {
    time  <- currentDateTime.map(_.toLocalTime)
    _     <- putStrLn(s"[$time] bye")
  } yield ()

  def run(args: List[String]): URIO[ZEnv,ExitCode] = p0.exitCode
  
}
