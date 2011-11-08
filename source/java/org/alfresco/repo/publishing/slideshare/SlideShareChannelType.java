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
package org.alfresco.repo.publishing.slideshare;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.messages.SlideshowInfo;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class SlideShareChannelType extends AbstractChannelType
{
    public final static String ID = "slideshare";

    private final static Log log = LogFactory.getLog(SlideShareChannelType.class);

    private final static int STATUS_QUEUED = 0;
    // private final static int STATUS_CONVERTING = 1;
    private final static int STATUS_SUCCEEDED = 2;
    private final static int STATUS_FAILED = 3;
    private final static int STATUS_TIMED_OUT = 10;
    private static final String ERROR_SLIDESHARE_CONVERSION_FAILED = "publish.slideshare.conversionFailed";
    private static final String ERROR_SLIDESHARE_CONVERSION_TIMED_OUT = "publish.slideshare.conversionTimedOut";

    private SlideSharePublishingHelper publishingHelper;
    private ContentService contentService;
    private TaggingService taggingService;

    private long timeoutMilliseconds = 60L * 60L * 1000L; // 1 hour default

    public void setPublishingHelper(SlideSharePublishingHelper publishingHelper)
    {
        this.publishingHelper = publishingHelper;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    public void setTimeoutMilliseconds(long timeoutMilliseconds)
    {
        this.timeoutMilliseconds = timeoutMilliseconds;
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
        return SlideSharePublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return publishingHelper.getAllowedMimeTypes().keySet();
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        NodeService nodeService = getNodeService();
        Pair<String, String> usernamePassword = publishingHelper
                .getSlideShareCredentialsFromChannelProperties(properties);
        if (usernamePassword == null)
        {
            throw new AlfrescoRuntimeException("publish.failed.no_credentials_found");
        }
        SlideShareAPI api = publishingHelper
                .getSlideShareApi(usernamePassword.getFirst(), usernamePassword.getSecond());

        ContentReader reader = contentService.getReader(nodeToPublish, ContentModel.PROP_CONTENT);
        if (reader.exists())
        {
            File contentFile;
            String mime = reader.getMimetype();

            String extension = publishingHelper.getAllowedMimeTypes().get(mime);
            if (extension == null)
                extension = "";

            boolean deleteContentFileOnCompletion = false;

            // SlideShare seems to work entirely off file extension, so we
            // always copy onto the
            // file system and upload from there.
            File tempDir = TempFileProvider.getLongLifeTempDir("slideshare");
            contentFile = TempFileProvider.createTempFile("slideshare", extension, tempDir);
            reader.getContent(contentFile);
            deleteContentFileOnCompletion = true;

            try
            {

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

                List<String> tagList = taggingService.getTags(nodeToPublish);
                StringBuilder tags = new StringBuilder();
                for (String tag : tagList)
                {
                    tags.append(tag);
                    tags.append(' ');
                }

                String assetId = api.uploadSlideshow(usernamePassword.getFirst(), usernamePassword.getSecond(), title,
                        contentFile, description, tags.toString(), false, false, false, false, false);

                String url = null;
                int status = STATUS_QUEUED;
                boolean finished = false;
                long timeoutTime = System.currentTimeMillis() + timeoutMilliseconds;
                // Fetch the slideshow info every 30 seconds until we timeout
                while (!finished)
                {
                    SlideshowInfo slideInfo = api.getSlideshowInfo(assetId, "");
                    if (slideInfo != null)
                    {
                        if (url == null)
                        {
                            url = slideInfo.getUrl();
                            if (log.isInfoEnabled())
                            {
                                log.info("SlideShare has provided a URL for asset " + assetId + ": " + url);
                            }
                        }
                        status = slideInfo.getStatus();
                    }
                    finished = (status == STATUS_FAILED || status == STATUS_SUCCEEDED);

                    if (!finished)
                    {
                        if (System.currentTimeMillis() < timeoutTime)
                        {
                            try
                            {
                                Thread.sleep(30000L);
                            }
                            catch (InterruptedException e)
                            {
                            }
                        }
                        else
                        {
                            status = STATUS_TIMED_OUT;
                            finished = true;
                        }
                    }
                }
                if (status == STATUS_SUCCEEDED)
                {
                    if (log.isInfoEnabled())
                    {
                        log.info("File " + name + " has been published to SlideShare with id " + assetId + " at URL "
                                + url);
                    }
                    nodeService.addAspect(nodeToPublish, SlideSharePublishingModel.ASPECT_ASSET, null);
                    nodeService.setProperty(nodeToPublish, PublishingModel.PROP_ASSET_ID, assetId);
                    nodeService.setProperty(nodeToPublish, PublishingModel.PROP_ASSET_URL, url);
                }
                else
                {
                    throw new AlfrescoRuntimeException(status == STATUS_FAILED ? ERROR_SLIDESHARE_CONVERSION_FAILED
                            : ERROR_SLIDESHARE_CONVERSION_TIMED_OUT);
                }
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
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        NodeService nodeService = getNodeService();
        if (nodeService.hasAspect(nodeToUnpublish, SlideSharePublishingModel.ASPECT_ASSET))
        {
            String assetId = (String) nodeService.getProperty(nodeToUnpublish, PublishingModel.PROP_ASSET_ID);
            if (assetId != null)
            {
                Pair<String, String> usernamePassword = publishingHelper
                        .getSlideShareCredentialsFromChannelProperties(properties);
                if (usernamePassword == null)
                {
                    throw new AlfrescoRuntimeException("publish.failed.no_credentials_found");
                }
                SlideShareApi api = publishingHelper.getSlideShareApi(usernamePassword.getFirst(), usernamePassword
                        .getSecond());

                api.deleteSlideshow(usernamePassword.getFirst(), usernamePassword.getSecond(), assetId);
                nodeService.removeAspect(nodeToUnpublish, SlideSharePublishingModel.ASPECT_ASSET);
                nodeService.removeAspect(nodeToUnpublish, PublishingModel.ASPECT_ASSET);
            }
        }
    }
}
