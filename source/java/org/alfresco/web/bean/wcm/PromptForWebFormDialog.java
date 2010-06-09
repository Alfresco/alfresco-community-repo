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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author arielb
 */
public class PromptForWebFormDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 8062203927131257236L;

   private static final Log logger = LogFactory.getLog(PromptForWebFormDialog.class);
   
   /** AVM service reference */
   transient private AVMService avmService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;

   /** The FormsService reference */
   transient private FormsService formsService;

   private transient List<SelectItem> formChoices;

   private String formName;
   private String cancelOutcome;
   private String finishOutcome;
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (this.avmService == null)
      {
         this.avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return this.avmService;
   }

   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   protected FormsService getFormsService()
   {
      if (this.formsService == null)
      {
         this.formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return this.formsService;
   }

   /**
    * @return Returns the current AVM node context.
    */
   public AVMNode getAvmNode()
   {
      return this.avmBrowseBean.getAvmActionNode();
   }
   
   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      this.cancelOutcome = (this.parameters.containsKey("cancelOutcome") 
                            ? this.parameters.get("cancelOutcome")
                            : "dialog:editAvmFile");
      this.finishOutcome = (this.parameters.containsKey("finishOutcome")
                            ? this.parameters.get("finishOutcome")
                            : "wizard:editWebContent");
      this.formName = null;
      this.formChoices = null;
      final String avmPath = this.getAvmNode().getPath();
      if (this.getAvmService().hasAspect(this.getAvmNode().getVersion(), 
                                    avmPath, 
                                    WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         // build a status message if this is an error case
         
         FormInstanceData fid = null;
         try
         {
            fid = this.getFormsService().getFormInstanceData(this.getAvmNode().getVersion(), avmPath);
            final Form f = fid.getForm();
            this.formName = f.getName();
            
            if (logger.isDebugEnabled())
            {
                // strange case... this should throw an exception if we're here... 
                logger.debug(avmPath + ".getForm() did not throw a form not found.  why are we here?");
            }
         }
         catch (final FormNotFoundException fnfe)
         {
            String msg = (fnfe.getWebProject() != null 
                          ? "prompt_for_web_form_form_not_found_error_in_web_project"
                          : "prompt_for_web_form_form_not_found_error");
            msg = Application.getMessage(FacesContext.getCurrentInstance(), msg);
            
            if (fid != null)
            {
                msg = (fnfe.getWebProject() != null
                       ? MessageFormat.format(msg, 
                                              fnfe.getFormName(), 
                                              fid.getName(), 
                                              fnfe.getWebProject().getName())
                       : MessageFormat.format(msg, 
                                              fnfe.getFormName(), 
                                              fid.getName()));
            }
            
            this.avmBrowseBean.displayStatusMessage(FacesContext.getCurrentInstance(), msg);
         }
      }
   }
   
   @Override
   public String cancel()
   {
      getAvmService().removeAspect(this.getAvmNode().getPath(), WCMAppModel.ASPECT_RENDITION);
      getAvmService().removeAspect(this.getAvmNode().getPath(), WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
       
      return super.cancel();
   }
   
   @Override
   protected String finishImpl(final FacesContext context, String outcome)
      throws Exception
   {
      
      if (logger.isDebugEnabled())
      {
         logger.debug("configuring " + this.getAvmNode().getPath() +
                      " to use form " + this.getFormName());
      }
      
      if (!this.getAvmService().hasAspect(this.getAvmNode().getVersion(),
                                     this.getAvmNode().getPath(), 
                                     WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         String cwd = AVMUtil.getCorrespondingPathInPreviewStore(this.avmBrowseBean.getCurrentPath());
         
         Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(WCMAppModel.PROP_PARENT_FORM_NAME, this.getFormName());
         props.put(WCMAppModel.PROP_ORIGINAL_PARENT_PATH, cwd);
         this.getNodeService().addAspect(this.getAvmNode().getNodeRef(), WCMAppModel.ASPECT_FORM_INSTANCE_DATA, props);
      }
      else
      {
          this.getAvmService().setNodeProperty(this.getAvmNode().getPath(),
                  WCMAppModel.PROP_PARENT_FORM_NAME,
                  new PropertyValue(DataTypeDefinition.TEXT, this.getFormName()));
      }
      
      return outcome;
   }
      
   @Override
   public boolean getFinishButtonDisabled()
   {
      return this.getFormChoices().size() == 0;
   }

   @Override
   protected String getDefaultCancelOutcome()
   {
      return (super.getDefaultCancelOutcome() + 
              AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
              this.cancelOutcome);
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return (super.getDefaultFinishOutcome() + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
              this.finishOutcome);
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * @return the available forms from this web project that can be created.
    */
   public List<SelectItem> getFormChoices()
   {
      if (this.formChoices == null)
      {
         final WebProject wp = new WebProject(this.getAvmNode().getPath());
         final List<Form> forms = wp.getForms();
         this.formChoices = new ArrayList<SelectItem>(forms.size());
         for (final Form f : forms)
         {
            this.formChoices.add(new SelectItem(f.getName(), f.getTitle()));
         }
         
         final QuickSort sorter = new QuickSort(this.formChoices, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
      }
      return this.formChoices;
   }

   /**
    * @return the currently selected form
    */
   public String getFormName()
   {
      return this.formName;
   }

   /**
    * @param form Sets the currently selected form
    */
   public void setFormName(final String formName)
   {
      this.formName = formName;
   }
}
