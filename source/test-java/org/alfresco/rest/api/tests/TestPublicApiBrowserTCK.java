package org.alfresco.rest.api.tests;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;

/**
 * OpenCMIS TCK unit tests.
 * 
 * @author steveglover
 *
 */
public class TestPublicApiBrowserTCK extends AbstractEnterpriseOpenCMISTCKTest
{
	private static final String CMIS_URL = "http://{0}:{1}/{2}/api/{3}/{4}/cmis/versions/1.0/browser";
	protected static final Log logger = LogFactory.getLog(TestPublicApiBrowserTCK.class);

	@Before
	public void before() throws Exception
	{
		int port = getTestFixture().getJettyComponent().getPort();
		TestNetwork network = getTestFixture().getRandomNetwork();
    	Map<String, String> cmisParameters = new HashMap<String, String>();
    	cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
    	clientContext = new OpenCMISClientContext(BindingType.BROWSER,
    			MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco", network.getId(), "public"),
    			"admin@" + network.getId(), "admin", cmisParameters);
    	
	}
}