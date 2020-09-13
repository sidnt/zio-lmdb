package zio.lmdb

import zio._

object MdbEnvConfig {
  type MdbEnvConfig = Has[Service]

  trait Service {
    val envDirPath: String
    val envMaxMiBs: Int
  }

  val envDirPath = ZIO.access[MdbEnvConfig](_.get.envDirPath)
  val envMaxMiBs = ZIO.access[MdbEnvConfig](_.get.envMaxMiBs)

  /** defaultEnvConfigL is 1 possible instance of
    * an MdbEnvConfig wrapped in a ULayer
    * .
    * this instance can be fed to other layers 
    * which require an MdbEnvConfig (ie, a Has[MdbEnvConfig.Service])
    * eg, a downstream layer that needs these configuration
    * to construct a layer that can provide a managedMdbEnvHandle downstream services
    * .
    * succeed constructor internally wraps the supplied instance
    * in a Has type.
    * .
    * so to read it,
    * defaultEnvConfigL is a ZLayer that Has an MdbEnvConfig.Service implementation
    * */
  val defaultEnvConfigL: ULayer[MdbEnvConfig] = ZLayer.succeed {
    new MdbEnvConfig.Service {
      val envDirPath: String = "defaultEnv"
      val envMaxMiBs: Int = 10
    }
  }

}
