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
package org.alfresco.web.app.portlet;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.User;
import org.apache.myfaces.portlet.DefaultViewSelector;

/**
 * @author Kevin Roast
 */
public class AlfrescoDefaultViewSelector implements DefaultViewSelector
{
   /**
    * Select the appropriate view ID
    */
   public String selectViewId(RenderRequest request, RenderResponse response) throws PortletException
   {
      User user = (User)request.getPortletSession().getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
      if (user != null && user.getUserName().equals(PermissionService.GUEST_AUTHORITY))
      {
         return "/jsp/browse/browse.jsp";
      }
      else
      {
         return null;
      }
   }
   
   /**
    * @see org.apache.myfaces.portlet.DefaultViewSelector#setPortletContext(javax.portlet.PortletContext)
    */
   public void setPortletContext(PortletContext portletContext)
   {
   }
}
