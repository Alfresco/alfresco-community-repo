/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.HOLD_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.HOLD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;

import org.testng.annotations.DataProvider;

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
     * Data Provider with the special file plan components alias
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] getContainers()
    {
        return new Object[][] {
                { FILE_PLAN_ALIAS },
                { TRANSFERS_ALIAS },
                { HOLDS_ALIAS },
                { UNFILED_RECORDS_CONTAINER_ALIAS },
        };
    }

    /**
     * Data Provider with:
     * the special file plan components alias
     * file plan component node type
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] getContainersAndTypes()
    {
        return new Object[][] {
                { FILE_PLAN_ALIAS, FILE_PLAN_TYPE },
                { TRANSFERS_ALIAS, TRANSFER_CONTAINER_TYPE },
                { HOLDS_ALIAS, HOLD_CONTAINER_TYPE },
                { UNFILED_RECORDS_CONTAINER_ALIAS, UNFILED_CONTAINER_TYPE },
        };
    }

    /**
     * The default CATEGORY name used when creating categories
     */
    public static String CATEGORY_NAME = "CATEGORY NAME" + getRandomAlphanumeric();

    /**
     * The default CATEGORY title used when creating categories
     */
    public static String CATEGORY_TITLE = "CATEGORY TITLE" + getRandomAlphanumeric();

    /**
     * The default FOLDER name used when creating folders
     */
    public static String FOLDER_NAME = "FOLDER NAME" + getRandomAlphanumeric();

    /**
     * The default FOLDER title used when creating folders
     */
    public static  String FOLDER_TITLE = "FOLDER TITLE" + getRandomAlphanumeric();

    /**
     * The default electronic record  name used when creating electronic records
     */
    public static String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();
    
    /**
     * The default Non electronic record name used when creating non-electronic records
     */
    public static String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();
    
    /**
     * Data Provider with:
     * with the object types not allowed as children for a record category
     *
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] childrenNotAllowedForCategory()
    {
        return new Object[][] {
            { FILE_PLAN_TYPE },
            { TRANSFER_CONTAINER_TYPE },
            { HOLD_CONTAINER_TYPE },
            { UNFILED_CONTAINER_TYPE },
            { UNFILED_RECORD_FOLDER_TYPE },
            { HOLD_TYPE },
            { TRANSFER_TYPE },
            { FOLDER_TYPE },
            { CONTENT_TYPE }
        };
    }
}
