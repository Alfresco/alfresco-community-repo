package org.alfresco.rest.api.tests;

import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Manages an embedded jetty server, hooking it up to the repository spring context and providing 
 * authenticated, tenant-based access through the tenant servlet.
 * 
 * @author steveglover
 *
 */
public class EnterpriseJettyComponent extends PublicApiJettyComponent
{
	public EnterpriseJettyComponent(int port, String contextPath, String[] configLocations, String[] classLocations)
	{
		super(port, contextPath, configLocations, classLocations);
	}
	
	@Override
	protected void configureWebAppContext(WebAppContext webAppContext)
	{
		super.configureWebAppContext(webAppContext);

	    // the tenant servlet with alfresco managed authentication
	    ServletHolder servletHolder = new ServletHolder(CmisAtomPubServlet.class);
	    servletHolder.setInitParameter("callContextHandler", "org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler");
	    webAppContext.addServlet(servletHolder, "/cmisatom/*");
	}
}
