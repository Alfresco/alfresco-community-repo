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
package org.alfresco.repo.publishing.youtube;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.publishing.PublishingModel;
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

import com.google.gdata.client.media.ResumableGDataFileUploader;
import com.google.gdata.client.uploader.ProgressListener;
import com.google.gdata.client.uploader.ResumableHttpFileUploader;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;

public class YouTubePublishAction extends ActionExecuterAbstractBase
{
    private final static Log log = LogFactory.getLog(YouTubePublishAction.class);

    public static final String NAME = "publish_youtube";

    public static final String RESUMABLE_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";

    /** Time interval at which upload task will notify about the progress */
    private static final int PROGRESS_UPDATE_INTERVAL = 1000;

    /** Max size for each upload chunk */
    private static final int DEFAULT_CHUNK_SIZE = 10000000;

    private NodeService nodeService;
    private ContentService contentService;
    private TaggingService taggingService;
    private YouTubePublishingHelper youTubeHelper;

    public void setYouTubeHelper(YouTubePublishingHelper youTubeHelper)
    {
        this.youTubeHelper = youTubeHelper;
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
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        YouTubeService service = youTubeHelper.getYouTubeServiceForNode(actionedUponNodeRef);
        if (service != null)
        {
            try
            {
                uploadVideo(service, actionedUponNodeRef);
            }
            catch(Exception ex)
            {
                log.error("Failed to send asset to YouTube", ex);
                throw new AlfrescoRuntimeException("exception.publishing.youtube.publishFailed", ex);
            }
        }
    }

    private void uploadVideo(YouTubeService service, NodeRef nodeRef) throws IOException, ServiceException,
            InterruptedException
    {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader.exists())
        {
            File contentFile;
            boolean deleteContentFileOnCompletion = false;
            if (FileContentReader.class.isAssignableFrom(reader.getClass()))
            {
                //Grab the content straight from the content store if we can...
                contentFile = ((FileContentReader)reader).getFile();
            }
            else
            {
                //...otherwise copy it to a temp file and use the copy... 
                File tempDir = TempFileProvider.getLongLifeTempDir("youtube");
                contentFile = TempFileProvider.createTempFile("youtube", "", tempDir);
                reader.getContent(contentFile);
                deleteContentFileOnCompletion = true;
            }
            MediaFileSource ms = new MediaFileSource(contentFile, reader.getMimetype());

            String videoName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String videoTitle = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
            if (videoTitle == null || videoTitle.length() == 0)
            {
                videoTitle = videoName;
            }
            String videoDescription = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
            if (videoDescription == null || videoDescription.length() == 0)
            {
                videoDescription = videoTitle;
            }

            VideoEntry newEntry = new VideoEntry();
            YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
            mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, "Tech"));
            mg.setTitle(new MediaTitle());
            mg.getTitle().setPlainTextContent(videoTitle);
            mg.setKeywords(new MediaKeywords());
            List<String> tags = taggingService.getTags(nodeRef);
            for (String tag : tags)
            {
                mg.getKeywords().addKeyword(tag);
            }
            mg.setDescription(new MediaDescription());
            mg.getDescription().setPlainTextContent(videoDescription);

            FileUploadProgressListener listener = new FileUploadProgressListener(videoName);
            ResumableGDataFileUploader uploader = new ResumableGDataFileUploader.Builder(service, new URL(
                    RESUMABLE_UPLOAD_URL), ms, newEntry).title(videoTitle).trackProgress(listener,
                    PROGRESS_UPDATE_INTERVAL).chunkSize(DEFAULT_CHUNK_SIZE).build();

            uploader.start();
            while (!uploader.isDone())
            {
                Thread.sleep(PROGRESS_UPDATE_INTERVAL);
            }

            switch (uploader.getUploadState())
            {
            case COMPLETE:
                VideoEntry entry = uploader.getResponse(VideoEntry.class);
                String videoId = entry.getMediaGroup().getVideoId();
                String contentUrl = entry.getMediaGroup().getContents().get(0).getUrl();
                String playerUrl = entry.getMediaGroup().getPlayer().getUrl();
                if (log.isDebugEnabled())
                {
                    log.debug("Video content uploaded successfully: " + videoName);
                    log.debug("YouTube video id is " + videoId);
                    log.debug("YouTube content URL is " + contentUrl);
                    log.debug("YouTube video player URL is " + playerUrl);
                }
                nodeService.addAspect(nodeRef, YouTubePublishingModel.ASPECT_ASSET, null);
                nodeService.setProperty(nodeRef, PublishingModel.PROP_ASSET_ID, videoId);
                nodeService.setProperty(nodeRef, PublishingModel.PROP_ASSET_URL, playerUrl);
                break;
            case CLIENT_ERROR:
                log.error("Video content failed to upload: " + videoName);
                break;
            default:
                log.warn("Unknown upload state. Video content may not have uploaded: " + videoName + "("
                        + uploader.getUploadState() + ") :" + nodeRef);
                break;
            }

            if (deleteContentFileOnCompletion)
            {
                contentFile.delete();
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }

    /**
     * A {@link ProgressListener} implementation to track upload progress. The
     * listener can track multiple uploads at the same time.
     */
    private class FileUploadProgressListener implements ProgressListener
    {
        String videoName;

        public FileUploadProgressListener(String videoName)
        {
            this.videoName = videoName;
        }

        public synchronized void progressChanged(ResumableHttpFileUploader uploader)
        {
            switch (uploader.getUploadState())
            {
            case COMPLETE:
                log.info("Upload Completed: " + videoName);
                break;
            case CLIENT_ERROR:
                log.error("Upload Failed: " + videoName);
                break;
            case IN_PROGRESS:
                log.info(videoName + String.format("  %3.0f", uploader.getProgress() * 100) + "%");
                break;
            case NOT_STARTED:
                log.info("Upload Not Started: " + videoName);
                break;
            }
        }
    }

}
