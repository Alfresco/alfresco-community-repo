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

import org.alfresco.repo.web.util.JettyComponent;

public class EnterprisePublicApiTestFixture extends EnterpriseTestFixture
{
	public final static String[] CONFIG_LOCATIONS = new String[]
    {
		"classpath:alfresco/application-context.xml",
		"classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml",
		"rest-api-test-context.xml",
		"testcmis-model-context.xml"
    };

	public final static String[] CLASS_LOCATIONS = new String[] {"classpath*:/publicapi/lucene/"};
	   
    private static EnterprisePublicApiTestFixture instance;

	/*
	 * Note: synchronized for multi-threaded test access
	 */
    public synchronized static EnterprisePublicApiTestFixture getInstance(boolean createTestData) throws Exception
    {
    	if(instance == null)
    	{
    		instance = new EnterprisePublicApiTestFixture();
    		instance.setup(createTestData);
    	}
    	return instance;
    }

	public static EnterprisePublicApiTestFixture getInstance() throws Exception
	{
		return getInstance(true);
	}

    private EnterprisePublicApiTestFixture()
	{
		super(CONFIG_LOCATIONS, CLASS_LOCATIONS, PORT, CONTEXT_PATH, PUBLIC_API_SERVLET_NAME, DEFAULT_NUM_MEMBERS_PER_SITE, false);
	}
    
	@Override
	protected JettyComponent makeJettyComponent()
	{
		JettyComponent jettyComponent = new EnterpriseJettyComponent(port, contextPath, configLocations, classLocations);
		return jettyComponent;
	}

	@Override
	protected RepoService makeRepoService() throws Exception
	{
		return new RepoService(applicationContext);
	}
}
