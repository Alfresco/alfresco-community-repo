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
package org.alfresco.web.bean.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.IHandler;
import org.alfresco.web.bean.rules.handlers.BaseConditionHandler;
import org.alfresco.web.bean.rules.handlers.CompositeConditionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CreateCompositeRuleWizard extends CreateRuleWizard
{
   private static final Log logger = LogFactory.getLog(CreateCompositeRuleWizard.class);

   private List<SelectItem> compositeConditions;

   // Right now the UI only supports one level of recursion, although the
   // backend supports unlimited recursion for composites.
   // This limitation is introduced by the fact that we are have two "current"
   // conditions - either normal condition, or composite conditions
   // basically, the UI will have to store conditions in a more native way
   // (instead of DataModel) to get unlimited number of composite conditions recursing
   protected DataModel currentCompositeConditionsDataModel;
   protected List<Map<String, Serializable>> currentCompositeConditionPropertiesList;

   private boolean addingCompositeCondition;
   private boolean editCurrentCompositeCondition;
   
   private int rowIndex = -1;
   
   
   public CreateCompositeRuleWizard()
   {
   }

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.setAddingCompositeCondition(false);
      this.currentCompositeConditionsDataModel = null;
      this.currentCompositeConditionPropertiesList = null;
   }

   public void setupCompositeConditionsMode()
   {
      this.setAddingCompositeCondition(true);
      this.currentCompositeConditionsDataModel = new ListDataModel();
      this.currentCompositeConditionPropertiesList = new ArrayList<Map<String, Serializable>>();
   }

   private void clearCompositeConditionMode()
   {
      // reset the action drop down
      this.selectedCondition = null;
      // this.currentConditionProperties = null;
      this.currentCompositeConditionsDataModel = null;
      this.currentCompositeConditionPropertiesList = null;
      this.setAddingCompositeCondition(false);
   }

   /**
    * Returns the properties for all the conditions as a JSF DataModel
    * 
    * @return JSF DataModel representing the condition properties
    */
   public DataModel getAllCompositeConditionsDataModel()
   {
      if (this.currentCompositeConditionsDataModel == null)
      {
         this.currentCompositeConditionsDataModel = new ListDataModel();
      }

      this.currentCompositeConditionsDataModel.setWrappedData(this.currentCompositeConditionPropertiesList);

      return this.currentCompositeConditionsDataModel;
   }

   /**
    * Displays the settings page for the current condition being added (if
    * required)
    */
   @Override
   public void promptForConditionValues()
   {
      if (CompositeConditionHandler.NAME.equals(this.selectedCondition))
      {
         setupCompositeConditionsMode();
      }
      
      super.promptForConditionValues();
   }

   public void finishAddingCompositeCondition()
   {
      if (logger.isDebugEnabled())
         logger.debug("Finishing Adding Composite Condition.");

      // reset the effective chosen condition to composite-condition
      this.selectedCondition = CompositeConditionHandler.NAME;

      FacesContext context = FacesContext.getCurrentInstance();
      returnViewId  = getWizardContainerViewId(context);

      printConditionState();

      if (logger.isDebugEnabled())
         logger.debug("\tAdding Condition '" + selectedCondition + "'");

      IHandler handler = this.conditionHandlers.get(this.selectedCondition);

      // creating object temporarily so we can pass it to CompositeConditionHandler
      currentConditionProperties.put(CompositeConditionHandler.PROP_COMPOSITE_CONDITION,
            (Serializable) this.currentCompositeConditionPropertiesList);
      currentConditionProperties.put(PROP_CONDITION_NAME, this.selectedCondition);

      // this is called from the actions page so there must be a handler
      // present so there's no need to check for null
      String summary = handler.generateSummary(context, this, currentConditionProperties);

      if (summary != null)
      {
         currentConditionProperties.put(PROP_CONDITION_SUMMARY, summary);
      }
      if (logger.isDebugEnabled())
         logger.debug("Generated Summary - [" + summary + "] + selectedCondition " + this.selectedCondition);
      
      if (this.editCurrentCompositeCondition == true)
      {
         if (rowIndex != -1)
         {
            this.allConditionsPropertiesList.remove(rowIndex);
            this.allConditionsPropertiesList.add(rowIndex, currentConditionProperties);
         }
      }
      else
      {
         this.allConditionsPropertiesList.add(currentConditionProperties);
      }
      
      this.editCurrentCompositeCondition = false;
      this.rowIndex = -1;
      clearCompositeConditionMode();
      // refresh the wizard
      goToPage(context, this.returnViewId);
   }

   private String getWizardContainerViewId(FacesContext context)
   {
      String viewId = null;
      ConfigService configSvc = Application.getConfigService(context);
      Config globalConfig = configSvc.getGlobalConfig();
      if (globalConfig != null)
      {
         viewId = globalConfig.getConfigElement("wizard-container").getValue();
      } 
      else
      {
         logger.error("plain-dialog-container configuraion setting is not found");
      }
      return viewId;
   }

   @Override
   public void cancelAddCondition()
   {
      this.editCurrentCompositeCondition = false;
      this.rowIndex = -1;
      if (isAddingCompositeCondition())
      {
         // don't clear when editing, since we are looking at a REFERENCE to an existing condition
         if (this.editingCondition == false) 
         {
            this.currentConditionProperties.clear();
         }
         
         // reset the action drop down
         this.selectedCondition = null;
         
         // determine what page to go back to
         FacesContext context = FacesContext.getCurrentInstance();
         String currentViewId = context.getViewRoot().getViewId();
         
         IHandler handler = this.conditionHandlers.get(CompositeConditionHandler.NAME);
         String compositePage = handler.getJSPPath();
         
         if (currentViewId.equals(compositePage))
         {
            this.returnViewId = getWizardContainerViewId(context);
         }

         goToPage(FacesContext.getCurrentInstance(), this.returnViewId);
      } 
      else
      {
         super.cancelAddCondition();
         return;
      }
   }

   /**
    * Adds the condition just setup by the user to the list of composite
    * conditions This gathers the composite conditions in the
    */
   @Override
   public void addCondition()
   {
      if (!isAddingCompositeCondition())
      {
         super.addCondition();
         printConditionState();
         return;
      }

      if (logger.isDebugEnabled())
         logger.debug("Adding Condition to Composite Condition. ");

      FacesContext context = FacesContext.getCurrentInstance();

      // this is called from the actions page so there must be a handler
      // present so there's no need to check for null
      IHandler handler = this.conditionHandlers.get(this.selectedCondition);
     
      if (handler != null) 
      {
         String summary = handler.generateSummary(context, this, this.currentConditionProperties);
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, summary);
      } 
      else
      {
         if (logger.isWarnEnabled())
            logger.warn("No Summary could be generated for rule condition " + this.selectedCondition);
         
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, "ERROR - No Summary for " + this.selectedCondition);
      }

      if (editingCondition == false)
      {
         //this check is needed to prevent an condition when you are editing a composite, to prevent it from adding twice
         currentCompositeConditionPropertiesList.add(this.currentConditionProperties);
         if (logger.isDebugEnabled())
            logger.debug("\tAdded condition to Composite condition.");
      } 
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("\tEdited composite condition. ");
      }

      this.currentConditionProperties = new HashMap<String, Serializable>(3);
      
      // resetting it for composite condition
      // TODO: this is not persistent currently, which causes a minor bug
      this.currentConditionProperties.put(PROP_CONDITION_NAME, CompositeConditionHandler.NAME);
      this.currentConditionProperties.put(BaseConditionHandler.PROP_CONDITION_NOT, Boolean.FALSE);

      // reset the action drop down
      this.selectedCondition = null;
      
      // refresh the wizard
      printConditionState();
      goToPage(context, this.returnViewId);
   }

   /**
    * Sets up the context for editing existing composite condition values
    */
   @SuppressWarnings("unchecked")
   public void editCondition()
   {
      this.editingCondition = true;

      if (logger.isDebugEnabled())
         logger.debug("Editing Conditions.  isAddingCompositeConditions - " + isAddingCompositeCondition());

      //if user is on main conditions screen, check if the condition to be edited is a composite condition
      if (!isAddingCompositeCondition())
      {
         Map condition = (Map) this.allConditionsDataModel.getRowData();
         this.rowIndex = this.allConditionsDataModel.getRowIndex();
         this.editCurrentCompositeCondition = true;
         
         if (condition.get(PROP_CONDITION_NAME).equals(CompositeConditionHandler.NAME))
         {
            logger.debug("Composite Condition selected, enabling CompositeCondition Mode");
            currentCompositeConditionPropertiesList = (List<Map<String, Serializable>>) condition
                  .get(CompositeConditionHandler.PROP_COMPOSITE_CONDITION);
            addingCompositeCondition = true;
         }
         super.editCondition(condition );
         return;
      } 
      else 
      {
         Map subCondition = (Map) currentCompositeConditionsDataModel.getRowData();
         super.editCondition(subCondition);
      }
   }
   
   public List<SelectItem> getCompositeConditions()
   {
      if (this.compositeConditions == null)
      {
         this.compositeConditions = new ArrayList<SelectItem>();
         List<SelectItem> tempConditions = this.getConditions(); // loads up the conditions
         for (SelectItem item : tempConditions)
         {
            if (!((item.getValue().equals(CompositeConditionHandler.NAME)) || (item.getValue().equals("no-condition"))))
            {
               this.compositeConditions.add(item);
            }
         }
      }
      
      return this.compositeConditions;
   }

   protected boolean isAddingCompositeCondition()
   {
      return addingCompositeCondition;
   }

   protected void setAddingCompositeCondition(boolean addingCompositeCondition)
   {
      if (logger.isDebugEnabled())
         logger.debug("Setting addingCompositeCondition to " + addingCompositeCondition);
      
      this.addingCompositeCondition = addingCompositeCondition;
   }

   /**
    * Removes the requested condition from the list
    */
   public void removeCondition()
   {
      if (!isAddingCompositeCondition())
      {
         super.removeCondition();
         return;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Removing Composite Conditions");

      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map conditionToRemove = (Map) this.currentCompositeConditionsDataModel.getRowData();
      this.currentCompositeConditionPropertiesList.remove(conditionToRemove);
      // reset the action drop down
      this.selectedCondition = null;

      // refresh the wizard
      FacesContext context = FacesContext.getCurrentInstance();
      goToPage(context, context.getViewRoot().getViewId());
   }

   protected void printConditionState()
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("\t\t*** GLOBAL ***");

         logger.debug("\t\tallConditionsProperties");
         if (allConditionsPropertiesList == null)
         {
            logger.debug("\t\t\tempty");
         }
         else
         {
            for (Object obj : allConditionsPropertiesList)
            {
               logger.debug("\t\t\t" + obj.toString());
            }
         }

         logger.debug("\t\t*** COMPOSITE ***");
         logger.debug("\t\taddingCompositeCondition " + addingCompositeCondition);

         logger.debug("\t\tcurrentCompositeConditionsProperties");
         if (currentCompositeConditionPropertiesList == null)
         {
            logger.debug("\t\t\t EMPTY");
         }
         else
         {
            int i = 1;
            for (Map<String, Serializable> cond : currentCompositeConditionPropertiesList)
            {
               logger.debug("\t\t\tCondition" + i++);

               for (String key : cond.keySet())
               {
                  logger.debug("\t\t\t\tkey - {" + key + "} value - {" + cond.get(key) + "}");
               }
            }
         }

         logger.debug("\t\t*** BOTH ***");

         logger.debug("\t\tcurrentConditionsProperties");
         if (currentConditionProperties == null)
         {
            logger.debug("\t\t\t EMPTY");
         } 
         else
         {
            for (String key : this.currentConditionProperties.keySet())
            {
               logger.debug("\t\t\tkey - {" + key + "} value - {" + this.currentConditionProperties.get(key) + "}");
            }
         }
      }
   }
}
