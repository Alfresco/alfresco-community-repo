/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
