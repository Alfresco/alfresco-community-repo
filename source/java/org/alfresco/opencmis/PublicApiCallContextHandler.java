package org.alfresco.opencmis;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler;

public class PublicApiCallContextHandler extends BasicAuthCallContextHandler
{
    private static final long serialVersionUID = 8877878113507734452L;

    @Override
	public Map<String, String> getCallContextMap(HttpServletRequest request)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put("isPublicApi", "true");
		return map;
	}
}
