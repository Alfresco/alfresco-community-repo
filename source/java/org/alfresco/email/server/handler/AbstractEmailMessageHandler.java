/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.email.server.handler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.auth.InvalidArgumentException;

/**
 * Abstract class implements common logic for processing email messages.
 * 
 * @author maxim
 * @since 2.2
 */
public abstract class AbstractEmailMessageHandler implements EmailMessageHandler
{
    private static final Log log = LogFactory.getLog(EmailMessageHandler.class);

    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;

    /**
     * @return Alfresco Content Service.
     */
    protected ContentService getContentService()
    {
        return contentService;
    }

    /**
     * @param contentService Alfresco Content Service.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @return                      the Alfresco dictionary service
     */
    protected DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * @param dictionaryService     Alfresco dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return Alfresco Node Service.
     */
    protected NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @param nodeService Alfresco Node Service.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService Alfresco Search Service.
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @return      the service used to determine mimeypte and encoding
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * @param mimetypeService       the the service to determine mimetype and encoding
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @param to Email address which user part specifies node-dbid
     * @return Referance to requested node.
     * @throws InvalidArgumentException The exception is thrown if input string has incorrect format or empty.
     */
    protected NodeRef getTargetNode(String to) throws InvalidArgumentException
    {
        if (to == null || to.length() == 0)
        {
            throw new InvalidArgumentException("Input string has to contain email address.");
        }
        String[] parts = to.split("@");
        if (parts.length != 2)
        {
            throw new InvalidArgumentException("Incorrect email address format.");
        }
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = "@sys\\:node-dbid:" + parts[0];
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() == 1)
        {
            return resultSet.getNodeRef(0);
        }
        return null;
    }

    /**
     * Write the content to the node 
     * 
     * @param nodeRef Target node
     * @param content Content
     */
    protected void writeContent(NodeRef nodeRef, String content)
    {
        writeContent(nodeRef, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
    }

    /**
     * Write the string as content to the node.
     * 
     * @param nodeRef Target node.
     * @param content Text for writting.
     * @param mimetype MIME content type. For exaple you can set this parameter to "text/html" or "text/xml", etc.
     */
    protected void writeContent(NodeRef nodeRef, String content, String mimetype)
    {
        try
        {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
            writeContent(nodeRef, inputStream, mimetype, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AlfrescoRuntimeException("Failed to write content", e);
        }
    }

    /**
     * Write content to the node from InputStream.
     * 
     * @param nodeRef Target node.
     * @param content Content stream.
     * @param mimetype MIME content type.
     * @param encoding Encoding. Can be null for non text based content.
     */
    protected void writeContent(NodeRef nodeRef, InputStream content, String mimetype, String encoding)
    {
        InputStream bis = new BufferedInputStream(content, 4092);
        
        // Guess the encoding if it is text
        if (mimetypeService.isText(mimetype))
        {
            ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
            encoding = charsetFinder.getCharset(bis, mimetype).name();
        }
        else if (encoding == null)
        {
            encoding = "UTF-8";
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Write content (MimeType=\"" + mimetype + "\", Encoding=\"" + encoding + "\"");
        }
        
        
        ContentService contentService = getContentService();
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding(encoding);
        writer.putContent(bis);
    }
    
    /**
     * Add emailed aspect to the specified node.
     * 
     * @param nodeService Alfresco Node Service.
     * @param nodeRef Target node.
     * @param mailParser Mail message that will be used for extracting necessary information
     */
    protected void addEmailedAspect(NodeRef nodeRef, EmailMessage message) 
    {
        Map<QName, Serializable> emailProps = new HashMap<QName, Serializable>();
        emailProps.put(ContentModel.PROP_SENTDATE, message.getSentDate());
        emailProps.put(ContentModel.PROP_ORIGINATOR, message.getFrom());
        emailProps.put(ContentModel.PROP_ADDRESSEE, message.getTo());
        emailProps.put(ContentModel.PROP_SUBJECT, message.getSubject());
        nodeService.addAspect(nodeRef, EmailServerModel.ASPECT_EMAILED, emailProps);
        
        if (log.isDebugEnabled())
        {
            log.debug("Emailed aspect has been added.");
        }
    }
}
