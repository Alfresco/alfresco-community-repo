package org.alfresco.opencmis;

import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Dispatches OpenCMIS requests to the OpenCMIS AtomPub servlet.
 * 
 * @author steveglover
 *
 */
public class PublicApiAtomPubCMISDispatcher extends AtomPubCMISDispatcher
{
	@Override
	protected CMISHttpServletRequest getHttpRequest(WebScriptRequest req)
	{
		String serviceName = getServiceName();
		CMISHttpServletRequest httpReqWrapper = new PublicApiCMISHttpServletRequest(req, serviceName, baseUrlGenerator,
		        getBinding(), getCurrentDescriptor(), tenantAdminService);
    	return httpReqWrapper;
	}
}