package org.alfresco.opencmis;

import javax.servlet.http.HttpServlet;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;

/**
 * Dispatches OpenCMIS requests to the OpenCMIS AtomPub servlet.
 * 
 * @author steveglover
 *
 */
public class AtomPubCMISDispatcher extends CMISServletDispatcher
{
    @Override
    protected Binding getBinding()
    {
    	return Binding.atom;
    }

	protected HttpServlet getServlet()
	{
		HttpServlet servlet = new CmisAtomPubServlet();
		return servlet;
	}
}
