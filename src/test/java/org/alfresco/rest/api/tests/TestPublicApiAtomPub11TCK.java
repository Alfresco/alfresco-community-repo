/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.junit.Assume.assumeFalse;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoOracle9Dialect;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.junit.AfterClass;
import org.junit.Before;

/**
 * OpenCMIS TCK unit tests.
 * 
 * @author steveglover
 *
 */
public class TestPublicApiAtomPub11TCK extends AbstractEnterpriseOpenCMIS11TCKTest
{
	private static final String CMIS_URL = "http://{0}:{1}/{2}/api/{3}/{4}/cmis/versions/1.1/atom";
	protected static final Log logger = LogFactory.getLog(TestPublicApiAtomPub11TCK.class);

	@Before
	public void before() throws Exception
	{
        //see REPO-1524	
        Dialect dialect = (Dialect) applicationContext.getBean("dialect");
        assumeFalse(dialect instanceof AlfrescoOracle9Dialect); 

		int port = getTestFixture().getJettyComponent().getPort();
		TestNetwork network = getTestFixture().getRandomNetwork();
    	Map<String, String> cmisParameters = new HashMap<String, String>();
    	cmisParameters.put(TestParameters.DEFAULT_RELATIONSHIP_TYPE, "R:cm:replaces");
        cmisParameters.put(TestParameters.DEFAULT_SECONDARY_TYPE, "P:cm:author");
        cmisParameters.put(TestParameters.DEFAULT_ITEM_TYPE, "I:cm:cmobject");
    	clientContext = new OpenCMISClientContext(BindingType.ATOMPUB,
    			MessageFormat.format(CMIS_URL, "localhost", String.valueOf(port), "alfresco", network.getId(), "public"),
    			"admin@" + network.getId(), "admin", cmisParameters, getTestFixture().getJettyComponent().getApplicationContext());
        overrideVersionableAspectProperties(getTestFixture().getJettyComponent().getApplicationContext());
	}

	@AfterClass
	public static void shutdown() throws Exception
	{
	}
}