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
package org.alfresco.repo.publishing.flickr;

import java.io.File;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoMetadata;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.social.connect.Connection;

public class FlickrPublishAction extends ActionExecuterAbstractBase
{
    private final static Log log = LogFactory.getLog(FlickrPublishAction.class);

    public static final String NAME = "publish_flickr";

    private NodeService nodeService;
    private ContentService contentService;
    private TaggingService taggingService;
    private FlickrPublishingHelper flickrHelper;

    public void setFlickrHelper(FlickrPublishingHelper helper)
    {
        this.flickrHelper = helper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef nodeToPublish)
    {
        ContentReader reader = contentService.getReader(nodeToPublish, ContentModel.PROP_CONTENT);
        if (reader.exists())
        {
            File contentFile;
            boolean deleteContentFileOnCompletion = false;
            if (FileContentReader.class.isAssignableFrom(reader.getClass()))
            {
                // Grab the content straight from the content store if we can...
                contentFile = ((FileContentReader) reader).getFile();
            }
            else
            {
                // ...otherwise copy it to a temp file and use the copy...
                File tempDir = TempFileProvider.getLongLifeTempDir("flickr");
                contentFile = TempFileProvider.createTempFile("flickr", "", tempDir);
                reader.getContent(contentFile);
                deleteContentFileOnCompletion = true;
            }
            try
            {
                Resource res = new FileSystemResource(contentFile);
                Connection<Flickr> connection = flickrHelper.getConnectionForPublishNode(nodeToPublish);

                String name = (String) nodeService.getProperty(nodeToPublish, ContentModel.PROP_NAME);
                String title = (String) nodeService.getProperty(nodeToPublish, ContentModel.PROP_TITLE);
                if (title == null || title.length() == 0)
                {
                    title = name;
                }
                String description = (String) nodeService.getProperty(nodeToPublish, ContentModel.PROP_DESCRIPTION);
                if (description == null || description.length() == 0)
                {
                    description = title;
                }

                MediaOperations mediaOps = connection.getApi().mediaOperations();
                PhotoMetadata metadata = mediaOps.createPhotoMetadata();
                metadata.setTitle(title);
                metadata.setDescription(description);
                String id = mediaOps.postPhoto(res, metadata);
                log.info("Posted image " + name + " to Flickr with id " + id);
                nodeService.setProperty(nodeToPublish, FlickrPublishingModel.PROP_ASSET_ID, id);
            }
            finally
            {
                if (deleteContentFileOnCompletion)
                {
                    contentFile.delete();
                }
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO Auto-generated method stub
        
    }
}
