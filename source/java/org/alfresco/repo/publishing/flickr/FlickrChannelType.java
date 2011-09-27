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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Brian
 * @since 4.0
 */
public class FlickrChannelType extends AbstractOAuth1ChannelType<Flickr>
{
    public final static String ID = "flickr";
    private final static Set<String> DEFAULT_SUPPORTED_MIME_TYPES = CollectionUtils.unmodifiableSet(
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_JPEG,
            MimetypeMap.MIMETYPE_IMAGE_PNG);
    private static Log log = LogFactory.getLog(FlickrChannelType.class);
    
    private ContentService contentService;
    private TaggingService taggingService;
    private FlickrPublishingHelper flickrHelper;
    private Set<String> supportedMimeTypes = DEFAULT_SUPPORTED_MIME_TYPES;
    
    public void setSupportedMimeTypes(Set<String> mimeTypes)
    {
        supportedMimeTypes = Collections.unmodifiableSet(new TreeSet<String>(mimeTypes));
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    public void setFlickrHelper(FlickrPublishingHelper flickrHelper)
    {
        this.flickrHelper = flickrHelper;
    }

    @Override
    public boolean canPublish()
    {
        return true;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    @Override
    public boolean canUnpublish()
    {
        return true;
    }

    @Override
    public QName getChannelNodeType()
    {
        return FlickrPublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return supportedMimeTypes;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> channelProperties)
    {
        NodeService nodeService = getNodeService();
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
                Connection<Flickr> connection = flickrHelper.getConnectionFromChannelProps(channelProperties);

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
                List<String> tags = taggingService.getTags(nodeToPublish);
                String[] tagArray = tags.toArray(new String[tags.size()]);

                MediaOperations mediaOps = connection.getApi().mediaOperations();
                String id = mediaOps.postPhoto(res, title, description, tagArray);
                
                //Store info onto the published node...
                nodeService.addAspect(nodeToPublish, FlickrPublishingModel.ASPECT_ASSET, null);
                log.info("Posted image " + name + " to Flickr with id " + id);
                nodeService.setProperty(nodeToPublish, PublishingModel.PROP_ASSET_ID, id);

                PhotoInfo photoInfo = mediaOps.getPhoto(id);
                String url = photoInfo.getPrimaryUrl();
                log.info("Photo url = " + url);
                nodeService.setProperty(nodeToPublish, PublishingModel.PROP_ASSET_URL, url);
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
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> channelProperties)
    {
        NodeService nodeService = getNodeService();
        if (nodeService.hasAspect(nodeToUnpublish, FlickrPublishingModel.ASPECT_ASSET))
        {
            String assetId = (String) nodeService.getProperty(nodeToUnpublish, PublishingModel.PROP_ASSET_ID);
            if (assetId != null)
            {
                Connection<Flickr> connection = flickrHelper.getConnectionFromChannelProps(channelProperties);
                MediaOperations mediaOps = connection.getApi().mediaOperations();
                mediaOps.deletePhoto(assetId);
                nodeService.removeAspect(nodeToUnpublish, FlickrPublishingModel.ASPECT_ASSET);
                nodeService.removeAspect(nodeToUnpublish, PublishingModel.ASPECT_ASSET);
            }
        }
    }

    @Override
    protected OAuth1Parameters getOAuth1Parameters(String callbackUrl)
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("perms", "delete");
        return new OAuth1Parameters(callbackUrl, params);
    }

}
