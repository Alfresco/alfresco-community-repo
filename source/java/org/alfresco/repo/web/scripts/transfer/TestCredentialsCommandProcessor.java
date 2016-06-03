
package org.alfresco.repo.web.scripts.transfer;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * This command processor is used simply to check that the transfer receiver is enabled and that the supplied
 * credentials are correct and identify an admin user.
 * 
 * @author brian
 *
 */
public class TestCredentialsCommandProcessor implements CommandProcessor
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.transfer.CommandProcessor#process(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public int process(WebScriptRequest req, WebScriptResponse resp)
    {
        //Since all the checks that are needed are actually carried out by the transfer web script, this processor
        //effectively becomes a no-op.
        int result = Status.STATUS_OK;
        resp.setStatus(result);
        return result;
    }

}
