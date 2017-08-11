/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.bean.content;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseInviteUsersWizard;

/**
 * Concrete implementation providing the ability to invite users to content.
 * 
 * @author gavinc
 */
public class InviteContentUsersWizard extends BaseInviteUsersWizard
{
   private static final long serialVersionUID = 9198783146031469545L;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      // Let the permission service do the caching to allow for dynamic model updates, etc.
      return this.permissionService.getSettablePermissions(getNode().getType());
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getDocument();
   }

    @Override
    protected String getEmailTemplateXPath()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String xpath = Application.getRootPath(fc) + "/" + 
              Application.getGlossaryFolderName(fc) + "/" +
              Application.getEmailTemplatesFolderName(fc) + "/" + 
              Application.getNotifyEmailTemplatesFolderName(fc) + "//*";
        return xpath;
    }
}
