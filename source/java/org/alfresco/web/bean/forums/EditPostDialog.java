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
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.springframework.util.StringUtils;

/**
 * Bean implementation for the "Edit Post Dialog".
 * 
 * @author gavinc
 */
public class EditPostDialog extends CreatePostDialog
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   private static final long serialVersionUID = 7925794441178897699L;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // we need to remove the <br> tags and replace with carriage returns
      // and then setup the content member variable
      Node currentDocument = this.browseBean.getDocument();
      ContentReader reader = this.getContentService().getReader(currentDocument.getNodeRef(), 
            ContentModel.PROP_CONTENT);
      
      if (reader != null)
      {
         String htmlContent = reader.getContentString();
         if (htmlContent != null)
         {
            // ETHREEOH-1216: replace both forms of 'br' as older posts have the <br/> version
            // which doesn't work in all browsers supported by Alfresco Explorer
            htmlContent = StringUtils.replace(htmlContent, "<br/>", "\r\n");
            this.content = StringUtils.replace(htmlContent, "<br>", "\r\n");
         }
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // remove link breaks and replace with <br>
      this.content = Utils.replaceLineBreaks(this.content, false);
      
      // update the content
      NodeRef postNode = this.browseBean.getDocument().getNodeRef();
      
      // check that the name of this post does not contain the :
      // character (used in previous versions), if it does rename
      // the post.
      String name = (String)this.getNodeService().getProperty(
            postNode, ContentModel.PROP_NAME);
      if (name.indexOf(":") != -1)
      {
         String newName = name.replace(':', '-');
         this.getFileFolderService().rename(postNode, newName);
      }
               
      ContentWriter writer = this.getContentService().getWriter(postNode, 
            ContentModel.PROP_CONTENT, true);
      if (writer != null)
      {
         writer.putContent(this.content);
      }
         
      return outcome;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
   }
}
