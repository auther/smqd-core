/*
	@Begin
	list current directory
	
	Syntax
		ls
	@End
 */

import java.io.File

import com.thing2x.smqd.net.telnet.ScShell
import com.thing2x.smqd.util.{StringUtil, TimeUtil}
import net.wimpi.telnetd.io.BasicTerminalIO

val args: Array[String] = $args
val shell: ScShell = $shell

def fileInfo(f: File): Unit = {
	val sb = new StringBuffer()
	
	if (f.isDirectory) sb.append("d")
	else sb.append("-")
	
	if (f.canRead) sb.append("r")
	else sb.append("-")
	
	if (f.canWrite) sb.append("w")
	else sb.append("-")
	
	if (f.isDirectory || f.getName.endsWith(".sc")) sb.append("x")
	else sb.append("-")

	sb.append("------")
	sb.append(" ")
  sb.append(f"${f.length()}%7d ")
	sb.append(new TimeUtil(f.lastModified).format(TimeUtil.SIMPLE)).append(" ")
  print(sb.toString)

  if (f.isDirectory) {
    shell.termIO.setForegroundColor(BasicTerminalIO.BLUE)
    shell.termIO.setBold(true)
    print(f.getName)
    shell.termIO.setForegroundColor(BasicTerminalIO.WHITE)
    shell.termIO.setBold(false)
  }
  else {
    print(f.getName)
  }
  println()
}

def getFileList: Seq[File] = {
  var off = 1
  var loop = true
  do {
    if (off >= args.length || !args(off).startsWith("-"))
      loop = false
    else
      off += 1
  } while (loop)

  val fileArgs = args.drop(off)

  if (fileArgs.isEmpty) {
    val f = new File(shell.getRealPath(shell.getWorkingDirectory))
    f.listFiles()
  }
  else {
    fileArgs.flatMap{ pt =>
      val f = new File(shell.getRealPath(shell.getWorkingDirectory))
      val arr = f.listFiles()
      if (StringUtil.isWildExp(pt)) {
        arr.filter(f => StringUtil.compareWildExp(f.getName, pt) == 0)
      }
      else {
        arr.filter(f => f.getName == pt)
      }
    }
  }
}

//
// diplay file list
//
val result = getFileList

if (result.isEmpty){
	print("File not found.");
}
else {
  result.foreach( fileInfo )
}

