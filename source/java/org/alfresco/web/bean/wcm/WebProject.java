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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormImpl;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.RenderingEngineTemplateImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides configured data for a web project.
 *
 * @author Ariel Backenroth
 */
public class WebProject implements Serializable
{
   /////////////////////////////////////////////////////////////////////////////

   private static final long serialVersionUID = 2480625511643744703L;
   
   /**
    * Wraps a form object to provide overridden values at the web project level
    */
   private class FormWrapper extends FormImpl
   {
      private static final long serialVersionUID = 1L;
      
      private final NodeRef formNodeRef;
      private Form baseForm;
      private NodeRef defaultWorkflowNodeRef;

      private FormWrapper(final Form form, 
                          final NodeRef formNodeRef,
                          final FormsService formsService)
      {
         super(((FormImpl)form).getNodeRef(), formsService);
         this.formNodeRef = formNodeRef;
      }

      @Override
      public String getTitle()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         return (String)nodeService.getProperty(this.formNodeRef,
                                                ContentModel.PROP_TITLE);
      }

      @Override
      public String getDescription()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         return (String)nodeService.getProperty(this.formNodeRef,
                                                ContentModel.PROP_DESCRIPTION);
      }

      @Override
      public String getOutputPathPattern()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         final String result = (String)
            nodeService.getProperty(this.formNodeRef,
                                    WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
         return (result != null ? result : this.baseForm.getOutputPathPattern());
      }

      @Override
      protected NodeRef getDefaultWorkflowNodeRef()
      {
         if (this.defaultWorkflowNodeRef == null)
         {
            final NodeService nodeService = this.getServiceRegistry().getNodeService();
            final List<ChildAssociationRef> workflowRefs = 
               nodeService.getChildAssocs(this.formNodeRef,
                                          WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                                          RegexQNamePattern.MATCH_ALL);
            if (workflowRefs.size() == 0)
            {
               return null;
            }
               
            this.defaultWorkflowNodeRef = workflowRefs.get(0).getChildRef();
         }
         return this.defaultWorkflowNodeRef;
      }

      @Override
      protected Map<String, RenderingEngineTemplate> loadRenderingEngineTemplates()
      {
         final Map<String, RenderingEngineTemplate> allRets = super.loadRenderingEngineTemplates();

         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         final List<ChildAssociationRef> retNodeRefs = 
            nodeService.getChildAssocs(this.formNodeRef,
                                       WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                                       RegexQNamePattern.MATCH_ALL);
         final Map<String, RenderingEngineTemplate> result = 
            new HashMap<String, RenderingEngineTemplate>(retNodeRefs.size(), 1.0f);
         for (ChildAssociationRef car : retNodeRefs)
         {
            final String renderingEngineTemplateName = (String)
               nodeService.getProperty(car.getChildRef(), 
                                       WCMAppModel.PROP_BASE_RENDERING_ENGINE_TEMPLATE_NAME);
            final String outputPathPattern = (String)
               nodeService.getProperty(car.getChildRef(), WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
            final RenderingEngineTemplateImpl ret = (RenderingEngineTemplateImpl)
               allRets.get(renderingEngineTemplateName);
            if (ret != null)
            {

                result.put(ret.getName(), 
                       new RenderingEngineTemplateImpl(ret.getNodeRef(),
                                                       ret.getRenditionPropertiesNodeRef(),
                                                       this.getFormsService())
                       {
                          private static final long serialVersionUID = -5498865830153013192L;
                          
                          @Override
                          public String getOutputPathPattern()
                          {
                             return outputPathPattern;
                          }
                       });
            }

         }
         return result;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private final static Log LOGGER = LogFactory.getLog(WebProject.class); 
   
   private final NodeRef nodeRef;
   private String storeId = null;
   
   public WebProject(final NodeRef nodeRef)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }

      final NodeService nodeService = getServiceRegistry().getNodeService();
      if (!WCMAppModel.TYPE_AVMWEBFOLDER.equals(nodeService.getType(nodeRef)))
      {
         throw new IllegalArgumentException(nodeRef + " is not a " + WCMAppModel.TYPE_AVMWEBFOLDER);
      }

      this.nodeRef = nodeRef;
   }

   public WebProject(final String avmPath)
   {
      if (avmPath == null)
      {
         throw new NullPointerException();
      }
      
      final String stagingStore = AVMUtil.buildStagingStoreName(AVMUtil.getStoreId(AVMUtil.getStoreName(avmPath)));
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      this.nodeRef = (NodeRef)
         avmService.getStoreProperty(stagingStore, 
                                     SandboxConstants.PROP_WEB_PROJECT_NODE_REF).getValue(DataTypeDefinition.NODE_REF);
   }

   /**
    * Returns the noderef for the webproject
    *
    * @return the noderef for the webproject.
    */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   /**
    * Returns the name of the web project.
    *
    * @return the name of the web project.
    * @deprecated
    */
   public String getName()
   {
       // TODO refactor out ...
      final NodeService nodeService = getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME);
   }

   /**
    * Returns the title of the web project.
    *
    * @return the title of the web project.
    * @deprecated
    */
   public String getTitle()
   {
      // TODO refactor out ...
      final NodeService nodeService = getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_TITLE);
   }

   /**
    * Returns the description of the web project.
    *
    * @return the description of the web project.
    * @deprecated
    */
   public String getDescription()
   {
      // TODO refactor out ...
      final NodeService nodeService = getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_DESCRIPTION);
   }

   /**
    * Returns the store id for this web project.
    *
    * @return the store id for this web project.
    * @deprecated
    */
   public String getStoreId()
   {
      // TODO refactor out ...
      if (this.storeId == null)
      {
          final NodeService nodeService = getServiceRegistry().getNodeService();
          this.storeId = (String)nodeService.getProperty(this.nodeRef, WCMAppModel.PROP_AVMSTORE);
      }
      return this.storeId;
   }
   
   /**
    * Returns the staging store name.
    *
    * @return the staging store name.
    * @deprecated
    */
   public String getStagingStore()
   {
      // TODO refactor out ...
      return AVMUtil.buildStagingStoreName(this.getStoreId());
   }

   /**
    * Returns the forms configured for this web project.
    *
    * @return the forms configured for this web project.
    */
   public List<Form> getForms()
   {
      final List<Form> forms = new ArrayList<Form>(this.getFormsImpl().values());
      final QuickSort sorter = new QuickSort(forms, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      return Collections.unmodifiableList(forms);
   }

   /**
    * Returns the form with the given name or <tt>null</tt> if not found.
    *
    * @param name the name of the form
    * @return the form or <tt>null</tt> if not found.
    * @exception NullPointerException if the name is <tt>null</tt>.
    */
   public Form getForm(final String name)
      throws FormNotFoundException
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      final Form result = this.getFormsImpl().get(name);
      if (result == null || !name.equals(result.getName()))
      {
         if (result != null)
         {
            LOGGER.debug("removing " + name + 
                         " from cache as it doesn't match mapped form " + result.getName());
            this.getFormsImpl().remove(name);
         }
         throw new FormNotFoundException(name, this);
      }
      return result;
   }
   
   /**
    * @return true if this WebProject has any workflows assigned directly to the website or
    *         assigned to any of the forms attached to it
    */
   public boolean hasWorkflow()
   {
      // note: there's always a submit workflow, as direct to staging is now
      //       also routed via workflow
      return true;
   }

   private Map<String, Form> getFormsImpl()
   {
      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      final FormsService formsService = WebProject.getFormsService();
      final List<ChildAssociationRef> formRefs = 
         nodeService.getChildAssocs(this.nodeRef,
                                    WCMAppModel.ASSOC_WEBFORM,
                                    RegexQNamePattern.MATCH_ALL);
      Map<String, Form> result = new HashMap<String, Form>(formRefs.size(), 1.0f);
      for (final ChildAssociationRef ref : formRefs)
      {
         final String formName = (String)
            nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_FORMNAME);
         try
         {
            final Form baseForm = formsService.getWebForm(formName);
            result.put(formName, new FormWrapper(baseForm, ref.getChildRef(), formsService));
         }
         catch (FormNotFoundException fnfe)
         {
            LOGGER.debug("got exception " + fnfe.getMessage() + 
                         " while loading web forms for project " + this.getName());
         }
      }
      return result;
   }

   private static FormsService getFormsService()
   {
      return (FormsService)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(),
                                                      "FormsService");
   }

   private static ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   public boolean equals(final Object other)
   {
      return (other != null && 
              other instanceof WebProject && 
              this.getNodeRef().equals(((WebProject)other).getNodeRef()));
   }

   public int hashCode()
   {
      return this.nodeRef.hashCode();
   }
}
