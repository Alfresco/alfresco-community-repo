/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.ContentFormat;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.Attachments;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the ContentService. The WSDL for this service
 * can be accessed from http://localhost:8080/alfresco/wsdl/content-service.wsdl
 * 
 * @author gavinc
 */
public class ContentWebService extends AbstractWebService implements
        ContentServiceSoapPort
{
    private static Log logger = LogFactory.getLog(ContentWebService.class);

    private static final String BROWSER_URL = "{0}://{1}{2}/download/direct/{3}/{4}/{5}/{6}";

    /**
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#read(org.alfresco.repo.webservice.types.Reference)
     */
    public Content[] read(final Predicate items, final String property)
            throws RemoteException, ContentFault
    {
        try
        {
            RetryingTransactionCallback<Content[]> callback = new RetryingTransactionCallback<Content[]>()
            {
                public Content[] execute() throws Throwable
                {
                    // resolve the predicates
                    List<NodeRef> nodes = Utils.resolvePredicate(items, nodeService, searchService, namespaceService);
                    List<Content> results = new ArrayList<Content>(nodes.size());
                    for (NodeRef nodeRef : nodes)
                    {   
                        // Add content to the result
                        results.add(createContent(nodeRef, property));
                    }

                    return results.toArray(new Content[results.size()]);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new ContentFault(0, e.getMessage());
        }
    }
    
    /**
     * Create the content object
     * 
     * @param nodeRef       the node reference
     * @param property      the content property
     * @return              the content object
     * @throws UnsupportedEncodingException
     */
    private Content createContent(NodeRef nodeRef, String property)
        throws UnsupportedEncodingException
    {
        Content content = null;
        
        // Lets have a look and see if this node has any content on this node
        ContentReader contentReader = this.contentService.getReader(nodeRef, QName.createQName(property));
        
        if (contentReader != null)
        {
            // Work out what the server, port and context path are
            HttpServletRequest req = (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
    
            String address = req.getServerName();
            if (req.getLocalPort() != 80)
            {
                address = address + ":" + req.getServerPort(); 
            }
    
            // Get the file name
            String filename = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
            // Filename may not exist if this node isn't a cm:object 
            if (filename == null) 
            { 
              filename = "file.bin"; 
            }
            
            // format the URL that can be used to download the content
            String downloadUrl = MessageFormat.format(BROWSER_URL,
                    new Object[] { req.getScheme(), address,
                            req.getContextPath(),
                            nodeRef.getStoreRef().getProtocol(),
                            nodeRef.getStoreRef().getIdentifier(),
                            nodeRef.getId(),
                            URLEncoder.encode(filename, "UTF-8") });
            
            // Create the content object
            ContentFormat format = new ContentFormat(contentReader.getMimetype(), contentReader.getEncoding());
            content = new Content(Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef), property, contentReader.getSize(), format, downloadUrl);
            
            // Debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Content: " + nodeRef.getId() + " name="
                        + filename + " encoding="
                        + content.getFormat().getEncoding() + " mimetype="
                        + content.getFormat().getMimetype() + " size="
                        + content.getLength() + " downloadURL="
                        + content.getUrl());
            }
        }
        else
        {
            // Create an empty content object
            content = new Content(Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef), property, 0, null, null);
            
            // Debug
            if (logger.isDebugEnabled())
            {
                logger.debug("No content found: " + nodeRef.getId());
            }
        }        
        
        return content;
    }

    /**
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#write(org.alfresco.repo.webservice.types.Reference,
     *      byte[])
     */
    public Content write(final Reference node, final String property, final byte[] content, final ContentFormat format) 
        throws RemoteException, ContentFault
    {
        try
        {
            RetryingTransactionCallback<Content> callback = new RetryingTransactionCallback<Content>()
            {
                public Content execute() throws Throwable
                {
                    // create a NodeRef from the parent reference
                    NodeRef nodeRef = Utils.convertToNodeRef(node, nodeService, searchService, namespaceService);

                    // Get the content writer
                    ContentWriter writer = contentService.getWriter(nodeRef, QName.createQName(property), true);
                    
                    // Set the content format details (if they have been specified)
                    if (format != null)
                    {
                        writer.setEncoding(format.getEncoding());
                        writer.setMimetype(format.getMimetype());
                    }
                    
                    // Write the content 
                    InputStream is = new ByteArrayInputStream(content);
                    writer.putContent(is);

                    // Debug
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Updated content for node with id: " + nodeRef.getId());
                    }

                    // Return the content object
                    return createContent(nodeRef, property);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new ContentFault(0, e.getMessage());
        }
    }
    
    /**
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#writeWithAttachments(org.alfresco.repo.webservice.types.Reference[], java.lang.String[], org.alfresco.repo.webservice.types.ContentFormat[])
     */
	public Content writeWithAttachment(final Reference node, final String property, final ContentFormat format) 
		throws RemoteException, ContentFault 
	{
		try
        {
            RetryingTransactionCallback<Content> callback = new RetryingTransactionCallback<Content>()
            {
                public Content execute() throws Throwable
                {
                	AttachmentPart[] attachments = getMessageAttachments();
            		if (attachments.length != 1)
            		{
            			throw new AlfrescoRuntimeException("Expecting only one attachment");
            		}
                	
                	// create a NodeRef from the parent reference
                    NodeRef nodeRef = Utils.convertToNodeRef(node, nodeService, searchService, namespaceService);

                    // Get the content writer
                    ContentWriter writer = contentService.getWriter(nodeRef, QName.createQName(property), true);
                    
                    // Set the content format details (if they have been specified)
                    if (format != null)
                    {
                        writer.setEncoding(format.getEncoding());
                        writer.setMimetype(format.getMimetype());
                    }
                    
                    // Write the content (just need to get the first in the array since we are only expecting one attachment)
                    DataHandler dh = attachments[0].getDataHandler();
                    InputStream is = dh.getInputStream();
                    writer.putContent(is);

                    // Debug
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Updated content for node with id: " + nodeRef.getId());
                    }

                    // Return the content object
                    return createContent(nodeRef, property);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new ContentFault(0, e.getMessage());
        }
		
		
	}
	
	@SuppressWarnings("unchecked")
	private AttachmentPart[] getMessageAttachments() 
		throws AxisFault
	{
		MessageContext msgContext = MessageContext.getCurrentContext();
		Message reqMsg = msgContext.getRequestMessage();
		Attachments messageAttachments = reqMsg.getAttachmentsImpl();
		if (null == messageAttachments)
		{
			return new AttachmentPart[0];
		}
		int attachmentCount = messageAttachments.getAttachmentCount();
		AttachmentPart attachments[] = new AttachmentPart[attachmentCount];
		Iterator it = messageAttachments.getAttachments().iterator();
		int count = 0;
		while (it.hasNext())
		{
			AttachmentPart part = (AttachmentPart)it.next();
			attachments[count++] = part;			
		}

		return attachments;
	}

    /**
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#clear(org.alfresco.repo.webservice.types.Predicate,
     *      java.lang.String)
     */
    public Content[] clear(final Predicate items, final String property) throws RemoteException, ContentFault
    {
        try
        {
            RetryingTransactionCallback<Content[]> callback = new RetryingTransactionCallback<Content[]>()
            {
                public Content[] execute() throws Throwable
                {
                    List<NodeRef> nodes = Utils.resolvePredicate(items, nodeService, searchService, namespaceService);
                    Content[] contents = new Content[nodes.size()];

                    // delete each node in the predicate
                    for (int x = 0; x < nodes.size(); x++)
                    {
                        NodeRef nodeRef = nodes.get(x);

                        // Clear the content
                        nodeService.setProperty(nodeRef, QName.createQName(property), null);

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Cleared content node with id: " + nodeRef.getId());
                        }

                        contents[x] = createContent(nodeRef, property);
                    }
                    return contents;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new ContentFault(0, e.getMessage());
        }
    }

    /**
     * Transforms content from one node and mimetype to another node and mimetype
     * 
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#transform(org.alfresco.repo.webservice.types.Reference, java.lang.String, org.alfresco.repo.webservice.types.Reference, java.lang.String, org.alfresco.repo.webservice.types.ContentFormat)
     */
	public Content transform(
						final Reference source, 
						final String property,
						final Reference destinationReference, 
						final String destinationProperty,
						final ContentFormat destinationFormat) 
		throws RemoteException, ContentFault 
	{
		try
        {
            RetryingTransactionCallback<Content> callback = new RetryingTransactionCallback<Content>()
            {
                public Content execute() throws Throwable
                {
					// Get the nodes and property qname's
					NodeRef sourceNodeRef = Utils.convertToNodeRef(source, ContentWebService.this.nodeService, ContentWebService.this.searchService, ContentWebService.this.namespaceService);
					NodeRef destinationNodeRef = Utils.convertToNodeRef(destinationReference, ContentWebService.this.nodeService, ContentWebService.this.searchService, ContentWebService.this.namespaceService);
					QName sourceQName = QName.createQName(property);
					QName destinationQName = QName.createQName(destinationProperty);
					
					// Get the content reader
					ContentReader contentReader = ContentWebService.this.contentService.getReader(sourceNodeRef, sourceQName);
					if (contentReader == null)
					{
						throw new AlfrescoRuntimeException("Source content does not exist.  Transform could not take place.");
					}
					
					// Get the content writer
					ContentWriter contentWriter = ContentWebService.this.contentService.getWriter(destinationNodeRef, destinationQName, true);
					contentWriter.setEncoding(destinationFormat.getEncoding());
					contentWriter.setMimetype(destinationFormat.getMimetype());
					
				        TransformationOptions options = new TransformationOptions();
				        options.setSourceNodeRef(sourceNodeRef);

					// Transform the content
					ContentWebService.this.contentService.transform(contentReader, contentWriter, options);
					
					// Return the content object to the user
					return createContent(destinationNodeRef, destinationProperty);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new ContentFault(0, e.getMessage());
        }
		
	}
}
