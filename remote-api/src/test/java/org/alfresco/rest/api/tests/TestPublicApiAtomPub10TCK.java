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
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningTestGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.opencmis.tck.tests.query.QueryForObjectCustom;
import org.alfresco.opencmis.tck.tests.query.QueryInFolderTestCustom;
import org.alfresco.opencmis.tck.tests.query.QueryLikeTestCustom;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;

/**
 * OpenCMIS TCK unit tests.
 * 
 * @author steveglover
 *
 */
public class TestPublicApiAtomPub10TCK extends AbstractEnterpriseOpenCMIS10TCKTest
{
    private static final String CMIS_URL = "http://{0}:{1}/{2}/api/{3}/{4}/cmis/versions/1.0/atom";
    protected static final Log logger = LogFactory.getLog(TestPublicApiAtomPub10TCK.class);

    @Before
    public void before() throws Exception
    {
        // see REPO-1524
        // the tests are always run on PostgreSQL only
        // Dialect dialect = (Dialect) applicationContext.getBean("dialect");
        // assumeFalse(dialect instanceof Oracle9Dialect);

        int port = getTestFixture().getJettyComponent().getPort();
        TestNetwork network = getTestFixture().getRandomNetwork();
        Map<String, String> cmisParameters = new HashMap<String, String>();
        cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
        clientContext = new OpenCMISClientContext(BindingType.ATOMPUB,
                MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco", network.getId(), "public"),
                "admin@" + network.getId(), "admin", cmisParameters, getTestFixture().getJettyComponent().getApplicationContext());
        overrideVersionableAspectProperties(getTestFixture().getJettyComponent().getApplicationContext());
    }

    @AfterClass
    public static void shutdown() throws Exception
    {}

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
        VersioningTestGroup versioningTestGroup = new VersioningTestGroup();
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

    // private class OverrideVersioningTestGroup extends AbstractSessionTestGroup
    // {
    // @Override
    // public void init(Map<String, String> parameters) throws Exception
    // {
    // super.init(parameters);
    //
    // setName("Versioning Test Group");
    // setDescription("Versioning tests.");
    //
    // addTest(new VersioningSmokeTest());
    // addTest(new VersionDeleteTest());
    // addTest(new VersioningStateCreateTest());
    // // relies on Solr being available
    // addTest(new CheckedOutTest());
    // }
    // }

    private class OverrideQueryTestGroup extends AbstractSessionTestGroup
    {
        @Override
        public void init(Map<String, String> parameters) throws Exception
        {
            super.init(parameters);

            setName("Query Test Group");
            setDescription("Query and content changes tests.");

            // this is failing because of an MT issue (the thread is a specific tenant but the DB metadata query is searching
            // against the workspace://SpacesStore)
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
