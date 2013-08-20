package org.alfresco.opencmis;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.server.shared.CallContextHandler;

public class PublicApiCallContextHandler implements CallContextHandler
{
	@Override
	public Map<String, String> getCallContextMap(HttpServletRequest request)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put("isPublicApi", "true");
		return map;
	}
}
