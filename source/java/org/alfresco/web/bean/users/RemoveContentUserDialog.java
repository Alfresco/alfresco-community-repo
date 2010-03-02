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
package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Implementation of remove content user dialog.
 * 
 * @author gavinc
 */
public class RemoveContentUserDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -3090054828215666084L;

   private static final String MSG_REMOVE_USER = "remove_user";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   private ContentUsersBean contentUsersBean;

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      contentUsersBean.removeOK();
      
      return outcome;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_REMOVE_USER) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) 
             + this.getPersonName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public String getContainerSubTitle()
   {
      return this.browseBean.getDocument().getName();
   }

   public void setupUserAction(ActionEvent event)
   {
      this.contentUsersBean.setupUserAction(event);
   }

   public String getPersonName()
   {
      return this.contentUsersBean.getPersonName();
   }

   public void setPersonName(String personName)
   {
      this.contentUsersBean.setPersonName(personName);
   }

   public ContentUsersBean getContentUsersBean()
   {
      return contentUsersBean;
   }

   public void setContentUsersBean(ContentUsersBean contentUsersBean)
   {
      this.contentUsersBean = contentUsersBean;
   }
}
