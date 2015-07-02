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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor;

import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;
import static org.alfresco.util.GUID.generate;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Classification enforcement pre method invocation test
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationEnforcementPreMethodInvocationTest extends BaseRMTestCase
{
    private String testUser;
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

    public void testClassificationEnforcementPreMethodInvocation()
    {
        /**
         * Given that I am a site manager and not cleared to see a document
         * When I try to set a property on that document an exception will be thrown
         * and I try to copy that document an exception will be thrown
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private NodeRef doc1;
            private NodeRef doc2;
            private String propertyValue = generate();

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                siteService.setMembership(collabSiteId, testUser, SITE_MANAGER);

                folder1 = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder1, generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder1, generate(), TYPE_CONTENT).getNodeRef();

                contentClassificationService.classifyContent(LEVEL1, generate(), generate(), newHashSet(REASON), doc1);
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
                        nodeService.setProperty(doc2, PROP_ADDRESSEE, propertyValue);
                        return null;
                    }
                }, testUser);

                doTestInTransaction(new FailureTest(AccessDeniedException.class)
                {
                    @Override
                    public void run() throws Exception
                    {
                        nodeService.setProperty(doc1, PROP_ADDRESSEE, propertyValue);
                    }
                }, testUser);

//                doTestInTransaction(new FailureTest(AccessDeniedException.class)
//                {
//                    @Override
//                    public void run() throws Exception
//                    {
//                        nodeService.getProperty(doc1, PROP_ADDRESSEE);
//                    }
//                }, testUser);

                doTestInTransaction(new FailureTest(AccessDeniedException.class)
                {
                    @Override
                    public void run() throws Exception
                    {
                        fileFolderService.copy(doc1, folder2, null);
                    }
                }, testUser);
            }
        });
    }
}
