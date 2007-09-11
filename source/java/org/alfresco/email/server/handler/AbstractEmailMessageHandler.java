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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
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

    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private PersonService personService;
    private SearchService searchService;
    private ContentService contentService;

    /**
     * @return Alfresco Content Service.
     */
    public ContentService getContentService()
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
     * @return Alfresco Authentication Component. 
     */
    public AuthenticationComponent getAuthenticationComponent()
    {
        return authenticationComponent;
    }

    /**
     * @param authenticationComponent Alfresco Authentication Component.
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * @return Alfresco Authentication Service.
     */
    public AuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    /**
     * @param authenticationService Alfresco Authentication Service.
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @return Alfresco Node Service.
     */
    public NodeService getNodeService()
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
     * @return Alfesco Person Service. 
     */
    public PersonService getPersonService()
    {
        return personService;
    }

    /**
     * @param personService Alfresco Person Service.
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * @return Alfresco Search Service.
     */
    public SearchService getSearchService()
    {
        return searchService;
    }

    /**
     * @param searchService Alfresco Search Service.
     */
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
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
     * @param contentType MIME content type. For exaple you can set this parameter to "text/html" or "text/xml", etc.
     */
    protected void writeContent(NodeRef nodeRef, String content, String contentType)
    {
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        writeContent(nodeRef, inputStream, contentType, "UTF-8");
    }

    /**
     * Write content to the node from InputStream.
     * 
     * @param nodeRef Target node.
     * @param content Content stream.
     * @param contentType MIME content type.
     * @param encoding Encoding. Can be null for non text based content.
     */
    protected void writeContent(NodeRef nodeRef, InputStream content, String contentType, String encoding)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Write content (MimeType=\"" + contentType + "\", Encoding=\"" + encoding + "\"");
        }
        ContentService contentService = getContentService();
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(contentType);
        writer.setEncoding(encoding);
        writer.putContent(content);
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
