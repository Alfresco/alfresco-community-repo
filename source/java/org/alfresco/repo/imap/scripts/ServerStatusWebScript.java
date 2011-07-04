/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.imap.scripts;

import java.io.IOException;

import org.alfresco.repo.imap.ImapService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Shows the availability of the IMAP server via web script request.
 */
public class ServerStatusWebScript extends AbstractWebScript
{
    private ImapService imapService;

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        if (imapService.getImapServerEnabled())
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

    public void setImapService(ImapService imapService)
    {
        this.imapService = imapService;
    }

}
