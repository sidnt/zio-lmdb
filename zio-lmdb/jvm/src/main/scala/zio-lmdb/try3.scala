package zio.lmdb
import Err._

import zio._
import console._
import clock._
import blocking._
import org.lmdbjava.Env
import java.nio.ByteBuffer
import org.lmdbjava.Dbi

object try3 extends App {





  type BB = ByteBuffer
  val reasonUnknown = "Reason unknown."
  def consoleLog(m:String) = (for {
    time  <- currentDateTime.map(_.toLocalTime)
    _     <- putStrLn(s"[$time] $m")
  } yield ()) orElse putStrLn(s"consoleLog($m) failed")






  object envConfig {
    type EnvConfig = Has[envConfig.Service]
    trait Service {
      val envDirPath: String
      val envMaxMiBs: Int
    }
    val getEnvConfigService = ZIO.access[EnvConfig](_.get)
    val defaultEnvConfig: ZLayer[Any, Nothing, EnvConfig] = ZLayer.succeed(
      new envConfig.Service {
        val envDirPath: String = "defaultEnv"
        val envMaxMiBs: Int = 10
      }
    )
  }; import envConfig._






  object envHandle {
    type EnvHandle = Has[envHandle.Service]
    trait Service {
      val envHandle: Env[BB]
    }
    val getEnvHandle = ZIO.access[EnvHandle](_.get.envHandle)
    def f(ecs: envConfig.Service): ZManaged[Console with Clock with Blocking, Err, envHandle.Service] = ZManaged.make(
      for {
        edfp  <- fsHelpers.createOrGetDirPathFile(ecs.envDirPath)
        _     <- consoleLog(s"attempting to open env: ${ecs.envDirPath}") 
        eh    <- Task(Env.open(edfp, ecs.envMaxMiBs)) orElse IO.fail(EnvOpeningFailed(reasonUnknown))
        _     <- consoleLog(s"succeeded in opening env, got: ${eh}") 
        ehs   = new envHandle.Service { val envHandle = eh }
      } yield ehs
    )( ehs => for {
        _   <-  consoleLog(s"attempting to close env: ${ehs.envHandle}")
        _   <-  UIO(ehs.envHandle.close()) *> consoleLog("succeeded in closing env")
      } yield ()
    )

    /* : ZLayer[Blocking with EnvConfig, Err, EnvHandle]
     * understand why is it tricky to give typing to mDefaultEnvHandle
     * */
    val mDefaultEnvHandle =
      ZLayer.fromServiceManaged(f)
  }; import envHandle._





  object dbiHandle {
    type DbiHandle = Has[dbiHandle.Service]
    trait Service {
      val dbiHandle: Dbi[BB]
    }
    val getDbiHandle = ZIO.access[DbiHandle](_.get.dbiHandle)

    val defaultDbiHandle/*: ZLayer[Console with EnvHandle, DbiOpeningFailed, DbiHandle]*/ =
      ZLayer.fromServiceM[envHandle.Service, Console with Clock, DbiOpeningFailed, dbiHandle.Service]( ehs =>
        for {
          _     <- consoleLog(s"attempting to open defaultDbi under Env ${ehs.envHandle}")
          dhs   <- Task( new dbiHandle.Service {
                          val dbiHandle: Dbi[BB] = ehs.envHandle.openDbi(null: String, org.lmdbjava.DbiFlags.MDB_CREATE)
                        }) orElse IO.fail(DbiOpeningFailed(reasonUnknown))
          _     <- consoleLog(s"succeeded in opening defaultDbi, got: ${dhs.dbiHandle}")
        } yield dhs
      )

    val defaultDbiHandle0: ZLayer[EnvHandle, DbiOpeningFailed, DbiHandle] =
      ZLayer.fromServiceM( ehs => 
        IO.effect(new dbiHandle.Service {
          val dbiHandle: Dbi[BB] = ehs.envHandle.openDbi(null: String, org.lmdbjava.DbiFlags.MDB_CREATE)
        }) orElse IO.fail(DbiOpeningFailed(reasonUnknown))
      )
  }; import dbiHandle._





  
  val l0: ULayer[ZEnv with EnvConfig] = ZEnv.live ++ defaultEnvConfig
  val l1: ZLayer[Any, Err, EnvHandle] = l0 >>> mDefaultEnvHandle
  val l2: ZLayer[Any, Err, ZEnv with EnvConfig] = l0 ++ l1 // useless

  val l3: ZLayer[Any, Err, DbiHandle] = (ZEnv.live ++ l1) >>> defaultDbiHandle
  val l4: ZLayer[Any, Err, EnvHandle with DbiHandle] = l1 ++ l3

  val p1: ZIO[ZEnv with EnvHandle with DbiHandle, Nothing, Unit] = for {
    _     <- consoleLog(s"domain logic starts here")
    envH  <- getEnvHandle
    _     <- consoleLog(s"got env handle: $envH")
    dbiH  <- getDbiHandle
    _     <- consoleLog(s"got dbi handle: $dbiH")
    _     <- consoleLog(s"domain logic ends here")
  } yield ()

  val p1p = p1.provideLayer(ZEnv.live ++ l4)
  // val p1p2 = p1.provideLayer( (ZEnv.live ++ defaultEnvConfig) )

  def run(args: List[String]): URIO[ZEnv,ExitCode] = p1p.exitCode
  
}
