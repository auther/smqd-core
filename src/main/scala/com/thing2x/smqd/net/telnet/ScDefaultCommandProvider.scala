// Copyright 2018 UANGEL
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.thing2x.smqd.net.telnet

import java.io._

import com.thing2x.smqd.net.telnet.ScDefaultCommandProvider._
import com.thing2x.smqd.util.StringUtil
import com.typesafe.scalalogging.StrictLogging
import javax.script.ScriptException

// 10/25/18 - Created by Kwon, Yeong Eon

/**
  *
  */
object ScDefaultCommandProvider {

  def apply(root: File, cwd: String, paths: Seq[String]) = new ScDefaultCommandProvider(root, cwd, paths)

  object Exit extends ScCommand {
    override val name = "exit"
    override def exe(args: Seq[String], shell: ScShell): Unit = {
      shell.printStream.println("Good bye\n")
      shell.printStream.flush()
      shell.exit(0)
    }
  }

  object History extends ScCommand {
    override val name = "history"
    override def exe(args: Seq[String], shell: ScShell): Unit = {
      val hist = shell.history
      hist.zipWithIndex.reverse.foreach{ case (h, idx) =>
        shell.printStream.println(f" ${idx + 1}%6d $h")
      }
      shell.printStream.flush()
    }
  }

  object ChangeDirectory extends ScCommand {
    override val name = "cd"
    override def exe(args: Seq[String], shell: ScShell): Unit = {
      if (args.length == 1) {
        shell.setWorkingDirectory("/")
      }
      else {
        if (shell.canAccess(args(1), true))
          shell.setWorkingDirectory(args(1))
        else
          shell.printStream.println("Access denied.")
      }
    }
  }

  class Exec(reader: Reader, cmd: String) extends ScCommand {
    override val name = "exec"
    override def exe(args: Seq[String], shell: ScShell): Unit = {
      try {
        shell.interpreter.eval(reader, cmd, args.toArray)
        shell.printStream.flush()
      } catch {
        case e: ScriptException => // throws by javax.script
          shell.printStream.println(s"Command [$cmd] has script error at line: ${e.getLineNumber} column: ${e.getColumnNumber}")
          shell.printStream.println({e.getMessage.split("\n").take(5).mkString("\n")})
        case e: Throwable =>
          shell.printStream.println(s"Command [$cmd] fail. - ${e.getClass.getName}\n")
          shell.printStream.println(e.getMessage)
          shell.printStream.println()
          shell.printStream.flush()
      }
      finally {
        reader.close()
      }
    }
  }
}


class ScDefaultCommandProvider(root: File, cwd: String, paths: Seq[String]) extends  ScCommandProvider(root, cwd) with StrictLogging {

  private val commands = Seq(Exit, History, ChangeDirectory)

  // default aliases
  aliases = "h" -> "history"
  aliases = "ll" -> "ls"

  override def command(args: Seq[String]): Option[ScCommand] = command(args.head)

  override def command(cmd: String): Option[ScCommand] = {

    def _find(fn: String, ext: String): Option[ScCommand] = {
      val fileName = if (fn.endsWith(ext)) cmd else s"$fn$ext"
      loadScriptFile(fileName) match {
        case Some(reader) =>
          Some(new Exec(reader, fileName))
        case None =>
          None
      }
    }

    val found = commands.find(_.name == cmd)
    if (found.isDefined) {
      found
    }
    else {
      _find(cmd, ".sc")
      //_find(cmd, ".sc").orElse ( _find(cmd, ".bsh").orElse( _find(cmd, ".js") ) )
    }
  }

  override def loadScriptFile(fileName: String): Option[Reader] = {
    paths.map( new File(_, fileName) ).find(_.exists) match {
      case Some(file) =>
        try {
          Some(new InputStreamReader(new FileInputStream(file)))
        }catch {
          case e : Throwable =>
            logger.warn("Failure for reading file", e)
            None
        }
      case None =>
        paths.flatMap(p => Option(getClass.getClassLoader.getResource(s"$p/$fileName"))).headOption match {
          case Some(url) => Some(new InputStreamReader(url.openStream()))
          case None => None
        }
    }
  }

  /**
    * @param pattern wild expression for script file; *.sc , abc*.sc, abc?.sc
    */
  override def findAllScriptFiles(pattern: String = "*"): Array[File] = {
    paths.map( new File(_) ).flatMap{ dir =>
      dir.listFiles(new FileFilter() {
        override def accept(file: File): Boolean = {
          if (pattern == null || "*" == pattern) true
          else StringUtil.compareCaseWildExp(file.getName, pattern) == 0
        }
      })
    }.toArray
  }

}

