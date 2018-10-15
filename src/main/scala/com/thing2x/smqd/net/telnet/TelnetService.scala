package com.thing2x.smqd.net.telnet

import java.util.Properties

import bsh.Interpreter
import com.thing2x.smqd.plugin.Service
import com.thing2x.smqd.{SmqSuccess, Smqd}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import net.wimpi.telnetd.TelnetD

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

// 10/13/18 - Created by Kwon, Yeong Eon

/**
  *`  `
  */
class TelnetService(name: String, smqd: Smqd, config: Config) extends Service(name, smqd, config) with StrictLogging {

  private var telnetD: TelnetD = _

  override def start(): Unit = {
    val properties: Properties = defaultProperties

    config.getConfig("telnet").entrySet.asScala.foreach{ entry =>
      val k = entry.getKey
      val v = entry.getValue.render
      properties.setProperty(k, v)
    }

    properties.asScala.foreach( s => logger.trace(s"telnetd property: ${s._1} = ${s._2}") )
    telnetD = TelnetD.createTelnetD(properties)

    val bshShell = telnetD.getShellManager.getShell("bsh").asInstanceOf[BshShell]
    bshShell.setDelegate(new BshShellDelegate(){
      // shell env에 smqd instance를 지정한다.
      override def prepare(shell: BshShell, bshInterpreter: Interpreter): Unit = {
        bshInterpreter.set("SMQD", smqdInstance)
      }
      // bsh file들을 찾을 bsh directory 경로 path를 지정한다.
      override def scriptPaths(shell: BshShell): Array[String] = {
        config.getStringList("script.path").asScala.toArray
      }
    })

    val loginShell = telnetD.getShellManager.getShell("login").asInstanceOf[LoginShell]
    // login 인증을 UserDelegate로 위임한다.
    loginShell.setDelegate((user: String, password: String) => {
      Await.result(smqd.userLogin(user, password), 3.seconds) match {
        case SmqSuccess(_) => true
        case _ => false
      }
    })

    telnetD.start()
  }

  override def stop(): Unit = {
    if (telnetD != null)
      telnetD.stop()
    telnetD = null
  }

  private def defaultProperties: Properties = {
    val p = new Properties()

    //////////////////////////////////////////////////
    // Terminals
    //////////////////////////////////////////////////

    // List of terminals available and defined below
    p.setProperty("terminals", "vt100,ansi,windoof,xterm")

    // vt100 implementation and aliases
    p.setProperty("term.vt100.class", "net.wimpi.telnetd.io.terminal.vt100")
    p.setProperty("term.vt100.aliases", "default,vt100-am,vt102,dec-vt100")

    // ansi implementation and aliases
    p.setProperty("term.ansi.class", "net.wimpi.telnetd.io.terminal.ansi")
    p.setProperty("term.ansi.aliases", "color-xterm,vt320,vt220,linux,screen")

    // windoof implementation and aliases
    p.setProperty("term.windoof.class", "net.wimpi.telnetd.io.terminal.Windoof")
    p.setProperty("term.windoof.aliases", "")

    // xterm implementation and aliases
    p.setProperty("term.xterm.class", "net.wimpi.telnetd.io.terminal.xterm")
    p.setProperty("term.xterm.aliases", "")

    //////////////////////////////////////////////////
    // Shells
    //////////////////////////////////////////////////

    // List of shells available and defined below
    p.setProperty("shells", "login,bsh,dummy")

    // shell implementations
    p.setProperty("shell.login.class", "com.thing2x.smqd.net.telnet.LoginShell")
    p.setProperty("shell.bsh.class",   "com.thing2x.smqd.net.telnet.BshShell")
    p.setProperty("shell.dummy.class", "com.thing2x.smqd.net.telnet.DummyShell")

    //////////////////////////////////////////////////
    // Listeners
    //////////////////////////////////////////////////

    p.setProperty("listeners", "default_listener") // comma seperated listener names

    // std listener specific properties
    //Basic listener and connection management settings
    p.setProperty("default_listener.port", "6621")
    p.setProperty("default_listener.floodprotection", "5")
    p.setProperty("default_listener.maxcon", "25")

    // Timeout Settings for connections (ms)
    p.setProperty("default_listener.time_to_warning", "3600000")
    p.setProperty("default_listener.time_to_timedout", "60000")

    // Housekeeping thread active every 1 secs
    p.setProperty("default_listener.housekeepinginterval", "1000")
    p.setProperty("default_listener.inputmode", "character")

    // Login shell
    p.setProperty("default_listener.loginshell", "login")

    // Connection filter class
    p.setProperty("default_listener.connectionfilter", "none")
    p
  }
}