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
package org.alfresco.web.bean;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Implementation for the SystemInfo dialog.
 * 
 * @author gavinc
 */
public class ShowSystemInfoDialog extends BaseDialogBean
{
   private static final String MSG_CURRENT_USER = "current_user";
   private static final String MSG_CLOSE = "close";
   private static final String MSG_VERSION = "version";

   protected AboutBean aboutBean; 

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }

   public void setAboutBean(AboutBean aboutBean)
   {
      this.aboutBean = aboutBean;
   }

   @Override
   public String getContainerSubTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CURRENT_USER) + ": " + 
               this.navigator.getCurrentUser().getUserName();
   }

   @Override
   public String getContainerDescription()
   {
      StringBuilder builder = new StringBuilder(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_VERSION));
      builder.append(": ");
      builder.append(this.aboutBean.getEdition());
      builder.append(" - v");
      builder.append(this.aboutBean.getVersion());
      
      return builder.toString();
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

}
