/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
   
   /*
    * @see org.alfresco.web.bean.actions.IHandler#isAllowMultiple()
    */
   public boolean isAllowMultiple()
   {
      return true;
   }
}
