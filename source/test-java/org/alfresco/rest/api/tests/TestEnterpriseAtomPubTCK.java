/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.util.JettyComponent;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.CRUDTestGroup;
import org.apache.chemistry.opencmis.tck.tests.filing.FilingTestGroup;
import org.apache.chemistry.opencmis.tck.tests.query.QueryTestGroup;
import org.apache.chemistry.opencmis.tck.tests.versioning.CheckedOutTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersionDeleteTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningStateCreateTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

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

	private static NodeRef getCompanyHome(NodeService nodeService, SearchService searchService, NamespaceService namespaceService)
	{
        NodeRef storeRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(
                storeRootNodeRef,
                "/app:company_home",
                null,
                namespaceService,
                false,
                SearchService.LANGUAGE_XPATH);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Didn't find Company Home");
        }
        NodeRef companyHomeNodeRef = results.get(0);
        return companyHomeNodeRef;
	}

	@Before
	public void setup() throws Exception
	{
		JettyComponent jetty = getTestFixture().getJettyComponent();
		
		final SearchService searchService = (SearchService)jetty.getApplicationContext().getBean("searchService");;
		final NodeService nodeService = (NodeService)jetty.getApplicationContext().getBean("nodeService");
		final FileFolderService fileFolderService = (FileFolderService)jetty.getApplicationContext().getBean("fileFolderService");
		final NamespaceService namespaceService = (NamespaceService)jetty.getApplicationContext().getBean("namespaceService");
		final TransactionService transactionService = (TransactionService)jetty.getApplicationContext().getBean("transactionService");
		final String name = "abc" + System.currentTimeMillis();

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
		{
			@Override
			public Void execute() throws Throwable
			{
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

				NodeRef companyHome = getCompanyHome(nodeService, searchService, namespaceService);
				fileFolderService.create(companyHome, name, ContentModel.TYPE_FOLDER).getNodeRef();

				return null;
			}
		}, false, true);

    	int port = jetty.getPort();
    	Map<String, String> cmisParameters = new HashMap<String, String>();
    	cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
    	cmisParameters.put(TestParameters.DEFAULT_TEST_FOLDER_PARENT, "/" + name);
    	clientContext = new OpenCMISClientContext(BindingType.ATOMPUB,
    			MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco"), "admin", "admin", cmisParameters);
	}

    @Test
    public void testCMISTCKBasics() throws Exception
    {
        BasicsTestGroup basicsTestGroup = new BasicsTestGroup();
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
    public void testCMISTCKQuery() throws Exception
    {
        QueryTestGroup queryTestGroup = new QueryTestGroup();
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
}
