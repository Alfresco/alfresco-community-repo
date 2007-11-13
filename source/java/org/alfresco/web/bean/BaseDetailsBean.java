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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.actions.handlers.SimpleWorkflowHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;

/**
 * Backing bean provided access to the details of a Node
 * 
 * @author Kevin Roast
 */
public abstract class BaseDetailsBean
{
   private static final String MSG_SUCCESS_OWNERSHIP = "success_ownership";

   /** BrowseBean instance */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** NodeServuce bean reference */
   protected NodeService nodeService;
   
   /** OwnableService bean reference */
   protected OwnableService ownableService;
   
   /** CopyService bean reference */
   protected CopyService copyService;
   
   /** The PermissionService reference */
   protected PermissionService permissionService;
   
   /** Selected template Id */
   protected String template;
   
   /** The map of workflow properties */
   protected Map<String, Serializable> workflowProperties;
   
   protected Map<String, Boolean> panels = new HashMap<String, Boolean>(4, 1.0f);
   
   private static final String MSG_ERROR_WORKFLOW_REJECT = "error_workflow_reject";
   private static final String MSG_ERROR_WORKFLOW_APPROVE = "error_workflow_approve";
   private static final String MSG_ERROR_UPDATE_SIMPLEWORKFLOW = "error_update_simpleworkflow";
   
   public BaseDetailsBean()
   {
      // initial state of some panels that don't use the default
      panels.put("workflow-panel", false);
      panels.put("category-panel", false);
   }
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * Sets the BrowseBean instance to use to retrieve the current node
    * 
    * @param browseBean BrowseBean instance
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @param nodeService   The NodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * Sets the ownable service instance the bean should use
    * 
    * @param ownableService The OwnableService
    */
   public void setOwnableService(OwnableService ownableService)
   {
      this.ownableService = ownableService;
   }
   
   /**
    * Sets the copy service instance the bean should use
    * 
    * @param copyService The CopyService
    */
   public void setCopyService(CopyService copyService)
   {
      this.copyService = copyService;
   }
   
   /**
    * @param permissionService The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @return Returns the panels expanded state map.
    */
   public Map<String, Boolean> getPanels()
   {
      return this.panels;
   }

   /**
    * @param panels The panels expanded state map.
    */
   public void setPanels(Map<String, Boolean> panels)
   {
      this.panels = panels;
   }
   
   /**
    * Returns the Node this bean is currently representing
    * 
    * @return The Node
    */
   public abstract Node getNode();
   
   /**
    * Returns the id of the current node
    * 
    * @return The id
    */
   public String getId()
   {
      return getNode().getId();
   }
   
   /**
    * Returns the name of the current node
    * 
    * @return Name of the current 
    */
   public String getName()
   {
      return getNode().getName();
   }
   
   /**
    * Return the Alfresco NodeRef URL for the current node
    * 
    * @return the Alfresco NodeRef URL
    */
   public String getNodeRefUrl()
   {
      return getNode().getNodeRef().toString();
   }
   
