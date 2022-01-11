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
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import static org.alfresco.repo.site.SiteServiceImpl.getSiteContainer;
import static org.alfresco.util.GUID.generate;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * Test class for MNT 19114, wiki page can not be created under RM site
 */
public class MNT19114Test extends BaseRMTestCase
{
    public static final String PARENT_NODE = "RMSite";
    public static final String DOCUMENT_LIBRARY_FOLDER_TYPE = "documentLibrary";
    public static final String SURF_CONFIG_FOLDER_TYPE = "surfConfigFolder";
    public static final String WIKI_PAGE_FOLDER_TYPE = "wikiPage";

    @Override
    protected boolean isRMSiteTest()
    {
        return true;
    }

    /**
     * Given a RM site and two folder type children
     * When creating a third folder type child as a Wiki page
     * The page will be created and no exception will be thrown.
     */
    public void testCreateWikiPageInRmSite() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef wikiPage;

            public void given()
            {
                // Creating a Records Management site
                siteService.createSite("rmSite", PARENT_NODE, generate(), generate(), SiteVisibility.PUBLIC, TYPE_RM_SITE);

                // Adding two immediate folder type children
                getSiteContainer(
                        PARENT_NODE,
                        DOCUMENT_LIBRARY_FOLDER_TYPE,
                        true,
                        siteService,
                        transactionService,
                        taggingService);
                getSiteContainer(
                        PARENT_NODE,
                        SURF_CONFIG_FOLDER_TYPE,
                        true,
                        siteService,
                        transactionService,
                        taggingService);
            }

            public void when() throws Exception
            {

                wikiPage = getSiteContainer(
                        PARENT_NODE,
                        WIKI_PAGE_FOLDER_TYPE,
                        true,
                        siteService,
                        transactionService,
                        taggingService);

            }

            public void then() throws Exception
            {
                // Check if the new folder type wiki page has been created
                assertEquals(true, nodeService.exists(wikiPage));
            }

            public void after()
            {
                siteService.deleteSite(PARENT_NODE);
            }
        });
    }
}
