/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco;

import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
        org.alfresco.repo.web.scripts.permission.PermissionServiceTest.class,
        org.alfresco.repo.web.scripts.search.AdvancedSearchTest.class,
        org.alfresco.repo.web.scripts.ReadOnlyTransactionInGetSlingshotApiTest.class,
        org.alfresco.repo.wiki.WikiServiceImplTest.class,
        org.alfresco.slingshot.documentlibrary.FolderTemplateTest.class,
        org.alfresco.slingshot.web.scripts.SlingshotContentGetTest.class,
})
public class ShareServicesTestSuite
{
}
