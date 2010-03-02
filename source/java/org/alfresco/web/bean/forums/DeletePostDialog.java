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

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.DeleteContentDialog;

/**
 * Bean implementation for the "Delete Post" dialog.
 * 
 * @author gavinc
 */
public class DeletePostDialog extends DeleteContentDialog
{
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   private static final long serialVersionUID = 6804626884508461423L;

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      super.doPostCommitProcessing(context, outcome);
      
      return this.getDefaultFinishOutcome();
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String postConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "delete_post_confirm");
      
      return MessageFormat.format(postConfirmMsg, 
            new Object[] {this.browseBean.getDocument().getProperties().get("creator")});
   }
}
