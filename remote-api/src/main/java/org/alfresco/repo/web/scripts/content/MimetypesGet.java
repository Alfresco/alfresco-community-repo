/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.transform.registry.TransformServiceRegistry;

/**
 * Lists mimetypes.
 * 
 * @author Nick Burch
 * @since 3.4.b
 */
public class MimetypesGet extends DeclarativeWebScript
{
    public static final String MODEL_MIMETYPES = "mimetypes";
    public static final String MODEL_EXTENSIONS = "extensions";
    public static final String MODEL_MIMETYPE_DETAILS = "details";

    private MimetypeService mimetypeService;
    private TransformServiceRegistry localTransformServiceRegistry;
    private MetadataExtracterRegistry metadataExtracterRegistry;

    /**
     * Sets the Mimetype Service to be used to get the list of mime types
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setLocalTransformServiceRegistry(TransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

    /**
     * Sets the Metadata Extractor Registry to be used to decide what extractors exist
     */
    public void setMetadataExtracterRegistry(
            MetadataExtracterRegistry metadataExtracterRegistry)
    {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // First up, get the list of mimetypes
        // We want it to be sorted
        String[] mimetypesA = mimetypeService.getMimetypes().toArray(new String[0]);
        Arrays.sort(mimetypesA);
        List<String> mimetypes = new ArrayList<String>(
                Arrays.asList(mimetypesA));

        // Now their extensions
        Map<String, String> extensions = new HashMap<String, String>();
        for (String mimetype : mimetypes)
        {
            String ext = mimetypeService.getExtension(mimetype);
            extensions.put(mimetype, ext);
        }

        // Now details on those it was requested for
        Map<String, Map<String, List<String>>> details = new HashMap<String, Map<String, List<String>>>();
        String reqMimetype = req.getParameter("mimetype");
        for (String mimetype : mimetypes)
        {
            if (mimetype.equals(reqMimetype) || "*".equals(reqMimetype))
            {
                Map<String, List<String>> mtd = new HashMap<String, List<String>>();
                mtd.put("extractors", getExtractors(mimetype));
                mtd.put("transformFrom", getTransformersFrom(mimetype, -1, mimetypes));
                mtd.put("transformTo", getTransformersTo(mimetype, -1, mimetypes));
                details.put(mimetype, mtd);
            }
        }

        // Return the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_MIMETYPES, mimetypes);
        model.put(MODEL_EXTENSIONS, extensions);
        model.put(MODEL_MIMETYPE_DETAILS, details);
        return model;
    }

    protected List<String> getExtractors(String mimetype)
    {
        List<String> exts = new ArrayList<String>();
        MetadataExtracter extractor = metadataExtracterRegistry.getExtracter(mimetype);
        if (extractor != null)
        {
            exts.add(extractor.getClass().getName());
        }
        return exts;
    }

    protected List<String> getTransformersFrom(String mimetype, long sourceSize, List<String> allMimetypes)
    {
        List<String> transforms = new ArrayList<String>();
        for (String toMT : allMimetypes)
        {
            if (toMT.equals(mimetype))
                continue;

            String details = getTransformer(mimetype, sourceSize, toMT);
            if (details != null)
                transforms.add(toMT + " = " + details);
        }
        return transforms;
    }

    protected List<String> getTransformersTo(String mimetype, long sourceSize, List<String> allMimetypes)
    {
        List<String> transforms = new ArrayList<String>();
        for (String fromMT : allMimetypes)
        {
            if (fromMT.equals(mimetype))
                continue;

            String details = getTransformer(fromMT, sourceSize, mimetype);
            if (details != null)
                transforms.add(fromMT + " = " + details);
        }
        return transforms;
    }

    /** Note - for now, only does the best one, not all */
    protected String getTransformer(String from, long sourceSize, String to)
    {
        return localTransformServiceRegistry.findTransformerName(from, sourceSize, to, Collections.emptyMap(), null);
    }
}
