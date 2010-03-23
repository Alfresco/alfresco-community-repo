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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Edit Form wizard.
 * 
 * @author Ariel Backenroth
 * @author Arseny Kovalchuk (Bug Fixer)
 * 
 * Methods removeRenderingEngineTemplateFromWebProjects, addRenderingEngineTemplateToWebProjects, searchRenderingEngineTemplateInWebProject
 * are added to fix an issue reported in https://issues.alfresco.com/jira/browse/ETWOONE-317
 * 
 */
public class EditFormWizard 
   extends CreateFormWizard
{
   private static final long serialVersionUID = -3260838389223325316L;
   
   private final static Log LOGGER = LogFactory.getLog(EditFormWizard.class);

   private List<RenderingEngineTemplateData> removedRenderingEngineTemplates;
   private List<WebProject> associatedWebProjects;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      
      final NodeRef formNodeRef = this.browseBean.getActionSpace().getNodeRef();
      if (formNodeRef == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }

      final Form form = this.getFormsService().getForm(formNodeRef);
      // simple properties
      this.setFormName(form.getName());
      this.setFormTitle(form.getTitle());
      this.setFormDescription(form.getDescription());
      this.setSchemaRootElementName(form.getSchemaRootElementName());
      NodeRef schemaNodeRef = (NodeRef)
         this.getNodeService().getProperty(formNodeRef, WCMAppModel.PROP_XML_SCHEMA);
      if (schemaNodeRef == null)
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug(WCMAppModel.PROP_XML_SCHEMA + " not set on " + formNodeRef +
                         ", checking " + WCMAppModel.PROP_XML_SCHEMA_OLD);

         schemaNodeRef = (NodeRef)
            getNodeService().getProperty(formNodeRef, WCMAppModel.PROP_XML_SCHEMA_OLD);
         if (schemaNodeRef != null)
         {
            getNodeService().setProperty(formNodeRef, WCMAppModel.PROP_XML_SCHEMA, schemaNodeRef);
         }
      }
      if (schemaNodeRef == null)
      {
         throw new NullPointerException("expected property " + WCMAppModel.PROP_XML_SCHEMA +
                                        " of " + formNodeRef + 
                                        " for form " + form.getName() +
                                        " not to be null.");
      }
      this.setSchemaFileName((String)this.getNodeService().getProperty(schemaNodeRef, 
                                                                  ContentModel.PROP_NAME));
      try
      {
         this.schema = form.getSchema();
      }
      catch (Throwable t)
      {
         final String msg = "unable to parse " + form.getName();
         Utils.addErrorMessage(msg, t);
      }
      final WorkflowDefinition wf = form.getDefaultWorkflow();
      if (wf != null)
      {
         this.defaultWorkflowName = wf.getName();
      }
      else
      {
         this.applyDefaultWorkflow = false;
      }
      this.setOutputPathPatternForFormInstanceData(form.getOutputPathPattern());

      for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
      {
         final RenderingEngineTemplateData data =
            this.new RenderingEngineTemplateData(ret);
         this.renderingEngineTemplates.add(data);
      }
      this.removedRenderingEngineTemplates = null;
      
      if (getIsWebForm() == true)
      {
         this.associatedWebProjects = this.getFormsService().getAssociatedWebProjects(form);
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(final FacesContext context, 
                               final String outcome) 
      throws Exception
   {
      final NodeRef formNodeRef = this.browseBean.getActionSpace().getNodeRef();
      
      // apply the name, title and description props
      if (!this.getFormName().equals(this.getNodeService().getProperty(formNodeRef, ContentModel.PROP_NAME)))
      {
         this.getFileFolderService().rename(formNodeRef, this.getFormName());
      }

      this.getNodeService().setProperty(formNodeRef, ContentModel.PROP_TITLE, this.getFormTitle());
      this.getNodeService().setProperty(formNodeRef, ContentModel.PROP_DESCRIPTION, this.getFormDescription());
      this.getNodeService().setProperty(formNodeRef, 
                                   WCMAppModel.PROP_OUTPUT_PATH_PATTERN, 
                                   this.getOutputPathPatternForFormInstanceData());
      this.getNodeService().setProperty(formNodeRef,
                                   WCMAppModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME,
                                   this.getSchemaRootElementName());
      final WorkflowDefinition wd = this.getDefaultWorkflowDefinition();
      final List<ChildAssociationRef> workflowRefs = 
         this.getNodeService().getChildAssocs(formNodeRef,
                                         WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                         RegexQNamePattern.MATCH_ALL);

      if (wd != null && workflowRefs.size() == 0)
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("adding workflow definition " + wd.getName() + 
                         " to form " + this.getFormName());
         
         final Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(WCMAppModel.PROP_WORKFLOW_NAME, wd.getName());
         this.getNodeService().createNode(formNodeRef,
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.TYPE_WORKFLOW_DEFAULTS,
                                     props);
      }
      else if (wd != null && workflowRefs.size() == 1)
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("setting workflow definition " + wd.getName() + 
                         " to form " + this.getFormName());

         this.getNodeService().setProperty(workflowRefs.get(0).getChildRef(),
                                      WCMAppModel.PROP_WORKFLOW_NAME,
                                      wd.getName());
      }              
      else if (wd == null && workflowRefs.size() == 1)
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("removing workflow definitions from form " + this.getFormName());
         
         this.getNodeService().removeChild(formNodeRef, workflowRefs.get(0).getChildRef());
      }         

      if (this.getSchemaFile() != null)
      {
         final FileInfo fileInfo = 
            this.getFileFolderService().create(formNodeRef,
                                          this.getSchemaFileName(),
                                          ContentModel.TYPE_CONTENT);
         // get a writer for the content and put the file
         final ContentWriter writer = this.contentService.getWriter(fileInfo.getNodeRef(),
                                                                    ContentModel.PROP_CONTENT,
                                                                    true);
         // set the mimetype and encoding
         writer.setMimetype(MimetypeMap.MIMETYPE_XML);
         writer.setEncoding("UTF-8");
         writer.putContent(this.getSchemaFile());
         this.getNodeService().setProperty(formNodeRef,
                                      WCMAppModel.PROP_XML_SCHEMA, 
                                      fileInfo.getNodeRef());
      }

      if (this.removedRenderingEngineTemplates != null)
      {
         for (final RenderingEngineTemplateData retd : this.removedRenderingEngineTemplates)
         {
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("removing rendering engine template " + retd);
            
            assert retd != null;
            assert retd.getNodeRef() != null;
            this.getNodeService().removeAssociation(formNodeRef, retd.getNodeRef(), 
                     WCMAppModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
            this.getNodeService().removeChild(formNodeRef, retd.getNodeRef());
            this.removeRenderingEngineTemplateFromWebProjects(formNodeRef, retd);

         }
      }
      for (final RenderingEngineTemplateData retd : this.renderingEngineTemplates)
      {
         if (retd.getFile() != null)
         {
            this.saveRenderingEngineTemplate(retd, formNodeRef);
            this.addRenderingEngineTemplateToWebProjects(formNodeRef, retd);
         }
      }
      return outcome;
   }
   
   /**
    * Removes an associated Rendering Engine Template from all web forms in all web projects.
    * 
    * @param formNodeRef Form nodeRef
    * @param retd Rendering engine template to remove from web projects
    */
   private void removeRenderingEngineTemplateFromWebProjects(NodeRef formNodeRef, RenderingEngineTemplateData retd)
   {
       List<WebProject> webProjects = getFormsService().getAssociatedWebProjects(getFormsService().getForm(formNodeRef));
       for (WebProject wp: webProjects)
       {
           ResultSet results = searchRenderingEngineTemplateInWebProject(wp, retd.getName());
           try
           {
              for (int i=0; i<results.length(); i++)
              {
                  NodeRef webformTemplateNodeRef = results.getNodeRef(i);
                  NodeRef webformNodeRef = getNodeService().getPrimaryParent(webformTemplateNodeRef).getParentRef();
                  getNodeService().removeChild(webformNodeRef, webformTemplateNodeRef);
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug(webformNodeRef);
              }
           }
           finally
           {
              results.close();
           }
       }
   }
   
   /**
    * Adds or updates an associated Rendering Engine Template from all web forms in all web projects.
    * 
    * @param formNodeRef
    * @param retd Rendering engine template to remove from web projects
    */
   
   private void addRenderingEngineTemplateToWebProjects(NodeRef formNodeRef, RenderingEngineTemplateData retd)
   {
       Form form = getFormsService().getForm(formNodeRef);
       List<WebProject> webProjects = getFormsService().getAssociatedWebProjects(form);
       Map<QName, Serializable> props = new HashMap<QName, Serializable>(4, 1.0f);
       for (WebProject wp: webProjects)
       {
           ResultSet results = searchRenderingEngineTemplateInWebProject(wp, retd.getName());
           try
           {
               int resultsCount = results.length();
               if (resultsCount>0)
               {
                   //update
                   for (int i=0; i<resultsCount; i++)
                   {
                       NodeRef webformTemplateNodeRef = results.getNodeRef(i);
                       if (retd.getOutputPathPatternForRendition() != null)
                       {
                          props.clear();
                          props.put(WCMAppModel.PROP_OUTPUT_PATH_PATTERN, retd.getOutputPathPatternForRendition());
                          getNodeService().addAspect(webformTemplateNodeRef, WCMAppModel.ASPECT_OUTPUT_PATH_PATTERN, props);
                       }
                   }
               }
               else
               {
                   //just add
                   String query = "+TYPE:\"" + WCMAppModel.TYPE_WEBFORM + "\"" +
                                  " +@" + Repository.escapeQName(WCMAppModel.PROP_FORMNAME) + ":\"" + form.getName() + "\"";
                   
                   if (LOGGER.isDebugEnabled())
                      LOGGER.debug("Search web forms query: " + query);
    
                   ResultSet webforms = getSearchService().query(wp.getNodeRef().getStoreRef(), SearchService.LANGUAGE_LUCENE, query);
                   
                   try
                   {
                      for (int i=0; i<webforms.length(); i++)
                      {
                          if (LOGGER.isDebugEnabled())
                             LOGGER.debug("WebForm NodeRef: " + webforms.getNodeRef(i));
                          
                          props.clear();
                          props.put(WCMAppModel.PROP_BASE_RENDERING_ENGINE_TEMPLATE_NAME, 
                                    retd.getName());
                          
                          NodeRef templateRef = getNodeService().createNode(webforms.getNodeRef(i),
                                                                            WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                                                                            WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                                                                            WCMAppModel.TYPE_WEBFORMTEMPLATE,
                                                                            props).getChildRef();
                          
                          if (retd.getOutputPathPatternForRendition() != null)
                          {
                             props.clear();
                             props.put(WCMAppModel.PROP_OUTPUT_PATH_PATTERN, retd.getOutputPathPatternForRendition());
                             getNodeService().addAspect(templateRef, WCMAppModel.ASPECT_OUTPUT_PATH_PATTERN, props);
                          }
                      }
                   }
                   finally
                   {
                      webforms.close();
                   }
               }
           }
           finally
           {
               results.close();
           }
       }
   }
   
   /**
    * Searches an specific Web Form Template with appropriate name in the Web Project.
    * 
    * @param wp The WebProject to search
    * @param name The name of Rendering Engine Template to search
    * @return Search result
    */
   private ResultSet searchRenderingEngineTemplateInWebProject(WebProject wp, String name)
   {
       ResultSet result = null;
       StringBuilder query = new StringBuilder(256);
       query.append("+TYPE:\"").append(WCMAppModel.TYPE_WEBFORMTEMPLATE).append("\" ");
       query.append("+@").append(Repository.escapeQName(WCMAppModel.PROP_BASE_RENDERING_ENGINE_TEMPLATE_NAME)).append(":\"").append(name).append("\" ");
       
       // Search not found anything in this StoreRef!
       // It looks like a wrong search in RegenerateRenditionsWizard.
       // 
       //StoreRef storeRef = AVMNodeConverter.ToStoreRef(wp.getStagingStore());
       
       StoreRef storeRef = wp.getNodeRef().getStoreRef();
       result = getSearchService().query(storeRef, SearchService.LANGUAGE_LUCENE, query.toString());
       if (LOGGER.isDebugEnabled())
       {
           LOGGER.debug(">>>Web Project: " + wp);
           LOGGER.debug(">>>StoreRef: " + storeRef);
           LOGGER.debug(">>>Search query: " + query.toString());
           LOGGER.debug(">>>Search results: " + result.length());
       }
       return result;
   }

   /**
    * Action handler called when the Remove button is pressed to remove a 
    * rendering engine
    */
   @Override
   public void removeSelectedRenderingEngineTemplate(final ActionEvent event)
   {
      final RenderingEngineTemplateData wrapper = (RenderingEngineTemplateData)
         this.getRenderingEngineTemplatesDataModel().getRowData();
      if (wrapper != null)
      {
         if (this.removedRenderingEngineTemplates == null)
         {
            this.removedRenderingEngineTemplates = new LinkedList<RenderingEngineTemplateData>();
         }
         this.removedRenderingEngineTemplates.add(wrapper);
      }
      super.removeSelectedRenderingEngineTemplate(event);
   }


   /** Indicates whether or not the wizard is currently in edit mode */
   @Override
   public boolean getEditMode()
   {
      return true;
   }

   @Override
   public List<WebProject> getAssociatedWebProjects()
   {
      return this.associatedWebProjects;
   }
}
