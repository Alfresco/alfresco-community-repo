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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareConnector;

public class SlideSharePublishingHelper
{
    private final static Map<String,String> DEFAULT_MIME_TYPES = new TreeMap<String,String>(); 
    static
    {
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_PPT, ".ppt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_PDF, ".pdf");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION, ".odp");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, ".pptx");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, "");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_IWORK_PAGES, "");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_TEXT_PLAIN, ".txt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, ".odt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_TEXT_CSV, ".csv");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_EXCEL, ".xls");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, ".docx");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET, ".ods");
    }
    
    private Map<String, String> allowedMimeTypes = Collections.unmodifiableMap(DEFAULT_MIME_TYPES);
    private NodeService nodeService;
    private SlideShareConnector slideshareConnector;
    private MetadataEncryptor encryptor;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSlideshareConnector(SlideShareConnector slideshareConnector)
    {
        this.slideshareConnector = slideshareConnector;
    }

    public Map<String, String> getAllowedMimeTypes()
    {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(Map<String, String> allowedMimeTypes)
    {
        this.allowedMimeTypes = Collections.unmodifiableMap(allowedMimeTypes);
    }

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    public SlideShareAPI getSlideShareApi()
    {
        return createApiObject();
    }
    
    private SlideShareApiImpl createApiObject()
    {
        return new SlideShareApiImpl(slideshareConnector);
    }
    
    public Pair<String, String> getSlideShareCredentialsForNode(NodeRef publishNode)
    {
        Pair<String, String> result = null;
        if (nodeService.exists(publishNode))
        {
            NodeRef parent = nodeService.getPrimaryParent(publishNode).getParentRef();
            if (nodeService.hasAspect(parent, SlideSharePublishingModel.ASPECT_DELIVERY_CHANNEL))
            {
                String username = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_USERNAME, nodeService
                        .getProperty(parent, PublishingModel.PROP_CHANNEL_USERNAME));
                String password = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_PASSWORD, nodeService
                        .getProperty(parent, PublishingModel.PROP_CHANNEL_PASSWORD));
                if (username != null && password != null)
                {
                    result = new Pair<String, String>(username, password);
                }
            }
        }
        return result;
    }

    public SlideShareApi getSlideShareApi(String username, String password)
    {
        SlideShareApiImpl api = createApiObject();
        api.setUsername(username);
        api.setPassword(password);
        return api;
    }

}
