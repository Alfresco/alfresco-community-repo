/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.opencmis;

import java.util.Map;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.service.descriptor.Descriptor;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Wraps an OpenCMIS HttpServletRequest, mapping urls and adding servlet attributes specific to the Alfresco implementation of OpenCMIS.
 */
public class PublicApiCMISHttpServletRequest extends CMISHttpServletRequest
{
	public PublicApiCMISHttpServletRequest(WebScriptRequest req, String serviceName, BaseUrlGenerator baseUrlGenerator,
			Binding binding, Descriptor currentDescriptor)
	{
		super(req, serviceName, baseUrlGenerator, binding, currentDescriptor);
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