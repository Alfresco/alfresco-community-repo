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
package org.alfresco.web.bean.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Create Content Wizard" dialog
 * 
 * @author gavinc
 */
public class CreateContentWizard extends BaseContentWizard
{
   protected String content = null;
   protected List<SelectItem> createMimeTypes;
   
   private static Log logger = LogFactory.getLog(CreateContentWizard.class);

   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      saveContent(null, this.content);
      
      // return the default outcome
      return outcome;
   }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.content = null;
      this.inlineEdit = true;
      this.mimeType = MimetypeMap.MIMETYPE_HTML;
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      boolean disabled = false;
      int step = Application.getWizardManager().getCurrentStep();
      switch(step)
      {
         case 1:
         {
            disabled = (this.fileName == null || this.fileName.length() == 0);
            break;
         }
      }
      
      return disabled;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // as we were successful, go to the set properties dialog if asked
      // to otherwise just return
      if (this.showOtherProperties)
      {
         // we are going to immediately edit the properties so we need
         // to setup the BrowseBean context appropriately
         this.browseBean.setDocument(new Node(this.createdNode));
      
         return getDefaultFinishOutcome() + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
                "dialog:setContentProperties";
      }
      else
      {
         return outcome;
      }
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * @return Returns the content from the edited form.
    */
   public String getContent()
   {
      return this.content;
   }
   
   /**
    * @param content The content to edit (should be clear initially)
    */
   public void setContent(String content)
   {
      this.content = content;
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreateMimeTypes()
   {
      if (this.createMimeTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.createMimeTypes = new ArrayList<SelectItem>(5);
         
         // add the configured create mime types to the list
         ConfigService svc = Application.getConfigService(context);
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg != null)
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  String currentMimeType = child.getAttribute("name");
                  if (currentMimeType != null)
                  {
                     String label = getSummaryMimeType(currentMimeType);
                     this.createMimeTypes.add(new SelectItem(currentMimeType, label));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find 'create-mime-types' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Content Wizards' configuration section");
         }
         
      }
      
      return this.createMimeTypes;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // TODO: show first few lines of content here?
      return buildSummary(
            new String[] {bundle.getString("file_name"), 
                          bundle.getString("type"), 
                          bundle.getString("content_type")},
            new String[] {this.fileName, getSummaryObjectType(), 
                          getSummaryMimeType(this.mimeType)});
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
      
   /**
    * Create content type value changed by the user
    */
   public void createContentChanged(ValueChangeEvent event)
   {
      // clear the content as HTML is not compatible with the plain text box etc.
      this.content = null;
   }
}
