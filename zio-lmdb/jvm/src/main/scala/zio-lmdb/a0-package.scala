package zio

import zio._
import zio.blocking._
import zio.nio.file._
import java.nio.ByteBuffer
import java.io.File

package object lmdb {
  
    type MdbEnvConfig = Has[MdbEnvConfig.Service]
    type MdbEnvP =      Has[MdbEnvP.Service]
    type DefaultDbiP =  Has[DefaultDbiP.Service]
    type DefaultDbi =   Has[DefaultDbi.Service]

    type BB = ByteBuffer
    
    object fsHelpers {

        def createOrGetDirPathFile(dirPath: String): ZIO[Blocking, Throwable, File] = for {
            dirPath          <- effectBlocking(FileSystem.default.getPath(dirPath))
            dirPathExists    <- Files.exists(dirPath)
            _                <- if(!dirPathExists) Files.createDirectory(dirPath) else IO.unit
            dirPathFile      =  dirPath.toFile
        } yield dirPathFile
  
    }

    object byteHelpers {
        import java.nio.charset.StandardCharsets.UTF_8
        
        /** 
         * we need the ability of interconversion between
          * runtime objects <-> database objects
          * */

        /** 
         * #doubt where will be dbb allocated? in the method's stack?
          * will it need GC later? Or will it be wiped after this method returns?
          * */
        
        def byteArrayToDByteBuffer(ba: Array[Byte]): ByteBuffer = {
            val dbb: ByteBuffer = ByteBuffer.allocateDirect(ba.length).put(ba)
            dbb.flip()
            dbb
            /** #doubt is ↑ concurrent safe?
             * seems so, because it doesn't access any value outside itself
             * even if it mutates something `dbb.flip()` the values affected are local to the method
             * each executing instance will have its own copy
             * and won't be working with any shared state. so looks like fiber safe? */
        }

        /** these apis are #hardcoded to the UTF_8 Charset */
        def stringToUtf8DByteBuffer(s:String): ByteBuffer = byteArrayToDByteBuffer(s.getBytes(UTF_8))
        def dByteBufferToUtf8String(bb:ByteBuffer): String = UTF_8.decode(bb).toString
    }
    
}
