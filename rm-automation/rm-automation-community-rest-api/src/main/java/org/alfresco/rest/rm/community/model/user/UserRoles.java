/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.rm.community.model.user;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

/**
 * Constants for RM user roles
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UserRoles
{
    public static final String ROLE_RM_ADMIN = "Administrator";
    public static final String ROLE_RM_MANAGER = "RecordsManager";
    public static final String ROLE_RM_POWER_USER = "PowerUser";
    public static final String ROLE_RM_SECURITY_OFFICER = "SecurityOfficer";
    public static final String ROLE_RM_USER = "User";

    /** The ids of the default RM roles. */
    public static final Set<String> RM_ROLES = newHashSet(ROLE_RM_ADMIN, ROLE_RM_MANAGER, ROLE_RM_POWER_USER,
                ROLE_RM_SECURITY_OFFICER, ROLE_RM_USER);
}
