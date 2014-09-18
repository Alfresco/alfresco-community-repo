package org.alfresco.rest.api.tests;

import org.alfresco.repo.web.util.JettyComponent;

public class TCKEnterpriseTestFixture extends EnterpriseTestFixture
{
	public final static String[] CONFIG_LOCATIONS = new String[]
    {
		"classpath:alfresco/application-context.xml",
		"classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml",
		"rest-api-test-context.xml"
    };

	public final static String[] CLASS_LOCATIONS = new String[] {"classpath*:/publicapi/lucene/"};

    private static TCKEnterpriseTestFixture instance;

	/*
	 * Note: synchronized for multi-threaded test access
	 */
    public synchronized static TCKEnterpriseTestFixture getInstance() throws Exception
    {
    	if(instance == null)
    	{
    		instance = new TCKEnterpriseTestFixture();
    		instance.setup();
    	}
    	return instance;
    }

    private TCKEnterpriseTestFixture()
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