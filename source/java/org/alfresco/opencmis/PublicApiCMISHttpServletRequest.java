package org.alfresco.opencmis;

import java.util.Map;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.descriptor.Descriptor;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Wraps an OpenCMIS HttpServletRequest, mapping urls and adding servlet attributes specific to the Alfresco implementation of OpenCMIS.
 */
public class PublicApiCMISHttpServletRequest extends CMISHttpServletRequest
{
	public PublicApiCMISHttpServletRequest(WebScriptRequest req, String serviceName, BaseUrlGenerator baseUrlGenerator,
			Binding binding, Descriptor currentDescriptor, TenantAdminService tenantAdminService)
	{
		super(req, serviceName, baseUrlGenerator, binding, currentDescriptor, tenantAdminService);
	}

	protected void addAttributes()
	{
		super.addAttributes();

		Match match = req.getServiceMatch();
		Map<String, String> templateVars = match.getTemplateVars();
		String apiScope = templateVars.get("apiScope");
		String apiVersion = templateVars.get("apiVersion");

		if(apiScope != null)
		{
			httpReq.setAttribute("apiScope", apiScope);
		}

		if(apiVersion != null)
		{
			httpReq.setAttribute("apiVersion", apiVersion);
		}
	}

}