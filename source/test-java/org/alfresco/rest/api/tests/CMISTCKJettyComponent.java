package org.alfresco.rest.api.tests;

import org.alfresco.repo.web.util.AbstractJettyComponent;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Manages an embedded jetty server, hooking it up to the repository spring context and providing access to the Chemistry OpenCMIS servlet.
 * 
 * @author steveglover
 *
 */
public class CMISTCKJettyComponent extends AbstractJettyComponent
{
	public CMISTCKJettyComponent(int port, String contextPath, String servletName, String[] configLocations, String[] classLocations)
	{
		super(port, contextPath, configLocations, classLocations);
	}
	
	@Override
	protected void configureWebAppContext(WebAppContext webAppContext)
	{
		super.configure(webAppContext);

	    // the Chemistry OpenCMIS servlet
	    ServletHolder servletHolder = new ServletHolder(CmisAtomPubServlet.class);
	    servletHolder.setInitParameter("callContextHandler", "org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler");
	    webAppContext.addServlet(servletHolder, "/cmisatom/*");
	}
}
