package org.alfresco.opencmis;

import javax.servlet.http.HttpServlet;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;

/**
 * Dispatches OpenCMIS requests to the OpenCMIS Browser Binding servlet.
 * 
 * @author steveglover
 *
 */
public class BrowserCMISDispatcher extends CMISServletDispatcher
{
    @Override
    protected Binding getBinding()
    {
    	return Binding.browser;
    }

	protected HttpServlet getServlet()
	{
		HttpServlet servlet = new CmisBrowserBindingServlet();
		return servlet;
	}
}
