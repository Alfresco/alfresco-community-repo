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
