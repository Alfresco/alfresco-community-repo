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
   
   private static final long serialVersionUID = -2859329677883776068L;

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
      
      // remove link breaks and replace with <br>
      this.content = Utils.replaceLineBreaks(this.content, false);
      
      // set UTF-8 encoding for the post (all original posts are UTF-8 also)
      this.encoding = "UTF-8";
      
      return super.finishImpl(context, outcome);
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "post");
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return true;
   }
}
