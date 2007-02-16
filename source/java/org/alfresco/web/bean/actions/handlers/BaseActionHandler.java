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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.web.bean.actions.IHandler;

/**
 * Base class for all action handler implementations.
 * 
 * @author gavinc
 */
public abstract class BaseActionHandler implements IHandler
{
   protected static final String ACTION_PAGES_LOCATION = "/jsp/actions/";
   protected static final String PROP_DESTINATION = "destinationLocation";
   
   public void setupUIDefaults(Map<String, Serializable> actionProps)
   {
      // do nothing by default, only those action handlers that need
      // to setup defaults need override this method
   }
   
   /**
    * Given the action name, generates the default path for the JSP
    * 
    * @param actionName The name of the action
    * @return The path to the JSP used for the action
    */
   protected String getJSPPath(String actionName)
   {
      return ACTION_PAGES_LOCATION + actionName + ".jsp";
   }
}
