/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.CRUDTestGroup;
import org.apache.chemistry.opencmis.tck.tests.filing.FilingTestGroup;
import org.apache.chemistry.opencmis.tck.tests.query.ContentChangesSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.query.QuerySmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.CheckedOutTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersionDeleteTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningStateCreateTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.opencmis.tck.tests.query.QueryForObjectCustom;
import org.alfresco.opencmis.tck.tests.query.QueryInFolderTestCustom;
import org.alfresco.opencmis.tck.tests.query.QueryLikeTestCustom;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.util.JettyComponent;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;

/**
 * OpenCMIS TCK unit tests.
 * 
 * @author steveglover
 *
 */
public class TestEnterpriseAtomPubTCK extends AbstractEnterpriseOpenCMIS10TCKTest
{
    private static final String CMIS_URL = "http://{0}:{1}/{2}/cmisatom";
    protected static final Log logger = LogFactory.getLog(TestEnterpriseAtomPubTCK.class);

    @Before
    public void setup() throws Exception
    {
        JettyComponent jetty = getTestFixture().getJettyComponent();

        final SearchService searchService = (SearchService) jetty.getApplicationContext().getBean("searchService");
        ;
        final NodeService nodeService = (NodeService) jetty.getApplicationContext().getBean("nodeService");
        final FileFolderService fileFolderService = (FileFolderService) jetty.getApplicationContext().getBean("fileFolderService");
        final NamespaceService namespaceService = (NamespaceService) jetty.getApplicationContext().getBean("namespaceService");
        final TransactionService transactionService = (TransactionService) jetty.getApplicationContext().getBean("transactionService");
        final String name = "abc" + System.currentTimeMillis();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                Repository repositoryHelper = (Repository) jetty.getApplicationContext().getBean("repositoryHelper");
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                fileFolderService.create(companyHome, name, ContentModel.TYPE_FOLDER).getNodeRef();

                return null;
            }
        }, false, true);

        int port = jetty.getPort();
        Map<String, String> cmisParameters = new HashMap<String, String>();
        cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
        cmisParameters.put(TestParameters.DEFAULT_TEST_FOLDER_PARENT, "/" + name);
        clientContext = new OpenCMISClientContext(BindingType.ATOMPUB,
                MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco"), "admin", "admin", cmisParameters, jetty.getApplicationContext());

        overrideVersionableAspectProperties(jetty.getApplicationContext());
    }

    @Test
    public void testCMISTCKBasics() throws Exception
    {
        AlfrescoCMISBasicsTestGroup basicsTestGroup = new AlfrescoCMISBasicsTestGroup();
        JUnitHelper.run(basicsTestGroup);
    }

    @Test
    public void testCMISTCKCRUD() throws Exception
    {
        CRUDTestGroup crudTestGroup = new CRUDTestGroup();
        JUnitHelper.run(crudTestGroup);
    }

    @Test
    public void testCMISTCKVersioning() throws Exception
    {
        OverrideVersioningTestGroup versioningTestGroup = new OverrideVersioningTestGroup();
        JUnitHelper.run(versioningTestGroup);
    }

    @Test
    public void testCMISTCKFiling() throws Exception
    {
        FilingTestGroup filingTestGroup = new FilingTestGroup();
        JUnitHelper.run(filingTestGroup);
    }

    @Test
    public void testCMISTCKControl() throws Exception
    {
        ControlTestGroup controlTestGroup = new ControlTestGroup();
        JUnitHelper.run(controlTestGroup);
    }

    @Test
    @Category({LuceneTests.class, RedundantTests.class})
    public void testCMISTCKQuery() throws Exception
    {
        OverrideQueryTestGroup queryTestGroup = new OverrideQueryTestGroup();
        JUnitHelper.run(queryTestGroup);
    }

    private class OverrideVersioningTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("Versioning Test Group");
            setDescription("Versioning tests.");

            addTest(new VersioningSmokeTest());
            addTest(new VersionDeleteTest());
            addTest(new VersioningStateCreateTest());
            // relies on Solr being available
            addTest(new CheckedOutTest());
        }
    }

    private class OverrideQueryTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("Query Test Group");
            setDescription("Query and content changes tests.");

            addTest(new QuerySmokeTest());
            // The test fails on Lucene see MNT-11223
            // addTest(new QueryRootFolderTest());
            addTest(new QueryForObjectCustom());
            addTest(new QueryLikeTestCustom());
            addTest(new QueryInFolderTestCustom());
            addTest(new ContentChangesSmokeTest());
        }
    }
}
