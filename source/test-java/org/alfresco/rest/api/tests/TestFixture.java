package org.alfresco.rest.api.tests;

import java.util.Iterator;

import org.alfresco.repo.web.util.JettyComponent;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.springframework.context.ApplicationContext;

public interface TestFixture
{
	public final static int PORT = 8081;
	public final static String CONTEXT_PATH = "/alfresco";
	public final static String PUBLIC_API_SERVLET_NAME = "api";
	
	ApplicationContext getApplicationContext();
	RepoService getRepoService();
	Iterator<TestNetwork> getNetworksIt();
	TestNetwork getRandomNetwork();
	Iterator<TestNetwork> networksIterator();
	JettyComponent getJettyComponent();
	TestNetwork getNetwork(String name);
}
