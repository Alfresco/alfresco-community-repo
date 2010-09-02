/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.transfer;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.requisite.TransferRequsiteWriter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXParseException;

/**
 * @author mrogers
 * 
 * The requsite manifest processor performs a parse of the manifest file to determine which 
 * resources are required.
 * 
 */
public class RepoRequsiteManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private CorrespondingNodeResolver nodeResolver;
    private TransferRequsiteWriter out;
    
    
    private static final Log log = LogFactory.getLog(RepoRequsiteManifestProcessorImpl.class);

    /**
     * @param receiver 
     * @param transferId
     */
    public RepoRequsiteManifestProcessorImpl(TransferReceiver receiver, String transferId, TransferRequsiteWriter out)
    {
        super(receiver, transferId);
        this.out = out;
    }

    protected void endManifest() 
    {   
        log.debug("End Requsite");
        out.endTransferRequsite();
    }
    
    protected void processNode(TransferManifestDeletedNode node)
    {
        //NOOP
    }

    protected void processNode(TransferManifestNormalNode node)
    {

        if (log.isDebugEnabled())
        {
            log.debug("Processing node with incoming noderef of " + node.getNodeRef());
        }
        logComment("Primary Processing incoming node: " + node.getNodeRef() + " --  Source path = " + node.getParentPath() + "/" + node.getPrimaryParentAssoc().getQName());

        ChildAssociationRef primaryParentAssoc = node.getPrimaryParentAssoc();

        CorrespondingNodeResolver.ResolvedParentChildPair resolvedNodes = nodeResolver.resolveCorrespondingNode(node
                .getNodeRef(), primaryParentAssoc, node.getParentPath());

        // Does a corresponding node exist in this repo?
        if (resolvedNodes.resolvedChild != null)
        {
            /**
             * there is a corresponding node so we need to check whether we already 
             * have the content item
             */
            NodeRef destinationNode = resolvedNodes.resolvedChild;
            
            Map<QName, Serializable> destProps = nodeService.getProperties(destinationNode);
            

            
            for (Map.Entry<QName, Serializable> propEntry : node.getProperties().entrySet())
            {
                Serializable value = propEntry.getValue();
                if (log.isDebugEnabled())
                {
                    if (value == null)
                    {
                        log.debug("Received a null value for property " + propEntry.getKey());
                    }
                }
                if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
                {
                    ContentData srcContent = (ContentData)value;
                    Serializable destSer = destProps.get(propEntry.getKey());
                    if(destSer != null && ContentData.class.isAssignableFrom(destSer.getClass()))
                    {
                        ContentData destContent = (ContentData)destProps.get(propEntry.getKey());
                        
                        /**
                         * If the modification dates for the node are different
                         */
                        Serializable srcModified = node.getProperties().get(ContentModel.PROP_MODIFIED);
                        Serializable destModified = destProps.get(ContentModel.PROP_MODIFIED);
                        
                        log.debug ("srcModified :" + srcModified + "destModified :" + destModified);
                        
                        if(srcModified != null && 
                           destModified != null &&
                           srcModified instanceof Date && 
                           destModified instanceof Date &&
                           ((Date)srcModified).getTime() >= ((Date)destModified).getTime())
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("the modified date is the same - no need to send it:" + destContent.getContentUrl());
                            }
                        }
                        else
                        {
                            out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                        }
                    }
                    else
                    {
                        //  We don't have the property on the destination node 
                        out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                    }
                }
            }
        }
        else
        {
            /**
             * there is no corresponding node so all content properties are "missing."
             */
            for (Map.Entry<QName, Serializable> propEntry : node.getProperties().entrySet())
            {
                Serializable value = propEntry.getValue();
                if (log.isDebugEnabled())
                {
                    if (value == null)
                    {
                        log.debug("Received a null value for property " + propEntry.getKey());
                    }
                }
                if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
                {
                    ContentData content = (ContentData)value;
                    // 
                    out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(content.getContentUrl()));
                }
            }
        }        
    }
    
    protected void processHeader(TransferManifestHeader header)
    {
        // T.B.D
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#startTransferManifest()
     */
    protected void startManifest()
    {
        log.debug("Start Requsite");
        out.startTransferRequsite();
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeResolver
     *            the nodeResolver to set
     */
    public void setNodeResolver(CorrespondingNodeResolver nodeResolver)
    {
        this.nodeResolver = nodeResolver;
    }
}
