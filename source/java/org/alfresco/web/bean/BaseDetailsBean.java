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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.security.OwnableService;
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
    * Sets the BrowseBean instance to use to retrieve the current Space
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
    * Returns the id of the current space
    * 
    * @return The id
    */
   public String getId()
   {
      return getNode().getId();
   }
   
   /**
    * Returns the name of the current space
    * 
    * @return Name of the current space
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
      Node space = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), space, URLMode.WEBDAV);
   }
   
   /**
    * Returns the CIFS path for the current node
    * 
    * @return The CIFS path
    */
   public String getCifsPath()
   {
      Node space = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), space, URLMode.CIFS);
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
    * @return true if the current document has the 'templatable' aspect applied and
    *         references a template that currently exists in the system.
    */
   public boolean isTemplatable()
   {
      NodeRef templateRef = (NodeRef)getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return (getNode().hasAspect(ContentModel.ASPECT_TEMPLATABLE) &&
              templateRef != null && nodeService.exists(templateRef));
   }
   
   /**
    * @return String of the NodeRef for the dashboard template used by the space if any 
    */
   public String getTemplateRef()
   {
      NodeRef ref = (NodeRef)getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.toString() : null;
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
       public String resolveImagePathForName(String filename, boolean small)
       {
           return Utils.getFileTypeImage(filename, small);
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
         // get the exisiting properties for the document
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
    * to the current document
    *  
    * @return The outcome string
    */
   public String saveWorkflow()
   {
      String outcome = "cancel";
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // firstly retrieve all the properties for the current node
         Map<QName, Serializable> updateProps = this.nodeService.getProperties(
               getNode().getNodeRef());
         
         // update the simple workflow properties
         
         // set the approve step name
         updateProps.put(ApplicationModel.PROP_APPROVE_STEP,
               this.workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_STEP_NAME));
         
         // specify whether the approve step will copy or move the content
         boolean approveMove = true;
         String approveAction = (String)this.workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_ACTION);
         if (approveAction != null && approveAction.equals("copy"))
         {
            approveMove = false;
         }
         updateProps.put(ApplicationModel.PROP_APPROVE_MOVE, Boolean.valueOf(approveMove));
         
         // create node ref representation of the destination folder
         updateProps.put(ApplicationModel.PROP_APPROVE_FOLDER,
               this.workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_FOLDER));
         
         // determine whether there should be a reject step
         boolean requireReject = true;
         String rejectStepPresent = (String)this.workflowProperties.get(
               SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT);
         if (rejectStepPresent != null && rejectStepPresent.equals("no"))
         {
            requireReject = false;
         }
         
         if (requireReject)
         {
            // set the reject step name
            updateProps.put(ApplicationModel.PROP_REJECT_STEP,
                  this.workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_STEP_NAME));
         
            // specify whether the reject step will copy or move the content
            boolean rejectMove = true;
            String rejectAction = (String)this.workflowProperties.get(
                  SimpleWorkflowHandler.PROP_REJECT_ACTION);
            if (rejectAction != null && rejectAction.equals("copy"))
            {
               rejectMove = false;
            }
            updateProps.put(ApplicationModel.PROP_REJECT_MOVE, Boolean.valueOf(rejectMove));

            // create node ref representation of the destination folder
            updateProps.put(ApplicationModel.PROP_REJECT_FOLDER,
                  this.workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_FOLDER));
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
         this.nodeService.setProperties(getNode().getNodeRef(), updateProps);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document so it reflects the changes just made
         getNode().reset();
         
         outcome = "finish";
      }
      catch (Throwable e)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
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
      
      NodeRef docNodeRef = new NodeRef(Repository.getStoreRef(), id);
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // call the service to perform the approve
         WorkflowUtil.approve(docNodeRef, this.nodeService, this.copyService);
         
         // commit the transaction
         tx.commit();
         
         // if this was called via the document details dialog we need to reset the document node
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
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
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
      
      NodeRef docNodeRef = new NodeRef(Repository.getStoreRef(), id);
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // call the service to perform the reject
         WorkflowUtil.reject(docNodeRef, this.nodeService, this.copyService);
         
         // commit the transaction
         tx.commit();
         
         // if this was called via the document details dialog we need to reset the document node
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
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_WORKFLOW_REJECT), e.getMessage()), e);
      }
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action handler to apply the selected Template and Templatable aspect to the current Space
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
    * Action handler to remove a dashboard template from the current Space
    */
   public void removeTemplate(ActionEvent event)
   {
      try
      {
         // clear template property
         this.nodeService.setProperty(getNode().getNodeRef(), ContentModel.PROP_TEMPLATE, null);
         this.nodeService.removeAspect(getNode().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE);
         
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
    * Action Handler to take Ownership of the current Space
    */
   public void takeOwnership(ActionEvent event)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(fc);
         tx.begin();
         
         this.ownableService.takeOwnership(getNode().getNodeRef());
         
         String msg = Application.getMessage(fc, MSG_SUCCESS_OWNERSHIP);
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         String formId = Utils.getParentForm(fc, event.getComponent()).getClientId(fc);
         fc.addMessage(formId + ':' + getPropertiesPanelId(), facesMsg);
         
         getNode().reset();
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
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
