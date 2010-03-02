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
package org.alfresco.web.bean.rules.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.ActionModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wizard.IWizardBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jean Barmash
 * This class has no equivalent evaluator, since it
 *
 */
public class CompositeConditionHandler extends BaseConditionHandler 
{
   public static final String NAME = "composite-condition";
   public static final String PROP_COMPOSITE_CONDITION = "composite-condition";

   private static final Log logger = LogFactory.getLog(CompositeConditionHandler.class);

   public static final String PROP_CONDITION_OR = "orconditions";
   
   /* (non-Javadoc)
    * @see org.alfresco.web.bean.actions.IHandler#generateSummary(javax.faces.context.FacesContext, org.alfresco.web.bean.wizard.IWizardBean, java.util.Map)
    */
   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> props) 
   {      
       List<Object> conditionPropertiesList = (List<Object>) props.get(PROP_COMPOSITE_CONDITION);
       if (conditionPropertiesList == null ) 
       {
          logger.error("Error - composite condition has no sub-conditions");
           
          if (logger.isDebugEnabled())
          {
             for (String str : props.keySet()) 
             {
                logger.debug("\t key - "+ str + " value " + props.get(str));
             }
          }
          
          return Application.getMessage(context, "condition_composite_error");
       }
       
       Boolean not = (Boolean) props.get(PROP_CONDITION_NOT);
      Boolean orconditions = (Boolean) props.get(PROP_CONDITION_OR);

       String msgId = not.booleanValue() ? "condition_composite_summary_not" : "condition_composite_summary";
      return MessageFormat.format(Application.getMessage(context, msgId),
                           new Object[] {conditionPropertiesList.size(), orconditions.booleanValue()?"ORed":"ANDed"});
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.actions.IHandler#getJSPPath()
    */
   public String getJSPPath() 
   {
      return getJSPPath(CompositeConditionHandler.NAME);
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.actions.IHandler#prepareForEdit(java.util.Map, java.util.Map)
    */
   public void prepareForEdit(Map<String, Serializable> uiConditionProps,
               Map<String, Serializable> repoProps) 
   {
      if (logger.isDebugEnabled())
         logger.debug("Preparing Composite Condition for Edit");

      Boolean orconditions = (Boolean) repoProps.get(PROP_CONDITION_OR);
      if (orconditions == null) 
      {
         if (logger.isWarnEnabled())
            logger.warn("orconditions is NULL, it should not be.  Defaulting to false");
         
          orconditions = Boolean.FALSE;
      }
      
      uiConditionProps.put(PROP_CONDITION_OR, orconditions);
   }

   public void prepareForSave(Map<String, Serializable> uiConditionProps,
        Map<String, Serializable> repoProps) 
   {
      if (logger.isDebugEnabled())
         logger.debug("Saving Composite Condition");
      
      // put the selected category in the condition params
      Boolean orconditions = (Boolean)uiConditionProps.get(PROP_CONDITION_OR);
      if (orconditions == null) 
      {
         if (logger.isWarnEnabled())
            logger.warn("orconditions is NULL, it should not be.  Defaulting to false");
         
          orconditions = Boolean.FALSE;
      }
      
      repoProps.put(PROP_CONDITION_OR, orconditions);
   }
}
