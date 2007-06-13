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
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Dialog bean to edit an existing multilingual container.
 * 
 * @author Yannick Pignot
 */
public class EditMLContainerDialog extends  BaseDialogBean
{
   MultilingualContentService multilingualContentService;

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

      // modify the properties of the container with the user modified properties
      for(Map.Entry<String, Object> entry : editProperties.entrySet())
      {
         QName qname = QName.createQName(entry.getKey());

         nodeService.setProperty(container, qname, (Serializable) entry.getValue());
      }

      return outcome;
   }

   /**
    * Init the editable Node
    */
   protected Node initEditableNode()
   {
      return new Node(
            multilingualContentService.getTranslationContainer(
                  this.browseBean.getDocument().getNodeRef())
      );
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
   public MultilingualContentService getMultilingualContentService() {
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
