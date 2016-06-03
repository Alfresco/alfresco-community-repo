package org.alfresco.opencmis;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;

/**
 * Generates an OpenCMIS base url based on the request, repository id and binding.
 * 
 * @author steveglover
 *
 */
public interface BaseUrlGenerator
{
	String getContextPath(HttpServletRequest httpReq);
	String getServletPath(HttpServletRequest req);
	String getBaseUrl(HttpServletRequest req, String repositoryId, Binding binding);
	String getRequestURI(HttpServletRequest req, String repositoryId, String operation, String id);
}
