package org.alfresco.opencmis;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * An Alfresco web script that handles dispatch of OpenCMIS requests.
 * 
 * @author steveglover
 *
 */
public class CMISWebScript extends AbstractWebScript
{
	private CMISDispatcherRegistry registry;
	
    public void setRegistry(CMISDispatcherRegistry registry)
    {
		this.registry = registry;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        CMISDispatcher dispatcher = registry.getDispatcher(req);
        if(dispatcher == null)
        {
        	res.setStatus(404);	
        }
        else
        {
        	dispatcher.execute(req, res);
        }
    }
}
