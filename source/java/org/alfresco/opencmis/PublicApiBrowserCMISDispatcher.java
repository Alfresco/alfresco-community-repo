package org.alfresco.opencmis;

import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Cloud-specific browser binding OpenCMIS dispatcher.
 * 
 * @author steveglover
 *
 */
public class PublicApiBrowserCMISDispatcher extends BrowserCMISDispatcher
{
	@Override
	protected CMISHttpServletRequest getHttpRequest(WebScriptRequest req)
	{
		String serviceName = getServiceName();
		CMISHttpServletRequest httpReqWrapper = new PublicApiCMISHttpServletRequest(req, serviceName, baseUrlGenerator, getBinding(), getCurrentDescriptor());
    	return httpReqWrapper;
	}
}
