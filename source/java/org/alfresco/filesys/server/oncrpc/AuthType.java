/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.oncrpc;

/**
 * Authentication Types Class
 * 
 * @author GKSpencer
 */
public final class AuthType {

  //	Authentication type contants
  
  public static final int Null		= 0;
  public static final int Unix		= 1;
  public static final int Short		= 2;
  public static final int DES		= 3;

  //	Authentication type strings
  
  private static final String[] _authTypes = { "Null", "Unix", "Short", "DES" };
  
  /**
   * Return the authentication type as string
   *
   * @param type int
   * @return String
   */
  public static final String getTypeAsString(int type) {
    if ( type < 0 || type >= _authTypes.length)
      return "" + type;
    return _authTypes[type];
  }
}
