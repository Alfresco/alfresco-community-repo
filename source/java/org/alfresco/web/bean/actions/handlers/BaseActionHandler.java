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
