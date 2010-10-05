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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.requisite.TransferRequsiteWriter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mrogers
 * 
 * The requisite manifest processor performs a parse of the manifest file to determine which 
 * resources are required.    In particular it returns a list of nodes which require content to be transferred.
 * 
 */
public class RepoRequisiteManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private NodeService nodeService;
    private CorrespondingNodeResolver nodeResolver;
    private TransferRequsiteWriter out;
    
    
    private static final Log log = LogFactory.getLog(RepoRequisiteManifestProcessorImpl.class);

    /**
     * @param receiver 
     * @param transferId
     */
    public RepoRequisiteManifestProcessorImpl(TransferReceiver receiver, String transferId, TransferRequsiteWriter out)
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
             * have the part for each content item
             */
            NodeRef destinationNode = resolvedNodes.resolvedChild;
            
            Map<QName, Serializable> destinationProps = nodeService.getProperties(destinationNode);            
            /**
             * For each property on the source node
             */
            for (Map.Entry<QName, Serializable> propEntry : node.getProperties().entrySet())
            {
                Serializable value = propEntry.getValue();
                QName propName = propEntry.getKey();
                
                if (log.isDebugEnabled())
                {
                    if (value == null)
                    {
                        log.debug("Received a null value for property " + propName);
                    }
                }
                if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
                {
                    /**
                     * Got a content property from source node.
                     */
                    ContentData srcContent = (ContentData)value;
                    
                    if(srcContent.getContentUrl() != null && !srcContent.getContentUrl().isEmpty() )
                    {
                        /**
                         * Source Content is not empty
                         */
                        String partName = TransferCommons.URLToPartName(srcContent.getContentUrl());
                    
                        Serializable destSer = destinationProps.get(propName);
                        if(destSer != null && ContentData.class.isAssignableFrom(destSer.getClass()))                  
                        {
                            /**
                             * Content property not empty and content property already exists on destination
                             */
                            ContentData destContent = (ContentData)destSer;
                            
                            Serializable destFromContents = destinationProps.get(TransferModel.PROP_FROM_CONTENT);
                            
                            if(destFromContents != null && Collection.class.isAssignableFrom(destFromContents.getClass()))
                            {
                                Collection<String> contents = (Collection<String>)destFromContents;
                                /**
                                 * Content property not empty and content property already exists on destination
                                 */
                                if(contents.contains(partName))
                                {
                                    if(log.isDebugEnabled())
                                    {
                                        log.debug("part already transferred, no need to send it again, partName:" + partName + ", nodeRef:" + node.getNodeRef());
                                    }   
                                }
                                else
                                {
                                    if(log.isDebugEnabled())
                                    {
                                        log.debug("part name not transferred, requesting new content item partName:" + partName + ", nodeRef:" + node.getNodeRef());
                                    }
                                    out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                                }
                            }
                            else
                            {
                                // dest from contents is null
                                if(log.isDebugEnabled())
                                {
                                    log.debug("from contents is null, requesting new content item partName:" + partName + ", nodeRef:" + node.getNodeRef());
                                }
                                out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                            }
                        }
                        else
                        {
                            /**
                             * Content property not empty and does not exist on destination
                             */
                            if(log.isDebugEnabled())
                            {
                                log.debug("no content on destination, all content is required" + propEntry.getKey() + srcContent.getContentUrl());
                            }
                            //  We don't have the property on the destination node 
                            out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                        }
                    }
                } // src content url not null
            } // value is content data
        }
        else
        {
            log.debug("Node does not exist on destination nodeRef:" + node.getNodeRef());
    
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
                    ContentData srcContent = (ContentData)value;
                    if(srcContent.getContentUrl() != null && !srcContent.getContentUrl().isEmpty())
                    {
                        if(log.isDebugEnabled())
                        {
                            log.debug("no node on destination, content is required" + propEntry.getKey() + srcContent.getContentUrl());
                        }
                        out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                    }
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
