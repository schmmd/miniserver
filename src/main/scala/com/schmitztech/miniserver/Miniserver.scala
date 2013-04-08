package com.schmitztech.miniserver

import java.io.File
import scala.Array.canBuildFrom
import scala.io.Source
import resource.managed
import scopt.immutable.OptionParser
import unfiltered.filter.Intent
import unfiltered.jetty.Http
import unfiltered.request.GET
import unfiltered.request.Path
import unfiltered.request.Seg
import unfiltered.response.NotFound
import unfiltered.response.Ok
import unfiltered.response.ResponseString
import org.slf4j.LoggerFactory
import unfiltered.filter.Planify

object Miniserver extends App {
  val logger = LoggerFactory.getLogger(this.getClass)

  case class Config(port: Int = 8080, base: File = new File(""))

  val argumentParser = new OptionParser[Config]("miniserver") {
    def options = Seq(
      arg("input-path", "input path for server") { (path: String, config: Config) =>
        val file = new File(path)
        require(file.exists, "File does not exist: " + path)
        config.copy(base = file.getAbsoluteFile)
      },
      intOpt("p", "port", "output file (otherwise stdout)") { (port: Int, config: Config) =>
        config.copy(port = port)
      })
  }

  argumentParser.parse(args, Config()) match {
    case Some(config) => run(config)
    case None =>
  }

  def run(configuration: Config) {
    object Plan extends unfiltered.filter.Plan {
      def intent = Intent {
        case req @ GET(Path(Seg(Nil))) if configuration.base.exists =>
          respond(configuration.base)
        case req @ GET(Path(path)) if new File(configuration.base, path).exists =>
          respond(new File(configuration.base, path))
      }

      def respond(file: File) = {
        file match {
          case file if file.isDirectory =>
            logger.info("Serving directory: " + file)
            val response = file.listFiles.map { subfile =>
              val address = subfile.getAbsolutePath.drop(configuration.base.getPath.length)
              s"<a href='$address'>$address</a>"
            }.mkString("<br>\n")
            ResponseString(response) ~> Ok
          case file if file.isFile =>
            val response = for (source <- managed(Source.fromFile(file))) yield {
              source.getLines.mkString("\n")
            }

            response.opt match {
              case Some(response) =>
                logger.info("Serving file: " + file)
                ResponseString(response) ~> Ok
              case None =>
                logger.error("Could not read file: " + file)
                ResponseString("Could not read: " + file) ~> NotFound
            }
          case _ =>
            logger.error("File not found: " + file)
            ResponseString("Could not serve: " + file) ~> NotFound
        }
      }
    }

    println("starting...")
    try {
      Http(configuration.port).filter(Plan).run()
    } catch {
      case e: java.net.BindException => println("Address already in use: " + configuration.port); System.exit(1)
    }

  }
}
