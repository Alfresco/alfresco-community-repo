/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Allow comment editing only to coordinators, site managers, creator and owner
 */
public class EditPostEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -5544290216536965941L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      return (currentUser.equalsIgnoreCase((String)node.getProperties().get(ContentModel.PROP_OWNER)) ||
              currentUser.equalsIgnoreCase((String)node.getProperties().get(ContentModel.PROP_CREATOR)) ||
              node.hasPermission(SiteModel.SITE_MANAGER) ||
              node.hasPermission(PermissionService.COORDINATOR));
   }
}
