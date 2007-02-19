/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.util.ParameterCheck;

/**
 * Custom config element that represents the config data for a property sheet
 * 
 * @author gavinc
 */
public class WizardsConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "wizards";

   private Map<String, WizardConfig> wizards = new LinkedHashMap<String, WizardConfig>(8, 10f);
   
   /**
    * Default constructor
    */
   public WizardsConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public WizardsConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the wizards config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      WizardsConfigElement combined = new WizardsConfigElement();
      
      // add all the wizards from this element
      for (WizardConfig wizard : this.getWizards().values())
      {
         combined.addWizard(wizard);
      }
      
      // add all the wizards from the given element
      for (WizardConfig wizard : ((WizardsConfigElement)configElement).getWizards().values())
      {
         combined.addWizard(wizard);
      }
      
      return combined;
   }
   
   /**
    * Returns the named wizard
    * 
    * @param name The name of the wizard to retrieve
    * @return The WizardConfig object for the requested wizard or null if it doesn't exist
    */
   public WizardConfig getWizard(String name)
   {
      return this.wizards.get(name);
   }
   
   /**
    * @return Returns a map of the wizards
    */
   public Map<String, WizardConfig> getWizards()
   {
      return this.wizards;
   }
   
   /**
    * Adds a wizard
    * 
    * @param wizardConfig A pre-configured wizard config object
    */
   /*package*/ void addWizard(WizardConfig wizardConfig)
   {
      this.wizards.put(wizardConfig.getName(), wizardConfig);
   }
   
   public abstract static class AbstractConfig
   {
      protected String title;
      protected String titleId;
      protected String description;
      protected String descriptionId;
      
      public AbstractConfig(String title, String titleId,
                            String description, String descriptionId)
      {
         this.title = title;
         this.titleId = titleId;
         this.description = description;
         this.descriptionId = descriptionId;
      }
      
      public String getDescription()
      {
         return this.description;
      }
      
      public String getDescriptionId()
      {
         return this.descriptionId;
      }
      
      public String getTitle()
      {
         return this.title;
      }
      
      public String getTitleId()
      {
         return this.titleId;
      }
   }
   
   /**
    * Represents the configuration of a single wizard i.e. the &lt;wizard&gt; element
    */
   public static class WizardConfig extends AbstractConfig
   {
      protected String name;
      protected String managedBean;
      protected String icon;
      protected String actionsConfigId;
      protected String errorMsgId = "error_wizard";
      
      protected Map<String, StepConfig> steps = new LinkedHashMap<String, StepConfig>(4);
      
      public WizardConfig(String name, String bean, 
                          String actionsConfigId, String icon,
                          String title, String titleId,
                          String description, String descriptionId,
                          String errorMsgId)
      {
         super(title, titleId, description, descriptionId);

         // check we have a name
         ParameterCheck.mandatoryString("name", name);
         
         this.name = name;
         this.managedBean = bean;
         this.icon = icon;
         this.actionsConfigId = actionsConfigId;
         
         if (errorMsgId != null && errorMsgId.length() > 0)
         {
            this.errorMsgId = errorMsgId;
         }
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public String getManagedBean()
      {
         return this.managedBean;
      }
      
      public String getIcon()
      {
         return this.icon;
      }
      
      public String getActionsConfigId()
      {
         return this.actionsConfigId;
      }
      
      public String getErrorMessageId()
      {
         return this.errorMsgId;
      }
      
      public int getNumberSteps()
      {
         return this.steps.size();
      }
      
      public Map<String, StepConfig> getSteps()
      {
         return this.steps;
      }
      
      public List<StepConfig> getStepsAsList()
      {
         List<StepConfig> stepList = new ArrayList<StepConfig>(this.steps.size());
         
         for (StepConfig stepCfg : this.steps.values())
         {
            stepList.add(stepCfg);
         }
         
         return stepList;
      }
      
      public StepConfig getStepByName(String name)
      {
         return this.steps.get(name);
      }
      
      /*package*/ void addStep(StepConfig step)
      {
         this.steps.put(step.getName(), step);
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override 
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" managed-bean=").append(this.managedBean);
         buffer.append(" actions-config-id=").append(this.actionsConfigId);
         buffer.append(" icon=").append(this.icon);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" errorMsgId=").append(this.errorMsgId).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Represents the configuration of a page in a wizard i.e. the &lt;page&gt; element
    */
   public static class PageConfig extends AbstractConfig
   {
      protected String path;
      protected String instruction;
      protected String instructionId;
      
      public PageConfig(String path, 
                        String title, String titleId,
                        String description, String descriptionId,
                        String instruction, String instructionId)
      {
         super(title, titleId, description, descriptionId);

         // check we have a path
         ParameterCheck.mandatoryString("path", path);
         
         this.path = path;
         this.instruction = instruction;
         this.instructionId = instructionId;
      }
      
      public String getPath()
      {
         return this.path;
      }
      
      public String getInstruction()
      {
         return this.instruction;
      }

      public String getInstructionId()
      {
         return this.instructionId;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override 
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (path=").append(this.path);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" instruction=").append(this.instruction);
         buffer.append(" instructionId=").append(this.instructionId).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Represents the configuration of a conditional page in a wizard 
    * i.e. a &lt;page&gt; element that is placed within a &lt;condition&gt;
    * element.
    */
   public static class ConditionalPageConfig extends PageConfig
   {
      protected String condition;
      
      public ConditionalPageConfig(String path, String condition,
                                   String title, String titleId,
                                   String description, String descriptionId,
                                   String instruction, String instructionId)
      {
         super(path, title, titleId, description, descriptionId, instruction, instructionId);

         // check we have a path
         ParameterCheck.mandatoryString("condition", condition);
         
         this.condition = condition;
      }
      
      public String getCondition()
      {
         return this.condition;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override 
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (path=").append(this.path);
         buffer.append(" condition=").append(this.condition);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" instruction=").append(this.instruction);
         buffer.append(" instructionId=").append(this.instructionId).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Represents the configuration of a step in a wizard
    * i.e. the &lt;step&gt; element.
    */
   public static class StepConfig extends AbstractConfig
   {
      protected String name;
      protected PageConfig defaultPage;
      protected List<ConditionalPageConfig> conditionalPages = 
         new ArrayList<ConditionalPageConfig>(3);
      
      public StepConfig(String name,
                        String title, String titleId,
                        String description, String descriptionId)
      {
         super(title, titleId, description, descriptionId);
         
         // check we have a name
         ParameterCheck.mandatoryString("name", name);
         
         // check we have a title
         if (this.title == null && this.titleId == null)
         {
            throw new IllegalArgumentException("A title or title-id attribute must be supplied for a step");
         }
         
         this.name = name;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public PageConfig getDefaultPage()
      {
         return this.defaultPage;
      }
      
      public boolean hasConditionalPages()
      {
         return (this.conditionalPages.size() > 0);
      }
      
      public List<ConditionalPageConfig> getConditionalPages()
      {
         return this.conditionalPages;
      }
      
      /*package*/ void addConditionalPage(ConditionalPageConfig conditionalPage)
      {
         this.conditionalPages.add(conditionalPage);
      }
      
      /*package*/ void setDefaultPage(PageConfig page)
      {
         this.defaultPage = page;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" defaultPage=").append(this.defaultPage);
         buffer.append(" conditionalPages=").append(this.conditionalPages).append(")");
         return buffer.toString();
      }
   }
}
