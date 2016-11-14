/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.base;

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.HOLD_CONTAINER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;

import org.testng.annotations.DataProvider;

/**
 * Test data used in tests
 *
 * @author Rodica Sutu
 * @since 1.0
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
                { FILE_PLAN_ALIAS.toString() },
                { TRANSFERS_ALIAS.toString() },
                { HOLDS_ALIAS.toString() },
                { UNFILED_RECORDS_CONTAINER_ALIAS.toString() },
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
    public static String CATEGORY_NAME = "CATEGORY NAME"+ getRandomAlphanumeric();

    /**
     * The default CATEGORY title used when creating categories
     */
    public static String CATEGORY_TITLE = "CATEGORY TITLE" + getRandomAlphanumeric();

    /**
     * The default FOLDER name used when creating categories
     */
    public static String FOLDER_NAME = "FOLDER NAME" + getRandomAlphanumeric();

    /**
     * The default FOLDER title used when creating categories
     */
    public static  String FOLDER_TITLE = "FOLDER TITLE" + getRandomAlphanumeric();
}
