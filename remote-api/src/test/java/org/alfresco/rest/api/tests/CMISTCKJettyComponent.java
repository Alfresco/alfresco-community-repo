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

import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import org.alfresco.repo.web.util.AbstractJettyComponent;

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
