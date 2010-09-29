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
             * have the content item
             */
            NodeRef destinationNode = resolvedNodes.resolvedChild;
            
            Map<QName, Serializable> destinationProps = nodeService.getProperties(destinationNode);
            
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

                    if(srcContent.getContentUrl() != null && !srcContent.getContentUrl().isEmpty() )
                    {
                        Serializable destSer = destinationProps.get(propEntry.getKey());
                        if(destSer != null && ContentData.class.isAssignableFrom(destSer.getClass()))
                        {
                            ContentData destContent = (ContentData)destinationProps.get(propEntry.getKey());
                            
                            /**
                             * If the modification dates for the node are different
                             */
                            Serializable srcModified = node.getProperties().get(ContentModel.PROP_MODIFIED);
                            Serializable destModified = destinationProps.get(ContentModel.PROP_MODIFIED);

                            if(log.isDebugEnabled())
                            {
                                SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");  
                                
                                log.debug ("srcModified :" + srcModified + "destModified :" + destModified);
                                
                                if(srcModified instanceof Date)
                                {
                                    log.debug("srcModified: "  + SDF.format(srcModified));
                                }
                                
                                if(destModified instanceof Date)
                                {
                                    log.debug("destModified: " + SDF.format(destModified));
                                }  
                            }
                            
                            if(srcModified != null && 
                                    destModified != null &&
                                    srcModified instanceof Date && 
                                    destModified instanceof Date &&
                                    ((Date)srcModified).getTime() <= ((Date)destModified).getTime())
                            {
                                if(log.isDebugEnabled())
                                {
                                    log.debug("the modified date is the same or before - no need send content:" + node.getNodeRef());
                                }
                            }
                            else
                            {
                                if(log.isDebugEnabled())
                                {
                                    log.debug("time different, require content for node : " + node.getNodeRef());
                                }
                                out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                            }
                        }
                        else
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("no content on destination, content is required" + propEntry.getKey() + srcContent.getContentUrl());
                            }
                            //  We don't have the property on the destination node 
                            out.missingContent(node.getNodeRef(), propEntry.getKey(), TransferCommons.URLToPartName(srcContent.getContentUrl()));
                        }
                    } // src content url not null
                } // value is content data
            }
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
