/*
 * #%L
 * Alfresco Search Services E2E Test
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

package org.alfresco.cmis.search;

import static lombok.AccessLevel.PROTECTED;

import lombok.Getter;
import org.alfresco.cmis.CmisWrapper;
import org.alfresco.dataprep.ContentService;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.network.ServerHealth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

@ContextConfiguration ("classpath:alfresco-cmis-context.xml")
public abstract class AbstractE2EFunctionalTest extends AbstractTestNGSpringContextTests
{
    /** The number of retries that a query will be tried before giving up. */
    protected static final int SEARCH_MAX_ATTEMPTS = 1;

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected TasProperties properties;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected DataSite dataSite;

    @Autowired
    protected DataContent dataContent;

    @Autowired
    protected RestWrapper restClient;

    @Autowired
    protected CmisWrapper cmisApi;

    @Autowired
    protected DataUser dataUser;

    protected UserModel testUser, adminUserModel;
    protected SiteModel testSite;

    protected static String unique_searchString;

    @BeforeClass (alwaysRun = true)
    public void setup()
    {
        serverHealth.assertServerIsOnline();

        adminUserModel = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser("UserSearch");

        testSite = new SiteModel(RandomData.getRandomName("SiteSearch"));
        testSite.setVisibility(Visibility.PRIVATE);

        testSite = dataSite.usingUser(testUser).createSite(testSite);

        unique_searchString = testSite.getTitle().replace("SiteSearch", "Unique");
    }
}
