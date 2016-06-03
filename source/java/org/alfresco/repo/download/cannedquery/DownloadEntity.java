package org.alfresco.repo.download.cannedquery;

import org.alfresco.repo.query.NodeBackedEntity;

/**
 * Download Entity - used by GetDownloads CQ
 *
 * @author Alex Miller
 */
public class DownloadEntity extends NodeBackedEntity
{
    /**
     * Default constructor
     */
    public DownloadEntity()
    {
        super();
    }
    
    public DownloadEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
    }
}