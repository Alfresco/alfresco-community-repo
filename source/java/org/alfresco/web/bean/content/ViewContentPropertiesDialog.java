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
package org.alfresco.web.bean.content;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Bean implementation of the "View Content Properties" dialog.
 * 
 * @author gavinc
 */
public class ViewContentPropertiesDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -867609607881256449L;
   
   protected static final String TEMP_PROP_MIMETYPE = "mimetype";
   protected static final String TEMP_PROP_ENCODING = "encoding";
   
   protected Node viewingNode;
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup the editable node
      this.viewingNode = new Node(this.browseBean.getDocument().getNodeRef());
      
      // special case for Mimetype - since this is a sub-property of the ContentData object
      // we must extract it so it can be edited in the client, then we check for it later
      // and create a new ContentData object to wrap it and it's associated URL
      ContentData content = (ContentData)this.viewingNode.getProperties().get(ContentModel.PROP_CONTENT);
      if (content != null)
      {
         this.viewingNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
         this.viewingNode.getProperties().put(TEMP_PROP_ENCODING, content.getEncoding());
      }
      
      // add the specially handled 'size' property
      this.viewingNode.addPropertyResolver("size", this.browseBean.resolverSize);
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // nothing to do as the finish button is not shown and the dialog is read only
         
      return outcome;
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * Returns the node being viewed
    * 
    * @return The node being viewed
    */
   public Node getViewingNode()
   {
      return this.viewingNode;
   }
}
