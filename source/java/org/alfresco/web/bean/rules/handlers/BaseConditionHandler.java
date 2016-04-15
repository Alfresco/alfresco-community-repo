package org.alfresco.web.bean.rules.handlers;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.bean.actions.IHandler;
import org.alfresco.web.bean.repository.Repository;

/**
 * Base class for all condition handler implementations.
 * 
 * @author gavinc
 */
public abstract class BaseConditionHandler implements IHandler
{
   protected static final String CONDITION_PAGES_LOCATION = "/jsp/rules/";
   public static final String PROP_CONDITION_NOT = "notcondition";
   
   public void setupUIDefaults(Map<String, Serializable> conditionProps)
   {
      // do nothing by default, only those condition handlers that need
      // to setup defaults need override this method
   }
   
   /**
    * Given the condition name, generates the default path for the JSP
    * 
    * @param conditionName The name of the condition
    * @return The path to the JSP used for the condition
    */
   protected String getJSPPath(String conditionName)
   {
      return CONDITION_PAGES_LOCATION + conditionName + ".jsp";
   }
   
   /**
    * Returns the NamespaceService for further retrieve a prefix strings.
    * 
    * @see org.alfresco.web.bean.rules.handlers.property.TextPropertyValueConditionHandler#prepareForEdit(Map, Map)
    * 
    * @return The NamespaseService for further usage
    */
   protected NamespaceService getNamespaceService()
   {
       return Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
   }

   /*
    * @see org.alfresco.web.bean.actions.IHandler#isAllowMultiple()
    */
   public boolean isAllowMultiple()
   {
      return true;
   }
}
