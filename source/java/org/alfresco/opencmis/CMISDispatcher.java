package org.alfresco.opencmis;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Dispatches OpenCMIS requests to the appropriate handler.
 * 
 * @author steveglover
 *
 */
public interface CMISDispatcher
{
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException;
}
