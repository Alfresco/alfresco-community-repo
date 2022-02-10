/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.base;

import static com.google.common.collect.Sets.newHashSet;

import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_ADMIN;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_POWER_USER;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_SECURITY_OFFICER;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_USER;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;

import java.util.Set;

/**
 * Test data used in tests
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public interface TestData
{
    /**
     * A user with ALFRESCO_ADMINISTRATORS role.
     * <p>"GROUP_ANOTHER_ADMIN_EXISTS" The ANOTHER_ADMIN user has been created.
     */
    public static final String ANOTHER_ADMIN = "another_admin";

    /**
     * The default password used when creating test users.
     */
    public static final String DEFAULT_PASSWORD = "password";

    /**
     * The default email address used when creating test users.
     */
    public static final String DEFAULT_EMAIL = "default@alfresco.com";

    /**
     * The default record category name used when creating categories
     */
    public static String RECORD_CATEGORY_NAME = "CATEGORY NAME" + getRandomAlphanumeric();

    /**
     * The default record category title used when creating categories
     */
    public static String RECORD_CATEGORY_TITLE = "CATEGORY TITLE" + getRandomAlphanumeric();

    /**
     * The default record folder name used when creating folders
     */
    public static String RECORD_FOLDER_NAME = "FOLDER NAME" + getRandomAlphanumeric();

    /**
     * The default record folder title used when creating folders
     */
    public static  String RECORD_FOLDER_TITLE = "FOLDER TITLE" + getRandomAlphanumeric();

    /**
     * The default electronic record name used when creating electronic records
     */
    public static String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();

    /**
     * The default non-electronic record name used when creating non-electronic records
     */
    public static String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();

    public static final String ALFRESCO_ADMINISTRATORS = "ALFRESCO_ADMINISTRATORS";
    /**
     * The ids of the default RM roles.
     */
    public static final Set<String> RM_ROLES = newHashSet(ROLE_RM_ADMIN.roleId, ROLE_RM_MANAGER.roleId,
            ROLE_RM_POWER_USER.roleId, ROLE_RM_SECURITY_OFFICER.roleId, ROLE_RM_USER.roleId);

    /**
     * The default hold description
     */
    String HOLD_DESCRIPTION = "Generalized hold case for tests";

    /**
     * The default hold reason
     */
    String HOLD_REASON = "Active content to be reviewed for the CASE McDermott, FINRA ";
}
