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

package com.thing2x.smqd.session

/**
  * 2018. 6. 3. - Created by Kwon, Yeong Eon
  */
trait SessionStoreDelegate {
/*
  /**
    * create new session
    * @param clientId client identifier
    * @return previous existing MqttSession
    */
  //  def createSession(clientId: String): MqttSession

  /**
    * read previously stored session
    * @param clientId client identifier
    * @return loaded session
    */
  //  def readSession(clientId: String): Option[MqttSession]

  /**
    * Remove current session that is associated with client identifier
    * @param clientId client identifier
    * @return removed MqttSession
    */
  //  def deleteSession(clientId: String): Option[MqttSession]

  /**
    * Store current session when client is disconnected
    * @param clientId client identifier
    * @param session MqttSession
    */
  //  def updateSession(clientId: String, session: MqttSession): Unit
*/
}

class SessionStore(delegate: SessionStoreDelegate) {

}