/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.admin.patch.impl;

import java.util.Set;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A patch to add a new group authority.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class AddGroupAuthorityPatch extends AbstractPatch
{
    private static final String MSG_START = "patch.addGroupAuthority.start";
    private static final String MSG_RESULT = "patch.addGroupAuthority.result";
    private static final String MSG_EXIST ="patch.addGroupAuthority.exist";

    private AuthorityService authorityService;
    private GroupAuthorityDetails groupAuthorityDetails;

    /**
     * Sets the authority service.
     * 
     * @param authorityService the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Sets the group authority details.
     * 
     * @param groupAuthorityDetails the groupAuthorityDetails
     */
    public void setGroupAuthorityDetails(GroupAuthorityDetails groupAuthorityDetails)
    {
        this.groupAuthorityDetails = groupAuthorityDetails;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(authorityService, "authorityService");
        checkPropertyNotNull(groupAuthorityDetails, "groupAuthorityDetails");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));

        String groupAuthorityName = PermissionService.GROUP_PREFIX + this.groupAuthorityDetails.groupName;
        if (!authorityService.authorityExists(groupAuthorityName))
        {
            groupAuthorityName = authorityService.createAuthority(AuthorityType.GROUP,
                        this.groupAuthorityDetails.groupName,
                        this.groupAuthorityDetails.groupDisplayName,
                        this.groupAuthorityDetails.authorityZones);

            authorityService.addAuthority(groupAuthorityName, AuthenticationUtil.getAdminUserName());

            result.append(I18NUtil.getMessage(MSG_RESULT, groupAuthorityName));
        }
        else
        {
            result.append(I18NUtil.getMessage(MSG_EXIST, groupAuthorityName));
        }
        return result.toString();
    }

    /**
     * A simple POJO to encapsulate the group authority details.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class GroupAuthorityDetails
    {
        private String groupName;
        private String groupDisplayName;
        private Set<String> authorityZones;

        /**
         * @param groupName the groupName to set
         */
        public void setGroupName(String groupName)
        {
            this.groupName = groupName;
        }

        /**
         * @param groupDisplayName the groupDisplayName to set
         */
        public void setGroupDisplayName(String groupDisplayName)
        {
            this.groupDisplayName = groupDisplayName;
        }

        /**
         * @param authorityZones the authorityZones to set
         */
        public void setAuthorityZones(Set<String> authorityZones)
        {
            this.authorityZones = authorityZones;
        }
    }
}
