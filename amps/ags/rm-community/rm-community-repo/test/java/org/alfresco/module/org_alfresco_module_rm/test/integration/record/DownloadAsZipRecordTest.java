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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import net.sf.acegisecurity.Authentication;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Download as zip record test.
 * <pre>Tests for <a href="https://issues.alfresco.com/jira/browse/MNT-21292">MNT-21292</a> </pre>
 * @author Rodica Sutu
 * @since 3.2.0.1
 */
public class DownloadAsZipRecordTest extends BaseRMTestCase
{
    private DownloadService downloadService;

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * @see BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        downloadService = (DownloadService) applicationContext.getBean("DownloadService");
    }

    /**
     * Given a record and a user without view record capability
     * When the user downloads the record
     * Then Access Denied exception is thrown
     */
    public void testDownloadRecordUserNoReadCapability()
    {

        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            /** user with no view record capability */
            String userDownload;
            Authentication previousAuthentication;

            public void given()
            {
                // create an inplace record
                AuthenticationUtil.runAs((RunAsWork<Void>) () -> {
                    recordService.createRecord(filePlan, dmDocument);
                    return null;
                }, AuthenticationUtil.getAdminUserName());
                // create user
                userDownload = GUID.generate();
                createPerson(userDownload);
            }

            public void when()
            {
                previousAuthentication = AuthenticationUtil.getFullAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(userDownload);
                downloadService.createDownload(new NodeRef[] { dmDocument }, true);
            }

            public void after()
            {
                AuthenticationUtil.setFullAuthentication(previousAuthentication);
                personService.deletePerson(userDownload);
            }
        });
    }

    /**
     * Given a record and a user with view record capability
     * When the user downloads the record
     * Then download node is created
     */
    public void testDownloadRecordUserWithReadCapability()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef downloadStorageNode;

            public void given()
            {
                // Create an inplace record
                AuthenticationUtil.runAs((RunAsWork<Void>) () -> {
                    // Declare record
                    recordService.createRecord(filePlan, dmDocument);
                    return null;
                }, dmCollaborator);
            }

            public void when()
            {
                Authentication previousAuthentication = AuthenticationUtil.getFullAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(dmCollaborator);
                //  request to download the record
                downloadStorageNode = downloadService.createDownload(new NodeRef[] { dmDocument }, true);
                AuthenticationUtil.setFullAuthentication(previousAuthentication);
            }

            public void then()
            {
                // check the download storage node is created
                assertTrue(nodeService.exists(downloadStorageNode));
            }
        });
    }
}
