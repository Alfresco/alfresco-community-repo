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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;

/**
 * Backing bean for the edit link properties dialog
 * 
 * @author kevinr
 * @author YanO
 */
public class LinkPropertiesDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -167326851073011187L;
   
   private Node editableNode = null;
   
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset node instance for next dialog invocation
      this.editableNode = null;
   }
   
   public Map<String, Object> getProperties()
   {
      return getEditableNode().getProperties();
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   /**
    * Returns the node being edited
    * 
    * @return The node being edited
    */
   public Node getEditableNode()
   {
      if (this.editableNode == null)
      {
         if (this.parameters.containsKey("nodeRef"))
         {
            this.editableNode = new Node(new NodeRef(this.parameters.get("nodeRef")));
         }
         else
         {
            throw new IllegalArgumentException("Dialog has not been initialised with noderef or setup action event called.");
         }
      }
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
      NodeRef destRef = (NodeRef)getEditableNode().getProperties().get(ContentModel.PROP_LINK_DESTINATION);
      return Repository.getNamePath(
            this.getNodeService(), this.getNodeService().getPath(destRef), null, "/", null);
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
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      try
      {
         NodeRef nodeRef = getEditableNode().getNodeRef();
         Map<String, Object> props = getEditableNode().getProperties();
         
         //get the name and move the node as necessary
         String name = (String) props.get(ContentModel.PROP_NAME);
         if (name != null)
         {
            getFileFolderService().rename(nodeRef, name);
         } 
         
         Map<QName, Serializable> properties = this.getNodeService().getProperties(nodeRef);
         
         // we need to put all the properties from the editable bag back into 
         // the format expected by the repository
         
         // deal with adding the "titled" aspect if required
         String title = (String)props.get(ContentModel.PROP_TITLE);
         String description = (String)props.get(ContentModel.PROP_DESCRIPTION);
         if (title != null || description != null)
         {
            // add the aspect to be sure it's present
            getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
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
               PropertyDefinition propDef = this.getDictionaryService().getProperty(qname);
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
         this.getNodeService().setProperties(nodeRef, properties);
      }
      catch (InvalidNodeRefException err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_NODEREF), new Object[] {this.browseBean.getDocument().getId()}));
         
         // this failure means the node no longer exists - we cannot show the doc properties screen
         outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                   AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), e.getMessage()), e);
         outcome = null;
         ReportedException.throwIfNecessary(e);
      }
      
      return outcome;
   }
   
   @Override 
   protected String doPostCommitProcessing(FacesContext context, String outcome) 
   { 
      if (this.browseBean.getDocument() != null) 
      { 
         this.browseBean.getDocument().reset(); 
      } 
      else if (this.browseBean.getActionSpace() != null) 
      { 
         this.browseBean.getActionSpace().reset(); 
      } 
      
      return outcome; 
   } 
}
