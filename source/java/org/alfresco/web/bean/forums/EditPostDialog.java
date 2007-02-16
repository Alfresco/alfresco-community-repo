/*
 * Copyright (C) 2005 Alfresco, Inc.
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
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // we need to remove the <br> tags and replace with carriage returns
      // and then setup the content member variable
      Node currentDocument = this.browseBean.getDocument();
      ContentReader reader = this.contentService.getReader(currentDocument.getNodeRef(), 
            ContentModel.PROP_CONTENT);
      
      if (reader != null)
      {
         String htmlContent = reader.getContentString();
         if (htmlContent != null)
         {
            this.content = StringUtils.replace(htmlContent, "<br/>", "\r\n");
         }
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // remove link breaks and replace with <br/>
      this.content = Utils.replaceLineBreaks(this.content);
      
      // update the content
      NodeRef postNode = this.browseBean.getDocument().getNodeRef();
      
      // check that the name of this post does not contain the :
      // character (used in previous versions), if it does rename
      // the post.
      String name = (String)this.nodeService.getProperty(
            postNode, ContentModel.PROP_NAME);
      if (name.indexOf(":") != -1)
      {
         String newName = name.replace(':', '-');
         this.fileFolderService.rename(postNode, newName);
      }
               
      ContentWriter writer = this.contentService.getWriter(postNode, 
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
