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
package org.alfresco.web.bean.forums;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation of the "Create Reply Dialog".
 * 
 * @author gavinc
 */
public class CreateReplyDialog extends CreatePostDialog
{
   private static final long serialVersionUID = 8036934269090933533L;

   protected String replyContent = null;
   
   private static final Log logger = LogFactory.getLog(CreateReplyDialog.class);

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.replyContent = null;
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // remove link breaks and replace with <br>
      this.content = Utils.replaceLineBreaks(this.content, false);
      
      super.finishImpl(context, outcome);
      
      // setup the referencing aspect with the references association
      // between the new post and the one being replied to
      this.getNodeService().addAspect(this.createdNode, ContentModel.ASPECT_REFERENCING, null);
      this.getNodeService().createAssociation(this.createdNode, this.browseBean.getDocument().getNodeRef(), 
            ContentModel.ASSOC_REFERENCES);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("created new node: " + this.createdNode);
         logger.debug("existing node: " + this.browseBean.getDocument().getNodeRef());
      }
      
      return outcome;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "reply");
   }
}
