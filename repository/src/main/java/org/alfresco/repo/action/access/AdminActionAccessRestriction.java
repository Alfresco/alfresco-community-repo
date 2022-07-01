/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.repo.action.access;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.security.AuthorityService;

public class AdminActionAccessRestriction extends ActionAccessRestrictionAbstractBase {

    private AuthorityService authorityService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * Only admin can run action access restriction
     *
     * @param action
     */
    protected void innerVerifyAccessRestriction(Action action) {
        boolean isAdminOrSystemUser = authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
        if (!isAdminOrSystemUser) {
            throw new ActionAccessException("Only admin or system user is allowed to define uses of " +
                    "or directly execute this action");
        }
    }
}
