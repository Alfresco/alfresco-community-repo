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
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
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
    public static String[][] getContainers()
    {
        return new String[][] {
                { FILE_PLAN_ALIAS },
                { TRANSFERS_ALIAS },
                { UNFILED_RECORDS_CONTAINER_ALIAS },
        };
    }

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

    /**
     * Data Provider with:
     * with the object types not allowed as children for a record category
     *
     * @return file plan component alias
     */
    @DataProvider
    public static String[][] childrenNotAllowedForCategory()
    {
        return new String[][] {
            { FILE_PLAN_TYPE },
            { TRANSFER_CONTAINER_TYPE },
            { UNFILED_CONTAINER_TYPE },
            { UNFILED_RECORD_FOLDER_TYPE },
            { TRANSFER_TYPE },
            { CONTENT_TYPE }
        };
    }

    /**
     * Data Provider with:
     * with the object types  for creating a Record Folder
     *
     * @return file plan component alias
     */
    @DataProvider
    public static String[][] folderTypes()
    {
        return new String[][] {
            { RECORD_FOLDER_TYPE },
            { FOLDER_TYPE }
        };
    }

    /**
     * Data Provider with:
     * with the object types  for creating a Record Category
     *
     * @return file plan component alias
     */
    @DataProvider
    public static String[][] categoryTypes()
    {
        return new String[][] {
            { FOLDER_TYPE },
            { RECORD_CATEGORY_TYPE }
        };
    }

    /**
     * Data Provider with:
     * with the object types  for creating a Record Category Child
     *
     * @return record category child type
     */
    @DataProvider
    public static Object[][] categoryChild()
    {
        return new String[][] {
                    { RECORD_FOLDER_TYPE },
                    { FOLDER_TYPE },
                    { RECORD_CATEGORY_TYPE }
        };
    }

    /**
     * Invalid root level types, at unfiled record folder/unfiled containers container  level that shouldn't be possible to create
     */
    @DataProvider (name = "invalidRootTypes")
    public static Object[][] getInvalidRootTypes()
    {
        return new String[][]
                {
                        { FILE_PLAN_TYPE },
                        { RECORD_CATEGORY_TYPE },
                        { RECORD_FOLDER_TYPE },
                        { TRANSFER_CONTAINER_TYPE },
                        { TRANSFER_TYPE },
                        { UNFILED_CONTAINER_TYPE },

                };
    }


    public static final String ALFRESCO_ADMINISTRATORS = "ALFRESCO_ADMINISTRATORS";
}
