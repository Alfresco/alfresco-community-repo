/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.content;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.reference.ReferenceFactory;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Content Info Service Get info about content from the Repository.
 * 
 * @author alex.malinovsky
 */
public class ContentInfo extends StreamContent
{

    private ReferenceFactory referenceFactory;

    public void setReferenceFactory(ReferenceFactory referenceFactory)
    {
        this.referenceFactory = referenceFactory;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // create empty map of args
        Map<String, String> args = new HashMap<String, String>(0, 1.0f);
        // create map of template vars
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        // create object reference from url
        CMISObjectReference reference = referenceFactory.createObjectReferenceFromUrl(args, templateVars);
        NodeRef nodeRef = reference.getNodeRef();
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + reference.toString());
        }

        // render content
        QName propertyQName = ContentModel.PROP_CONTENT;

        // Stream the content
        streamContent(req, res, nodeRef, propertyQName, false, null, null);
    }

    protected void streamContentImpl(WebScriptRequest req, WebScriptResponse res, 
            ContentReader reader, NodeRef nodeRef, QName propertyQName, 
            boolean attach, Date modified, String eTag, String attachFileName)
            throws IOException
    {
        setAttachment(res, attach, attachFileName);

        // establish mimetype
        String mimetype = reader.getMimetype();
        String extensionPath = req.getExtensionPath();
        if (mimetype == null || mimetype.length() == 0)
        {
            mimetype = MimetypeMap.MIMETYPE_BINARY;
            int extIndex = extensionPath.lastIndexOf('.');
            if (extIndex != -1)
            {
                String ext = extensionPath.substring(extIndex + 1);
                mimetype = mimetypeService.getMimetype(ext);
            }
        }

        // set mimetype for the content and the character encoding + length for the stream
        res.setContentType(mimetype);
        res.setContentEncoding(reader.getEncoding());
        res.setHeader("Content-Length", Long.toString(reader.getSize()));

        // set caching
        Cache cache = new Cache();
        cache.setNeverCache(false);
        cache.setMustRevalidate(true);
        cache.setMaxAge(0L);
        cache.setLastModified(modified);
        cache.setETag(eTag);
        res.setCache(cache);
    }

}
