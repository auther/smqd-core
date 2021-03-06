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

package com.thing2x.smqd.util

import java.text.ParseException

import akka.stream.OverflowStrategy
import com.typesafe.config._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.parser._

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.language.implicitConversions

// 2018. 8. 13. - Created by Kwon, Yeong Eon

object ConfigUtil {

  implicit val typesafeConfigEncoder: Encoder[Config] = new Encoder[Config] {
    private val RenderOptions = ConfigRenderOptions.concise().setJson(true)
    override def apply(config: Config): Json = {
      parse(config.root.render(RenderOptions)) match {
        case Right(json) => json
        case Left(_) => Json.Null
      }
    }
  }

  implicit val typesafeConfigDecoder: Decoder[Config] = new Decoder[Config] {
    private val ParseOptions = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON)
    override def apply(c: HCursor): Result[Config] = {
      Right(ConfigFactory.parseString(c.value.noSpaces, ParseOptions))
    }
  }

  implicit def configToOptionalConfig(base: Config): OptionalConfig = new OptionalConfig(base)
}

class OptionalConfig(base: Config) {
  def getOptionBoolean(path: String): Option[Boolean] =
    if (base.hasPath(path))
      Some(base.getBoolean(path))
    else
      None

  def getOptionLong(path: String): Option[Long] =
    if (base.hasPath(path))
      Some(base.getLong(path))
    else
      None

  def getOptionInt(path: String): Option[Int] =
    if (base.hasPath(path)) {
      Some(base.getInt(path))
    } else {
      None
    }

  def getOptionString(path: String): Option[String] =
    if (base.hasPath(path)) {
      Some(base.getString(path))
    } else {
      None
    }

  def getOptionStringList(path: String): Option[java.util.List[String]] =
    if (base.hasPath(path)) {
      Some(base.getStringList(path))
    }
    else {
      None
    }

  def getOptionConfig(path: String): Option[Config] =
    if (base.hasPath(path)) {
      Some(base.getConfig(path))
    } else {
      None
    }

  def getOptionConfigList(path: String): Option[java.util.List[_ <: Config]] =
    if (base.hasPath(path)) {
      Some(base.getConfigList(path))
    }
    else {
      None
    }

  def getOptionDuration(path: String): Option[FiniteDuration] =
    if (base.hasPath(path)) {
      Some(FiniteDuration(base.getDuration(path).toMillis, MILLISECONDS))
    } else {
      None
    }

  def getOverflowStrategy(path: String): OverflowStrategy =
    getOptionString(path).getOrElse("drop-new").toLowerCase match {
      case "drop-head"    => OverflowStrategy.dropHead
      case "drop-tail"    => OverflowStrategy.dropTail
      case "drop-buffer"  => OverflowStrategy.dropBuffer
      case "backpressure" => OverflowStrategy.backpressure
      case "drop-new"     => OverflowStrategy.dropNew
      case "fail"         => OverflowStrategy.fail
      case _              => OverflowStrategy.dropNew
    }
}
