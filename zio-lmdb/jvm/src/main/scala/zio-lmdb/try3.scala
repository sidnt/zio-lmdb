package zio.lmdb
import Err._

import zio._
import console._
import clock._
import blocking._
import org.lmdbjava.Env
import java.nio.ByteBuffer

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
        eh    <- consoleLog(s"attempting to open env: ${ecs.envDirPath}") *> Task(Env.open(edfp, ecs.envMaxMiBs)) orElse IO.fail(EnvOpeningFailed(reasonUnknown))
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


  
  val l0: ULayer[ZEnv with EnvConfig] = ZEnv.live ++ defaultEnvConfig
  val l1: ZLayer[Any, Err, EnvHandle] = l0 >>> mDefaultEnvHandle
  val l2: ZLayer[Any, Err, ZEnv with EnvConfig] = l0 ++ l1

  val p1: ZIO[ZEnv with EnvHandle, Nothing, Unit] = for {
    envH  <- getEnvHandle
    _     <- consoleLog(s"got env handle: $envH")
  } yield ()

  val p1p = p1.provideLayer(ZEnv.live ++ l1)
  // val p1p2 = p1.provideLayer( (ZEnv.live ++ defaultEnvConfig) )

  def run(args: List[String]): URIO[ZEnv,ExitCode] = p1p.exitCode
  
}
