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
package org.alfresco.web.bean.content;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Create Content Wizard" dialog
 * 
 * @author gavinc
 */
public class CreateContentWizard extends BaseContentWizard
{
   private static final long serialVersionUID = -2740634368271194418L;
   
   protected String content = null;
   protected List<SelectItem> createMimeTypes;
   
   transient protected FormsService formsService;
   protected String formName;
   protected FormProcessor.Session formProcessorSession = null;
   transient protected Document instanceDataDocument = null;
   
   private static Log logger = LogFactory.getLog(CreateContentWizard.class);
   
  
   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   /**
    * @return the formsService
    */
   private FormsService getFormsService()
   {
      //check for null for cluster environment
      if (formsService == null)
      {
         formsService =  (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   

   @Override
   public String finish()
   {
      // if a form is entered, then save the form instance data as XML ...
      if (this.instanceDataDocument != null)
      {
         this.content = XMLUtil.toString(this.instanceDataDocument, true);
         
         this.mimeType = MimetypeMap.MIMETYPE_XML; // belts & braces - override mimetype (in case is not set to XML)
      }
      
      String result = super.finish();
      
      if ((super.createdNode != null) && (this.instanceDataDocument != null))
      {
         final Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(WCMAppModel.PROP_PARENT_FORM_NAME, getFormName());
         props.put(WCMAppModel.PROP_ORIGINAL_PARENT_PATH, "");
         getNodeService().addAspect(super.createdNode, WCMAppModel.ASPECT_FORM_INSTANCE_DATA, props);
      }
      
      return result;
   }
   
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
      this.formName = "";
      
      this.instanceDataDocument = null;
      if (this.formProcessorSession != null)
      {
         this.formProcessorSession.destroy();
      }
      this.formProcessorSession = null;
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
   public boolean getFinishButtonDisabled()
   {
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
         // check whether the created node is checked out, if a 'check out'
         // rule is present in the space the new node will be and an
         // attempt to modify the properties will cause an error (ALF-438)
         if (getNodeService().hasAspect(this.createdNode, ContentModel.ASPECT_LOCKABLE))
         {
            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_NODE_LOCKED));
            return outcome;
         }
         else
         {
            // we are going to immediately edit the properties so we need
            // to setup the BrowseBean context appropriately
            this.browseBean.setDocument(new Node(this.createdNode));
      
            return getDefaultFinishOutcome() + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
                   "dialog:setContentProperties";
         }
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
      if ((this.createMimeTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
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
      
      if (this.instanceDataDocument != null)
      {
         this.mimeType = MimetypeMap.MIMETYPE_XML; // belts & braces - override mimetype (in case is not set to XML)
      }
      
      // TODO: show first few lines of content here?
      return buildSummary(
            new String[] {bundle.getString("file_name"), 
                          bundle.getString("type"), 
                          bundle.getString("content_type")},
            new String[] {Utils.encode(this.fileName), getSummaryObjectType(), 
                          getSummaryMimeType(this.mimeType)});
   }
   
   /**
    * @return List of UI items to represent the full list of available ECM Forms
    */
   public List<SelectItem> getFormsList()
   {
      Collection<Form> forms = this.getFormsService().getForms();
      List<SelectItem> items = new ArrayList<SelectItem>(forms.size()+1);
      items.add(new SelectItem("", ""));
      for (Form form : forms)
      {
    	 items.add(new SelectItem(form.getName(), form.getTitle()));
      }
      return items;
   }
   
   public String getFormName()
   {
      return this.formName;
   }

   public void setFormName(String formName)
   {
      this.formName = formName;
   }
   
   public Form getForm() throws FormNotFoundException
   {
      return (this.getFormName() != null 
              ? getFormsService().getForm(formName)
	      : null);
   }

   public FormProcessor.Session getFormProcessorSession()
   {
      return this.formProcessorSession;
   }

   public void setFormProcessorSession(final FormProcessor.Session formProcessorSession)
   {
      this.formProcessorSession = formProcessorSession;
   }
   
   public Document getInstanceDataDocument()
   {
      if (this.instanceDataDocument == null)
      {
         final String content = this.getContent();
         try
         {
            this.instanceDataDocument = (content != null 
                                         ? XMLUtil.parse(content) 
                                         : XMLUtil.newDocument());
         }
         catch (Exception e)
         {
            Utils.addErrorMessage("error parsing document", e);
            this.instanceDataDocument = XMLUtil.newDocument();
         }
      }
      return this.instanceDataDocument;
   }
   
   /** Overrides in order to strip an xml extension if the user entered it */
   // TODO do we need ? it is currently referenced in create-forms.jsp (copied from wcm create-xml.jsp)
   @Override
   public String getFileName()
   {
      final String result = super.getFileName();
      return (result != null &&
              MimetypeMap.MIMETYPE_XML.equals(this.mimeType) &&
              this.getFormName() != null &&
              "xml".equals(FilenameUtils.getExtension(result).toLowerCase())
              ? FilenameUtils.removeExtension(result)
              : result);
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
   
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS),
               ((FileExistsException)exception).getName());
      }
      else
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), "error_content"),
               exception.getMessage());
      }
   }

}