   /**
    * Returns the WebDAV URL for the current node
    * 
    * @return The WebDAV url
    */
   public String getWebdavUrl()
   {
      Node node = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), node, URLMode.WEBDAV);
   }
   
   /**
    * Returns the CIFS path for the current node
    * 
    * @return The CIFS path
    */
   public String getCifsPath()
   {
      Node node = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), node, URLMode.CIFS);
   }

   /**
    * Returns the URL to access the details page for the current node
    * 
    * @return The bookmark URL
    */
   public String getBookmarkUrl()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getNode(), URLMode.SHOW_DETAILS);
   }
   
   /**
    * Resolve the actual Node from any Link object that may be proxying it
    * 
    * @return current Node or Node resolved from any Link object
    */
   protected abstract Node getLinkResolvedNode();

   /**
    * @return Returns the template Id.
    */
   public String getTemplate()
   {
      // return current template if it exists
      NodeRef ref = (NodeRef)getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.getId() : this.template;
   }

   /**
    * @param template The template Id to set.
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * @return true if the current node has a custom Template or Webscript view applied and
    *         references a template/webscript that currently exists in the system.
    */
   public boolean getHasCustomView()
   {
      return getHasWebscriptView() || getHasTemplateView();
   }
   
   /**
    * @return true if the current node has a Template based custom view available
    */
   public boolean getHasTemplateView()
   {
      if (getNode().hasAspect(ContentModel.ASPECT_TEMPLATABLE))
      {
         NodeRef templateRef = (NodeRef)getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
         return (templateRef != null && this.nodeService.exists(templateRef) &&
                 this.permissionService.hasPermission(templateRef, PermissionService.READ) == AccessStatus.ALLOWED);
      }
      return false;
   }
   
   /**
    * @return true if the current node has a Webscript based custom view available
    */
   public boolean getHasWebscriptView()
   {
      if (getNode().hasAspect(ContentModel.ASPECT_WEBSCRIPTABLE))
      {
         return (getNode().getProperties().get(ContentModel.PROP_WEBSCRIPT) != null);
      }
      return false;
   }
   
   /**
    * @return String of the NodeRef for the custom view for the node
    */
   public String getTemplateRef()
   {
      NodeRef ref = (NodeRef)getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.toString() : null;
   }
   
   /**
    * @return Webscript URL for the custom view for the node
    */
   public String getWebscriptUrl()
   {
      return (String)getNode().getProperties().get(ContentModel.PROP_WEBSCRIPT);
   }
   
   /**
    * Returns a model for use by a template on the Details page.
    * 
    * @return model containing current current node info.
    */
   public abstract Map getTemplateModel();
   
   /** Template Image resolver helper */
   protected TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, FileTypeImageSize size)
      {
         return Utils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
   
   /**
    * Returns the properties for the attached workflow as a map
    * 
    * @return Properties of the attached workflow, null if there is no workflow
    */
   public Map<String, Serializable> getWorkflowProperties()
   {
      if (this.workflowProperties == null && 
          getNode().hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW))
      {
         // get the exisiting properties for the node
         Map<String, Object> props = getNode().getProperties();
         
         String approveStepName = (String)props.get(
               ApplicationModel.PROP_APPROVE_STEP.toString());
         String rejectStepName = (String)props.get(
               ApplicationModel.PROP_REJECT_STEP.toString());
         
         Boolean approveMove = (Boolean)props.get(
               ApplicationModel.PROP_APPROVE_MOVE.toString());
         Boolean rejectMove = (Boolean)props.get(
               ApplicationModel.PROP_REJECT_MOVE.toString());
         
         NodeRef approveFolder = (NodeRef)props.get(
               ApplicationModel.PROP_APPROVE_FOLDER.toString());
         NodeRef rejectFolder = (NodeRef)props.get(
               ApplicationModel.PROP_REJECT_FOLDER.toString());

         // put the workflow properties in a separate map for use by the JSP
         this.workflowProperties = new HashMap<String, Serializable>(7);
         this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_STEP_NAME, 
               approveStepName);
         this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_ACTION, 
               approveMove ? "move" : "copy");
         this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_FOLDER, approveFolder);
         
         if (rejectStepName == null || rejectMove == null || rejectFolder == null)
         {
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT, "no");
         }
         else
         {
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT, 
                  "yes");
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_NAME, 
                  rejectStepName);
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_ACTION, 
                  rejectMove ? "move" : "copy");
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_FOLDER, 
                  rejectFolder);
         }
      }
      
      return this.workflowProperties;
   }
   
   /**
    * Cancel Workflow Edit dialog
    */
   public String cancelWorkflowEdit()
   {
      // resets the workflow properties map so any changes made
      // don't appear to be persisted
      this.workflowProperties.clear();
      this.workflowProperties = null;
      return "cancel";
   }
   
   /**
    * Saves the details of the workflow stored in workflowProperties
    * to the current node
    *  
    * @return The outcome string
    */
   public String saveWorkflow()
   {
      String outcome = "cancel";
      
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // firstly retrieve all the properties for the current node
               Map<QName, Serializable> updateProps = nodeService.getProperties(
                     getNode().getNodeRef());
               
               // update the simple workflow properties
               
               // set the approve step name
               updateProps.put(ApplicationModel.PROP_APPROVE_STEP,
                     workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_STEP_NAME));
               
               // specify whether the approve step will copy or move the content
               boolean approveMove = true;
               String approveAction = (String)workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_ACTION);
               if (approveAction != null && approveAction.equals("copy"))
               {
                  approveMove = false;
               }
               updateProps.put(ApplicationModel.PROP_APPROVE_MOVE, Boolean.valueOf(approveMove));
               
               // create node ref representation of the destination folder
               updateProps.put(ApplicationModel.PROP_APPROVE_FOLDER,
                     workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_FOLDER));
               
               // determine whether there should be a reject step
               boolean requireReject = true;
               String rejectStepPresent = (String)workflowProperties.get(
                     SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT);
               if (rejectStepPresent != null && rejectStepPresent.equals("no"))
               {
                  requireReject = false;
               }
               
               if (requireReject)
               {
                  // set the reject step name
                  updateProps.put(ApplicationModel.PROP_REJECT_STEP,
                        workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_STEP_NAME));
               
                  // specify whether the reject step will copy or move the content
                  boolean rejectMove = true;
                  String rejectAction = (String)workflowProperties.get(
                        SimpleWorkflowHandler.PROP_REJECT_ACTION);
                  if (rejectAction != null && rejectAction.equals("copy"))
                  {
                     rejectMove = false;
                  }
                  updateProps.put(ApplicationModel.PROP_REJECT_MOVE, Boolean.valueOf(rejectMove));

                  // create node ref representation of the destination folder
                  updateProps.put(ApplicationModel.PROP_REJECT_FOLDER,
                        workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_FOLDER));
               }
               else
               {
                  // set all the reject properties to null to signify there should
                  // be no reject step
                  updateProps.put(ApplicationModel.PROP_REJECT_STEP, null);
                  updateProps.put(ApplicationModel.PROP_REJECT_MOVE, null);
                  updateProps.put(ApplicationModel.PROP_REJECT_FOLDER, null);
               }
               
               // set the properties on the node
               nodeService.setProperties(getNode().getNodeRef(), updateProps);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
         
         // reset the state of the current node so it reflects the changes just made
         getNode().reset();
         
         outcome = "finish";
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE_SIMPLEWORKFLOW), e.getMessage()), e);
      }
      
      return outcome;
   }
   
   /**
    * Returns the name of the approve step of the attached workflow
    * 
    * @return The name of the approve step or null if there is no workflow
    */
   public String getApproveStepName()
   {
      String approveStepName = null;
      
      if (getNode().hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW))
      {
         approveStepName = (String)getNode().getProperties().get(
               ApplicationModel.PROP_APPROVE_STEP.toString());
      }
      
      return approveStepName; 
   }
   
   /**
    * Event handler called to handle the approve step of the simple workflow
    * 
    * @param event The event that was triggered
    */
   public void approve(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id == null || id.length() == 0)
      {
         throw new AlfrescoRuntimeException("approve called without an id");
      }
      
      final NodeRef docNodeRef = new NodeRef(Repository.getStoreRef(), id);
      
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // call the service to perform the approve
               WorkflowUtil.approve(docNodeRef, nodeService, copyService);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
         
         // if this was called via the node details dialog we need to reset the node
         if (getNode() != null)
         {
            getNode().reset();
         }
         
         // also make sure the UI will get refreshed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_WORKFLOW_APPROVE), e.getMessage()), e);
      }
   }
   
   /**
    * Returns the name of the reject step of the attached workflow
    * 
    * @return The name of the reject step or null if there is no workflow
    */
   public String getRejectStepName()
   {
      String approveStepName = null;
      
      if (getNode().hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW))
      {
         approveStepName = (String)getNode().getProperties().get(
               ApplicationModel.PROP_REJECT_STEP.toString());
      }
      
      return approveStepName;
   }
   
   /**
    * Event handler called to handle the approve step of the simple workflow
    * 
    * @param event The event that was triggered
    */
   public void reject(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id == null || id.length() == 0)
      {
         throw new AlfrescoRuntimeException("reject called without an id");
      }
      
      final NodeRef docNodeRef = new NodeRef(Repository.getStoreRef(), id);
      
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // call the service to perform the reject
               WorkflowUtil.reject(docNodeRef, nodeService, copyService);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
         
         // if this was called via the node details dialog we need to reset the node
         if (getNode() != null)
         {
            getNode().reset();
         }
         
         // also make sure the UI will get refreshed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_WORKFLOW_REJECT), e.getMessage()), e);
      }
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action handler to apply the selected Template and Templatable aspect to the current node
    */
   public void applyTemplate(ActionEvent event)
   {
      if (this.template != null && this.template.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         try
         {
            // apply the templatable aspect if required 
            if (getNode().hasAspect(ContentModel.ASPECT_TEMPLATABLE) == false)
            {
               this.nodeService.addAspect(getNode().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE, null);
            }
            
            // get the selected template from the Template Picker
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.template);
            
            // set the template NodeRef into the templatable aspect property
            this.nodeService.setProperty(getNode().getNodeRef(), ContentModel.PROP_TEMPLATE, templateRef); 
            
            // reset node details for next refresh of details page
            getNode().reset();
         }
         catch (Exception e)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         }
      }
   }
   
   /**
    * Action handler to remove a custom view template from the current node
    */
   public void removeTemplate(ActionEvent event)
   {
      try
      {
         // clear template property
         this.nodeService.setProperty(getNode().getNodeRef(), ContentModel.PROP_TEMPLATE, null);
         this.nodeService.removeAspect(getNode().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE);
         this.nodeService.removeAspect(getNode().getNodeRef(), ContentModel.ASPECT_WEBSCRIPTABLE);
         
         // reset node details for next refresh of details page
         getNode().reset();
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
   
   /**
    * Action Handler to take Ownership of the current node
    */
   public void takeOwnership(final ActionEvent event)
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               ownableService.takeOwnership(getNode().getNodeRef());
               
               String msg = Application.getMessage(fc, MSG_SUCCESS_OWNERSHIP);
               FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
               String formId = Utils.getParentForm(fc, event.getComponent()).getClientId(fc);
               fc.addMessage(formId + ':' + getPropertiesPanelId(), facesMsg);
               
               getNode().reset();
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               fc, Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
   
   /**
    * @return id of the properties panel component 
    */
   protected abstract String getPropertiesPanelId();
   
   /**
    * Save the state of the panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         String id = event.getComponent().getId();
         // we prefix some panels with "no-" which we remove to give consistent behaviour in the UI
         if (id.startsWith("no-") == true)
         {
            id = id.substring(3);
         }
         this.panels.put(id, ((ExpandedEvent)event).State);
      }
   }
}
