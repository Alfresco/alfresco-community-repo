/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
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
