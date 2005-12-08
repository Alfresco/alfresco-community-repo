/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.webservice.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.ContentFormat;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.axis.MessageContext;
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
    public Content[] read(Predicate items, String property)
            throws RemoteException, ContentFault
    {
        UserTransaction tx = null;

        try
        {
            tx = Utils.getUserTransaction(MessageContext.getCurrentContext());
            tx.begin();

            // resolve the predicates
            List<NodeRef> nodes = Utils.resolvePredicate(items, this.nodeService, this.searchService, this.namespaceService);
            List<Content> results = new ArrayList<Content>(nodes.size());
            for (NodeRef nodeRef : nodes)
            {   
                // Add content to the result
                results.add(createContent(nodeRef, property));
            }

            // commit the transaction
            tx.commit();

            return results.toArray(new Content[results.size()]);
        } 
        catch (Throwable e)
        {
            // rollback the transaction
            try
            {
                if (tx != null)
                {
                    tx.rollback();
                }
            } 
            catch (Exception ex)
            {
            }

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
    
            String address = req.getLocalName();
            if (req.getLocalPort() != 80)
            {
                address = address + ":" + req.getLocalPort();
            }
    
            // Get the file name
            String filename = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
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
            content = new Content(Utils.convertToReference(nodeRef), property, contentReader.getSize(), format, downloadUrl);
            
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
            content = new Content(Utils.convertToReference(nodeRef), property, 0, null, null);
            
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
    public Content write(Reference node, String property, byte[] content, ContentFormat format) 
        throws RemoteException, ContentFault
    {
        UserTransaction tx = null;

        try
        {
            tx = Utils.getUserTransaction(MessageContext.getCurrentContext());
            tx.begin();

            // create a NodeRef from the parent reference
            NodeRef nodeRef = Utils.convertToNodeRef(node, this.nodeService,
                    this.searchService, this.namespaceService);

            // Get the content writer
            ContentWriter writer = this.contentService.getWriter(nodeRef, QName.createQName(property), true);
            
            // Set the content format details (if they have been specified)
            if (format != null)
            {
                writer.setEncoding(format.getEncoding());
                writer.setMimetype(format.getMimetype());
            }
            
            // Write the content 
            writer.putContent(new String(content));

            // Debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Updated content for node with id: " + nodeRef.getId());
            }

            // Commit the transaction
            tx.commit();
            
            // Return the content object
            return createContent(nodeRef, property);
        } 
        catch (Throwable e)
        {
            // Rollback the transaction
            try
            {
                if (tx != null)
                {
                    tx.rollback();
                }
            } catch (Exception ex)
            {
            }

            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }

            throw new ContentFault(0, e.getMessage());
        }
    }

    /**
     * @see org.alfresco.repo.webservice.content.ContentServiceSoapPort#clear(org.alfresco.repo.webservice.types.Predicate,
     *      java.lang.String)
     */
    public Content[] clear(Predicate items, String property)
            throws RemoteException, ContentFault
    {
        UserTransaction tx = null;

        try
        {
            tx = Utils.getUserTransaction(MessageContext.getCurrentContext());
            tx.begin();

            List<NodeRef> nodes = Utils.resolvePredicate(items, this.nodeService,this.searchService, this.namespaceService);
            Content[] contents = new Content[nodes.size()];

            // delete each node in the predicate
            for (int x = 0; x < nodes.size(); x++)
            {
                NodeRef nodeRef = nodes.get(x);

                // Clear the content
                this.nodeService.setProperty(nodeRef, QName.createQName(property), null);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Cleared content node with id: " + nodeRef.getId());
                }

                contents[x] = createContent(nodeRef, property);
            }

            // commit the transaction
            tx.commit();

            return contents;
        } 
        catch (Throwable e)
        {
            // rollback the transaction
            try
            {
                if (tx != null)
                {
                    tx.rollback();
                }
            } catch (Exception ex)
            {
            }

            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }

            throw new ContentFault(0, e.getMessage());
        }
    }
}
