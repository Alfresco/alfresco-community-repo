package org.alfresco.rest.api.tests;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.CRUDTestGroup;
import org.apache.chemistry.opencmis.tck.tests.filing.FilingTestGroup;
import org.apache.chemistry.opencmis.tck.tests.query.ContentChangesSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.query.QueryForObject;
import org.apache.chemistry.opencmis.tck.tests.query.QueryLikeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.CheckedOutTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersionDeleteTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningStateCreateTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

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
		int port = getTestFixture().getJettyComponent().getPort();
		TestNetwork network = getTestFixture().getRandomNetwork();
    	Map<String, String> cmisParameters = new HashMap<String, String>();
    	cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
    	clientContext = new OpenCMISClientContext(BindingType.ATOMPUB,
    			MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco", network.getId(), "public"),
    			"admin@" + network.getId(), "admin", cmisParameters);
    	
	}

	@AfterClass
	public static void shutdown() throws Exception
	{
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

            // this is failing because of an MT issue (the thread is a specific tenant but the DB metadata query is searching
            // against the workspace://SpacesStore)
//            addTest(new QuerySmokeTest());
            // The test fails on Lucene see MNT-11223
//            addTest(new QueryRootFolderTest());
            addTest(new QueryForObject());
            addTest(new QueryLikeTest());
            addTest(new ContentChangesSmokeTest());
        }
    }
}