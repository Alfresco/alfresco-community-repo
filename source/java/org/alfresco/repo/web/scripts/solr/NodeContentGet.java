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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * A web service to return the text content (transformed if required) of a node's
 * content property.
 * 
 * @since 4.0
 */
public class NodeContentGet extends StreamContent
{
    private static final String TRANSFORM_STATUS_HEADER = "X-Alfresco-transformStatus";
    private static final String TRANSFORM_EXCEPTION_HEADER = "X-Alfresco-transformException";
    private static final String TRANSFORM_DURATION_HEADER = "X-Alfresco-transformDuration";
    
    private static final Log logger = LogFactory.getLog(NodeContentGet.class);

    /**
     * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3 
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private ContentService contentService;

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @in
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        ContentReader textReader = null;
        Exception transformException = null;

        String nodeIDString = req.getParameter("nodeId");
        if(nodeIDString == null)
        {
            throw new WebScriptException("nodeID parameter is required for GetNodeContent");
        }
        long nodeId = Long.valueOf(nodeIDString).longValue();

        String propertyQName = req.getParameter("propertyQName");
        QName propertyName = null;
        if(propertyQName == null)
        {
            propertyName = ContentModel.PROP_CONTENT;
        }
        else
        {
            propertyName = QName.createQName(propertyQName);
        }
        Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeId);
        if(pair == null)
        {
            throw new WebScriptException("Node id does not exist");
        }
        NodeRef nodeRef = pair.getSecond();

        // check If-Modified-Since header and set Last-Modified header as appropriate
        Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        long modifiedSince = -1;
        String modifiedSinceStr = req.getHeader("If-Modified-Since");
        if(modifiedSinceStr != null)
        {
            try
            {
                modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
            }
            catch (Throwable e)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
                }
            }
            
            if (modifiedSince > 0L)
            {
                // round the date to the ignore millisecond value which is not supplied by header
                long modDate = (modified.getTime() / 1000L) * 1000L;
                if (modDate <= modifiedSince)
                {
                    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
        }

        ContentReader reader = contentService.getReader(nodeRef, propertyName);
        if(reader == null)
        {
            res.setStatus(HttpStatus.SC_NO_CONTENT);
            return;            
        }

        // Perform transformation catering for mimetype AND encoding
        ContentWriter writer = contentService.getTempWriter();
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");                            // Expect transformers to produce UTF-8

        ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
        if(transformer == null)
        {
            res.setHeader(TRANSFORM_STATUS_HEADER, "noTransform");
            res.setStatus(HttpStatus.SC_NO_CONTENT);
            return;
        }

        try
        {
        	long start = System.currentTimeMillis();
            transformer.transform(reader, writer);
            long transformDuration = System.currentTimeMillis() - start;
            res.setHeader(TRANSFORM_DURATION_HEADER, String.valueOf(transformDuration));
        }
        catch (ContentIOException e)
        {
            transformException = e;
        }

        if(transformException == null)
        {
            // point the reader to the new-written content
            textReader = writer.getReader();
            // Check that the reader is a view onto something concrete
            if (textReader == null || !textReader.exists())
            {
                transformException = new ContentIOException(
                        "The transformation did not write any content, yet: \n"
                        + "   transformer:     " + transformer + "\n" + "   temp writer:     " + writer);
            }
        }

        if(transformException != null)
        {
            res.setHeader(TRANSFORM_STATUS_HEADER, "transformFailed");
            res.setHeader(TRANSFORM_EXCEPTION_HEADER, transformException.getMessage());
            res.setStatus(HttpStatus.SC_NO_CONTENT);
        }
        else
        {
            res.setStatus(HttpStatus.SC_OK);
            streamContentImpl(req, res, textReader, false, modified, String.valueOf(modified.getTime()), null);            
        }
    }
}
