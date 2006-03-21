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
    * Adds a wizard
    * 
    * @param wizardConfig A pre-configured wizard config object
    */
   /*package*/ void addWizard(WizardConfig wizardConfig)
   {
      this.wizards.put(wizardConfig.getName(), wizardConfig);
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
      protected Map<String, StepConfig> steps = new LinkedHashMap<String, StepConfig>(4);
      
      public WizardConfig(String name, String bean, 
                          String title, String titleId,
                          String description, String descriptionId)
      {
         super(title, titleId, description, descriptionId);

         // check we have a name
         ParameterCheck.mandatoryString("name", name);
         
         this.name = name;
         this.managedBean = bean;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public String getManagedBean()
      {
         return this.managedBean;
      }
      
      public void addStep(StepConfig step)
      {
         this.steps.put(step.getName(), step);
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
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override 
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" managed-bean=").append(this.managedBean);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId).append(")");
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
   public static class StepConfig
   {
      protected String name;
      protected PageConfig defaultPage;
      protected List<ConditionalPageConfig> conditionalPages = 
         new ArrayList<ConditionalPageConfig>(3);
      
      public StepConfig(String name)
      {
         // check we have a name
         ParameterCheck.mandatoryString("name", name);
         
         this.name = name;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public void setDefaultPage(PageConfig page)
      {
         this.defaultPage = page;
      }
      
      public PageConfig getDefaultPage()
      {
         return this.defaultPage;
      }
      
      public boolean hasConditionalPages()
      {
         return (this.conditionalPages.size() > 0);
      }
      
      public void addConditionalPage(ConditionalPageConfig conditionalPage)
      {
         this.conditionalPages.add(conditionalPage);
      }
      
      public List<ConditionalPageConfig> getConditionalPages()
      {
         return this.conditionalPages;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" defaultPage=").append(this.defaultPage);
         buffer.append(" conditionalPages=").append(this.conditionalPages).append(")");
         return buffer.toString();
      }
   }
}
