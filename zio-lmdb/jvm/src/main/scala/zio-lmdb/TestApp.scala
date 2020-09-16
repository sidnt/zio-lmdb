package zio.lmdb

import zio._
import zio.blocking._
import console._
import DefaultMdb._

object TestApp extends App {

  val l1:ZLayer[Any,Nothing,MdbEnvConfig] =                         MdbEnvConfig.defaultEnvConfigL
  val l2:ZLayer[Any,Nothing,Blocking with MdbEnvConfig] =           Blocking.live ++ l1
  val l3:ZLayer[Blocking with MdbEnvConfig,Throwable,MdbEnvP] =     MdbEnvP.managedMdbEnvPServiceLayer2
  val l4:ZLayer[Any,Throwable,MdbEnvP] =                            l2 >>> l3
  val l5:ZLayer[MdbEnvP, Throwable, DefaultDbiP] =                  DefaultDbiP.defaultDbiL
  val l6:ZLayer[Any, Throwable, DefaultDbiP] =                      l4 >>> l5
  val l7:ZLayer[Any, Throwable, MdbEnvP with DefaultDbiP] =         l4 ++ l6
  val l8:ZLayer[MdbEnvP with DefaultDbiP, Throwable, DefaultMdb] =  DefaultMdb.defaultMdbLPure
  val l9:ZLayer[Any,Throwable,DefaultMdb] =                         l7 >>> l8
  val la = console.Console.live ++ l9

  import byteHelpers._

  val testApp:ZIO[DefaultMdb with Console, Throwable, Unit] = for {
    dbs  <- ZIO.access[DefaultMdb](identity)
    db   =  dbs.get
    kbb =  stringToUtf8DByteBuffer("alargerkeyhere")
    b   <- db.put(kbb,stringToUtf8DByteBuffer("alargervaluehere"))
    _   <- putStrLn("got " + b)
    vbb <- db.get(kbb)
    _   <- putStrLn("got back " + dByteBufferToUtf8String(vbb))
  } yield ()

  val testAppRunnable = testApp.provideLayer(la).exitCode

  def run(args: List[String]): zio.URIO[zio.ZEnv,ExitCode] = testAppRunnable

}
