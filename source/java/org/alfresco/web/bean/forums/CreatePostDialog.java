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

import org.alfresco.model.ForumModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.CreateContentWizard;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean implementation of the "New Post Dialog".
 * 
 * @author gavinc
 */
public class CreatePostDialog extends CreateContentWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // set up for creating a post
      this.objectType = ForumModel.TYPE_POST.toString();
      
      // make sure we don't show the edit properties dialog after creation
      this.showOtherProperties = false;
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // create appropriate values for filename and content type
      this.fileName = ForumsBean.createPostFileName();
      this.mimeType = Repository.getMimeTypeForFileName(
                  FacesContext.getCurrentInstance(), this.fileName);
      
      // remove link breaks and replace with <br/>
      this.content = Utils.replaceLineBreaks(this.content);
      
      return super.finishImpl(context, outcome);
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "post");
   }
}
