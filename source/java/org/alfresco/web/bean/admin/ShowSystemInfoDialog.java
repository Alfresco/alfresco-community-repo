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
package org.alfresco.web.bean.admin;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.AboutBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Implementation for the SystemInfo dialog.
 * 
 * @author gavinc
 */
public class ShowSystemInfoDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 1328587489092603676L;
   
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
