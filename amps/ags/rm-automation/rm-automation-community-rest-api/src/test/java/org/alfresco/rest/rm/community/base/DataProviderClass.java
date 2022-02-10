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

import org.testng.annotations.DataProvider;

/**
 * Data Provider class used in tests
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class DataProviderClass
{
    /**
     * Data Provider with the special file plan components alias
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] getContainers()
    {
        return new String[][] {
                { FILE_PLAN_ALIAS },
                { TRANSFERS_ALIAS },
                { UNFILED_RECORDS_CONTAINER_ALIAS },
        };
    }

    /**
     * Data Provider with:
     * with the object types not allowed as children for a record category
     *
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] childrenNotAllowedForCategory()
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
    public static Object[][] folderTypes()
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
    public static Object[][] categoryTypes()
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
}
