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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.youtube.YouTubeService;

public class YouTubePublishingHelper
{
    private static final Log log = LogFactory.getLog(YouTubePublishingHelper.class);
    private MetadataEncryptor encryptor;

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    public YouTubeService getYouTubeServiceFromChannelProperties(Map<QName, Serializable> channelProperties)
    {
        YouTubeService service = null;
        if (channelProperties != null)
        {
            String youtubeUsername = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_USERNAME, 
                    channelProperties.get(PublishingModel.PROP_CHANNEL_USERNAME));
            String youtubePassword = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_PASSWORD, 
                    channelProperties.get(PublishingModel.PROP_CHANNEL_PASSWORD));
            service = new YouTubeService("Alfresco",
                    "AI39si78RHlniONCtnu9o8eBfwZToBAp2ZbbURm5eoJjj4gZi0LcxjDqJTzD35oYokmtFXbCo5ojofbimGnMlRbmNrh7-M7ZCw");
            try
            {
                service.setUserCredentials(youtubeUsername, youtubePassword);
            }
            catch (Exception e)
            {
                service = null;
                log.error("Failed to connect to YouTube", e);
            }
        }
        return service;
    }

}
