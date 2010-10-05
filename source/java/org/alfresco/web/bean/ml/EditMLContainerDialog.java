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
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Dialog bean to edit an existing multilingual container.
 *
 * @author Yannick Pignot
 */
public class EditMLContainerDialog extends  BaseDialogBean
{
   private static final long serialVersionUID = -6340255019962646300L;

   transient private MultilingualContentService multilingualContentService;

   private Node editableNode;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.editableNode = initEditableNode();
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get the container node ref
      NodeRef container = editableNode.getNodeRef();

      // get the modified properties
      Map<String, Object> editProperties = this.editableNode.getProperties();

      // Modify the properties of the container with the user modified properties
      // (We don't know which properties have been edited at this point, so edit
      //  all non-core ones)
      for(Map.Entry<String, Object> entry : editProperties.entrySet())
      {
         QName qname = QName.createQName(entry.getKey());
         
         if(! qname.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI) )
         {
            // Update the property on the real node
            getNodeService().setProperty(container, qname, (Serializable) entry.getValue());
         }
      }

      return outcome;
   }

   /**
    * Init the editable Node
    */
   protected Node initEditableNode()
   {
      Node currentNode = this.browseBean.getDocument();

      if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(currentNode.getType()))
      {
          return currentNode;
      }
      else
      {
          return new Node(
                getMultilingualContentService().getTranslationContainer(
                        currentNode.getNodeRef())
            );
      }

   }

   /**
    * @return the editableNode
    */
   public Node getEditableNode()
   {
      return editableNode;
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      this.browseBean.getDocument().reset();

      return outcome;
   }


   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * @return the multilingualContentService
    */
   public MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

   /**
    * @param multilingualContentService the multilingualContentService to set
    */
   public void setMultilingualContentService(
         MultilingualContentService multilingualContentService) {
      this.multilingualContentService = multilingualContentService;
   }
   
}
