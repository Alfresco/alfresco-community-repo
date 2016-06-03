
package org.alfresco.repo.web.scripts.transfer;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * @author brian
 *
 */
public interface CommandProcessor
{
    int process(WebScriptRequest req, WebScriptResponse resp);
}
