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

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * A registry of OpenCMIS bindings to dispatchers.
 * 
 * @author steveglover
 *
 */
public class CMISDispatcherRegistryImpl implements CMISDispatcherRegistry
{
	private Map<Endpoint, CMISDispatcher> registry = new HashMap<Endpoint, CMISDispatcher>();

	@Override
	public void registerDispatcher(Endpoint endpoint, CMISDispatcher dispatcher)
	{
		registry.put(endpoint, dispatcher);
	}

	@Override
	public CMISDispatcher getDispatcher(WebScriptRequest req)
	{
		CMISDispatcher dispatcher = null;

		Match match = req.getServiceMatch();
		Map<String, String> templateVars = match.getTemplateVars();
		String bindingStr = templateVars.get("binding");
		String apiVersion = templateVars.get("apiVersion");
		if(bindingStr != null && apiVersion != null)
		{
			Binding binding = null;
			try
			{
				binding = Binding.valueOf(bindingStr);
			}
			catch(IllegalArgumentException e)
			{
				// nothing to do, binding remains null
			}
			
			if(binding != null)
			{
				Endpoint endpoint = new Endpoint(binding, apiVersion);
				dispatcher = registry.get(endpoint);
			}
			else
			{
				// TODO
			}
		}

		return dispatcher;
	}
}