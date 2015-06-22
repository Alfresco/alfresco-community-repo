/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification;

import static java.lang.Integer.MAX_VALUE;
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI;
import static org.alfresco.util.GUID.generate;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Integration test for ResultTest post method invocation processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ResultSetPostMethodInvocationProcessorTest extends BaseRMTestCase
{
    private static final String LEVEL1 = "level1";
    private static final String REASON = "Test Reason 1";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    public void testResultSetPostMethodInvocationProcessor()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef doc1;
            private NodeRef doc2;
            private String searchQuery = generate();
            private ResultSet result;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                siteService.setMembership(collabSiteId, myUser, SITE_MANAGER);

                doc1 = fileFolderService.create(documentLibrary, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(documentLibrary, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        //contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), doc1);

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        SearchParameters searchParameters = new SearchParameters();
                        searchParameters.setQuery("@cm\\:name:" + searchQuery + "*");
                        searchParameters.setLanguage(LANGUAGE_FTS_ALFRESCO);
                        searchParameters.addStore(STORE_REF_WORKSPACE_SPACESSTORE);
                        searchParameters.setMaxItems(MAX_VALUE);
                        searchParameters.setNamespace(CONTENT_MODEL_1_0_URI);
                        result = searchService.query(searchParameters);

                        return null;
                    }
                }, myUser);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#then()
             */
            @Override
            public void then() throws Exception
            {
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<NodeRef> nodeRefs = result.getNodeRefs();

                        assertEquals(2, nodeRefs.size());
                        assertTrue(nodeRefs.contains(doc1));
                        assertTrue(nodeRefs.contains(doc2));

                        return null;
                    }
                }, myUser);
            }
        });
    }
}
