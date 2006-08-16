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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
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
   
   /** Selected template Id */
   protected String template;
   
   protected Map<String, Boolean> panels = new HashMap<String, Boolean>(4, 1.0f);
   
   
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
