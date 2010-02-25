/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferReceiver;

/**
 * @author brian
 * 
 */
public class DefaultManifestProcessorFactoryImpl implements ManifestProcessorFactory
{
    private NodeService nodeService;
    private ContentService contentService;
    private CorrespondingNodeResolverFactory nodeResolverFactory;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.ManifestProcessorFactory#getPrimaryCommitProcessor()
     */
    public List<TransferManifestProcessor> getCommitProcessors(TransferReceiver receiver, String transferId)
    {
        List<TransferManifestProcessor> processors = new ArrayList<TransferManifestProcessor>();
        CorrespondingNodeResolver nodeResolver = nodeResolverFactory.getResolver();
        
        RepoPrimaryManifestProcessorImpl primaryProcessor = new RepoPrimaryManifestProcessorImpl(receiver, transferId);
        primaryProcessor.setContentService(contentService);
        primaryProcessor.setNodeResolver(nodeResolver);
        primaryProcessor.setNodeService(nodeService);
        processors.add(primaryProcessor);
        
        RepoSecondaryManifestProcessorImpl secondaryProcessor = new RepoSecondaryManifestProcessorImpl(receiver, transferId);
        secondaryProcessor.setNodeResolver(nodeResolver);
        secondaryProcessor.setNodeService(nodeService);
        processors.add(secondaryProcessor);
        
        return processors;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param nodeResolverFactory the nodeResolverFactory to set
     */
    public void setNodeResolverFactory(CorrespondingNodeResolverFactory nodeResolverFactory)
    {
        this.nodeResolverFactory = nodeResolverFactory;
    }

}
