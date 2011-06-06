/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.client;

import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * WebScript for CMIS links.
 */
public class CMISContentStreamWebScript extends AbstractWebScript
{
    public static final String DOC_ID = "id";
    public static final String STREAM_ID = "stream";
    public static final String CONNECTION = "conn";

    private CMISConnectionManager connectionManager;

    public void setConnectionManager(CMISConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        CMISConnection conn = null;

        String docId = req.getParameter(DOC_ID);
        String streamId = req.getParameter(STREAM_ID);

        String connectionId = req.getParameter(CONNECTION);

        if (connectionId != null)
        {
            conn = connectionManager.getConnection(connectionId);
        } else
        {
            conn = connectionManager.getConnection();
        }

        if (conn == null)
        {
            throw new WebScriptException(500, "Invalid connection!");
        }

        Session session = conn.getSession();

        // get object
        CmisObject object;
        try
        {
            object = session.getObject(docId);
        } catch (CmisBaseException e)
        {
            if (e instanceof CmisObjectNotFoundException)
            {
                throw new WebScriptException(404, "Object not found!", e);
            } else
            {
                throw new WebScriptException(500, e.getMessage(), e);
            }
        }

        if (!(object instanceof Document))
        {
            throw new WebScriptException(404, "Object is not a document!");
        }

        // get document content
        Document document = (Document) object;
        ContentStream stream = streamId == null ? document.getContentStream() : document.getContentStream(streamId);

        // stream content
        if (stream.getMimeType() != null)
        {
            res.setContentType(stream.getMimeType());
        }
        long length = stream.getLength();
        if (length != -1)
        {
            res.setHeader("Content-Length", Long.toString(length));
        }
        FileCopyUtils.copy(stream.getStream(), res.getOutputStream());
    }
}
