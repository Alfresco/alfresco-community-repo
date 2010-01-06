package org.alfresco.repo.imap.scripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class ServerStatusWebScript extends AbstractWebScript
{
    private boolean imapServerEnabled;

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        if (imapServerEnabled)
        {
            res.getWriter().write("enabled");
        }
        else
        {
            res.getWriter().write("disabled");
        }
        res.getWriter().flush();
        res.getWriter().close();
    }

    public void setImapServerEnabled(boolean imapServerEnabled)
    {
        this.imapServerEnabled = imapServerEnabled;
    }

}
