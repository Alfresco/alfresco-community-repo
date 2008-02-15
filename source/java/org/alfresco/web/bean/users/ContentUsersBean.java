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
package org.alfresco.web.bean.users;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing access to users of the current content/document.
 * 
 * @author gavinc
 */
public class ContentUsersBean extends UserMembersBean
{
   private static final long serialVersionUID = 5206400236997654181L;

   private final static String MSG_MANAGE_CONTENT_USERS = "manage_content_users";
   private final static String MSG_CONTENT_OWNER = "content_owner";
   private final static String MSG_CLOSE= "close";
   
   /**
    * @return The space to work against
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   @Override
   public Object getActionsContext()
   {
      return getNode();
   }
   
   @Override
   public String getContainerTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_MANAGE_CONTENT_USERS) + " '" + browseBean.getDocument().getName() + "'";
   }
   
   @Override
   public String getContainerSubTitle()
   {
      String pattern = Application.getMessage(FacesContext.getCurrentInstance(), MSG_CONTENT_OWNER);
      return MessageFormat.format(pattern, getOwner());
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
}
