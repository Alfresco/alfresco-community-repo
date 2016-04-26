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