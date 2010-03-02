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
package org.alfresco.web.bean.content;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

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
      
      Node document = this.browseBean.getDocument();
      if(document != null)
      {
      
    	  // setup the editable node
    	  this.viewingNode = new Node(document.getNodeRef());
    
      
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
