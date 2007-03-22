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
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;

/**
 * Backing bean for the edit link properties dialog
 * 
 * @author kevinr
 */
public class LinkPropertiesBean
{
   protected NodeService nodeService;
   protected DictionaryService dictionaryService;
   protected BrowseBean browseBean;
   protected NavigationBean navigator;
   private Node editableNode;
   
   /**
    * Returns the node being edited
    * 
    * @return The node being edited
    */
   public Node getEditableNode()
   {
      return this.editableNode;
   }
   
   /**
    * Event handler called to setup the link object for property editing
    * 
    * @param event The event
    */
   public void setupFileLinkForAction(ActionEvent event)
   {
      this.editableNode = new Node(this.browseBean.getDocument().getNodeRef());
   }
   
   /**
    * Event handler called to setup the link object for property editing
    * 
    * @param event The event
    */
   public void setupFolderLinkForAction(ActionEvent event)
   {
      this.editableNode = new Node(this.browseBean.getActionSpace().getNodeRef());
   }
   
   /**
    * @return Human readable version of the Path to the destination object
    */
   public String getDestinationPath()
   {
      NodeRef destRef = (NodeRef)this.editableNode.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
      return Repository.getNamePath(
            this.nodeService, this.nodeService.getPath(destRef), null, "/", null);
   }
   
   /**
    * Returns the URL to access the details page for the current document link object
    * 
    * @return The bookmark URL
    */
   public String getFileLinkBookmarkUrl()
   {
      NodeRef destRef = (NodeRef)this.browseBean.getDocument().getProperties().get(ContentModel.PROP_LINK_DESTINATION);
      return Utils.generateURL(FacesContext.getCurrentInstance(), new Node(destRef), URLMode.SHOW_DETAILS);
   }
   
   /**
    * Returns the URL to access the details page for the current document link object
    * 
    * @return The bookmark URL
    */
   public String getSpaceLinkDestinationUrl()
   {
      NodeRef destRef = (NodeRef)this.browseBean.getActionSpace().getProperties().get(ContentModel.PROP_LINK_DESTINATION);
      return Utils.generateURL(FacesContext.getCurrentInstance(), new Node(destRef), URLMode.SHOW_DETAILS);
   }
   
   /**
    * Event handler used to save the edited properties back to the repository
    * 
    * @return The outcome
    */
   public String save()
   {
      String outcome = "cancelEdit";
      
      // setup the dispatch context as it is required for correct cancel/finish back to link dialog
      this.navigator.setupDispatchContext(this.editableNode);
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         NodeRef nodeRef = this.editableNode.getNodeRef();
         Map<String, Object> props = this.editableNode.getProperties();
         
         Map<QName, Serializable> properties = this.nodeService.getProperties(nodeRef);
         
         // we need to put all the properties from the editable bag back into 
         // the format expected by the repository
         
         // deal with adding the "titled" aspect if required
         String title = (String)props.get(ContentModel.PROP_TITLE);
         String description = (String)props.get(ContentModel.PROP_DESCRIPTION);
         if (title != null || description != null)
         {
            // add the aspect to be sure it's present
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
            // other props will get added later in setProperties()
         }
         
         // add the remaining properties
         Iterator<String> iterProps = props.keySet().iterator();
         while (iterProps.hasNext())
         {
            String propName = iterProps.next();
            QName qname = QName.createQName(propName);
            
            // make sure the property is represented correctly
            Serializable propValue = (Serializable)props.get(propName);
            
            // check for empty strings when using number types, set to null in this case
            if ((propValue != null) && (propValue instanceof String) && 
                (propValue.toString().length() == 0))
            {
               PropertyDefinition propDef = this.dictionaryService.getProperty(qname);
               if (propDef != null)
               {
                  if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                      propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                      propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                      propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
                  {
                     propValue = null;
                  }
               }
            }
            
            properties.put(qname, propValue);
         }
         
         // send the properties back to the repository
         this.nodeService.setProperties(nodeRef, properties);
         
         // commit the transaction
         tx.commit();
         
         // set the outcome to refresh
         outcome = "finishEdit";
         
         // reset any document held by the browse bean as it's just been updated
         // if this is a space link then it doesn't matter anyway
         if (this.browseBean.getDocument() != null)
         {
             this.browseBean.getDocument().reset();
         }
      }
      catch (InvalidNodeRefException err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {this.browseBean.getDocument().getId()}) );
         
         // this failure means the node no longer exists - we cannot show the doc properties screen
         outcome = "browse";
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      
      return outcome;
   }
   
   public String cancel()
   {
      // setup the dispatch context as it is required for correct cancel/finish back to link dialog
      this.navigator.setupDispatchContext(this.editableNode);
      
      return "cancelEdit";
   }
   
   public Map<String, Object> getProperties()
   {
      return this.editableNode.getProperties();
   }
   
   /**
    * @return Returns the nodeService.
    */
   public NodeService getNodeService()
   {
      return this.nodeService;
   }

   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * Sets the DictionaryService to use when persisting metadata
    * 
    * @param dictionaryService The DictionaryService
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   /**
    * @return The BrowseBean
    */
   public BrowseBean getBrowseBean()
   {
      return this.browseBean;
   }

   /**
    * @param browseBean The BrowseBean to set.
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
}
