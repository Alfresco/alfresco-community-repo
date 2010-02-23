/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.renditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionKind;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;

/**
 * Rendition Service Implementation
 * 
 * @author Stas Sokolovsky
 */
public class CMISRenditionServiceImpl implements CMISRenditionService
{

    /** Rendition filter constants */
    private static final String FILTER_WILDCARD = "*";
    private static final String FILTER_NONE = "cmis:none";
    private static final String FILTER_DELIMITER = ",";
    private static final String SUBTYPES_POSTFIX = "/*";
    private static final String SUBTYPES_DELIMITER = "/";

    /** Service dependencies */
    private ThumbnailService thumbnailService;
    private NodeService nodeService;

    /** Kind to thumbnail mapping */
    private Map<String, List<String>> kindToThumbnailNames = new HashMap<String, List<String>>();
    private Map<String, CMISRenditionKind> thumbnailNamesToKind = new HashMap<String, CMISRenditionKind>();

    /** Custom renditions */
    private CustomRenditionsCache customRenditionsCache;

    /**
     * @see org.alfresco.cmis.CMISRenditionService#getRenditions(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public List<CMISRendition> getRenditions(NodeRef node, String renditionFilter)
    {
        Collection<CMISRendition> result = null;
        ThumbnailFilter thumbnailFilter = getThumbnailFilter(renditionFilter);
        if (thumbnailFilter != null)
        {
            result = getRenditions(node, thumbnailFilter);
        }
        return result != null ? new ArrayList<CMISRendition>(result) : null;
    }

    /**
     * Get renditions for a document.
     * 
     * @param node node reference of document
     * @param thumbnailFilter thumbnail filter
     * @return set of renditions
     */
    private Set<CMISRendition> getRenditions(NodeRef node, ThumbnailFilter thumbnailFilter)
    {
        Set<CMISRendition> result = new HashSet<CMISRendition>();
        if (thumbnailFilter.isAny())
        {
            result.addAll(getAllRenditions(node));
        }
        else
        {
            for (String thumbnailName : thumbnailFilter.getThumbnailNames())
            {
                CMISRendition rendition = getRenditionByThumbnailName(node, thumbnailName);
                if (rendition != null)
                {
                    result.add(rendition);
                }
            }
            for (String mimetype : thumbnailFilter.getMimetypes())
            {
                result.addAll(getRenditionByMimetype(node, mimetype));
            }
        }
        result.addAll(getCustomRenditions(thumbnailFilter));
        return result;
    }

    /**
     * Get rendition by thumbnail name.
     * 
     * @param node node reference of document
     * @param thumbnailName thumbnail name
     * @return rendition
     */
    private CMISRendition getRenditionByThumbnailName(NodeRef node, String thumbnailName)
    {
        CMISRendition result = null;

        NodeRef thumbnailNode = thumbnailService.getThumbnailByName(node, ContentModel.PROP_CONTENT, thumbnailName);

        if (thumbnailNode != null)
        {
            result = getRendition(thumbnailNode, node);
        }
        return result;
    }

    /**
     * Get rendition by mimetype.
     * 
     * @param node node reference of document
     * @param mimetype rendition mimetype
     * @return list of renditions
     */
    private List<CMISRendition> getRenditionByMimetype(NodeRef node, String mimetype)
    {
        List<CMISRendition> result = new ArrayList<CMISRendition>();

        List<NodeRef> thumbnails = thumbnailService.getThumbnails(node, ContentModel.PROP_CONTENT, mimetype, null);

        if (thumbnails != null)
        {
            for (NodeRef thumbnailNode : thumbnails)
            {
                CMISRendition rendition = getRendition(thumbnailNode, node);
                if (rendition != null)
                {
                    result.add(rendition);
                }
            }
        }
        return result;
    }

    /**
     * Convert the rendition filter to thumbnail filter.
     * 
     * @param renditionFilter rendition filter
     * @return thumbnail filter
     */
    private ThumbnailFilter getThumbnailFilter(String renditionFilter)
    {
        ThumbnailFilter result = null;
        if (renditionFilter != null && !renditionFilter.equals(FILTER_NONE))
        {
            result = new ThumbnailFilter();
            if (renditionFilter.equals(FILTER_WILDCARD))
            {
                result.setAny(true);
            }
            else
            {
                String[] filterElements = renditionFilter.split(FILTER_DELIMITER);
                if (filterElements == null || filterElements.length < 1)
                {
                    throw new AlfrescoRuntimeException("Invalid rendition filter");
                }
                for (String filterElement : filterElements)
                {
                    filterElement = filterElement.trim();
                    if (isRenditionKind(filterElement))
                    {
                        CMISRenditionKind kind = null;
                        for (CMISRenditionKind renditionKind : CMISRenditionKind.values())
                        {
                            if (renditionKind.getLabel().equals(filterElement))
                            {
                                kind = renditionKind;
                            }
                        }
                        result.getKinds().add(kind);
                        List<String> thumbnails = kindToThumbnailNames.get(filterElement);
                        if (thumbnails != null)
                        {
                            result.getThumbnailNames().addAll(thumbnails);
                        }
                    }
                    else
                    {
                        result.getMimetypes().add(filterElement);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the thumbnail definition.
     * 
     * @param thumbnailName thumbnail name
     * @return thumbnail definition
     */
    private ThumbnailDefinition getThumbnailDefinition(String thumbnailName)
    {
        return thumbnailService.getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
    }

    /**
     * Get the image attributes of thumbnail.
     * 
     * @param thumbnailName thumbnail name
     * @return image attributes
     */
    private ImageResizeOptions getImageAttributes(String thumbnailName)
    {
        ThumbnailDefinition thumbnailDefinition = getThumbnailDefinition(thumbnailName);
        if (thumbnailDefinition != null && thumbnailDefinition.getTransformationOptions() != null
                && thumbnailDefinition.getTransformationOptions() instanceof ImageTransformationOptions)
        {
            return ((ImageTransformationOptions) thumbnailDefinition.getTransformationOptions()).getResizeOptions();
        }
        return null;
    }

    /**
     * @param filterElement filter element
     * @return true if filter element is rendition kind
     */
    private boolean isRenditionKind(String filterElement)
    {
        boolean result = false;
        for (CMISRenditionKind renditionKind : CMISRenditionKind.values())
        {
            if (renditionKind.getLabel().equals(filterElement))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Create CMISRendition by thumbnailNode and documentNode.
     * 
     * @param thumbnailNode thumbnail node reference
     * @param documentNode document node reference
     * @return CMISRendition
     */
    private CMISRendition getRendition(NodeRef thumbnailNode, NodeRef documentNode)
    {
        CMISRenditionImpl rendition = null;

        String thumbnailName = (String) nodeService.getProperty(thumbnailNode, ContentModel.PROP_THUMBNAIL_NAME);
        CMISRenditionKind kind = thumbnailNamesToKind.get(thumbnailName);
        if (thumbnailName != null && kind != null)
        {
            rendition = new CMISRenditionImpl();
            ContentData contentData = (ContentData) nodeService.getProperty(thumbnailNode, ContentModel.PROP_CONTENT);
            rendition.setNodeRef(thumbnailNode);
            rendition.setStreamId(thumbnailNode.toString());
            rendition.setRenditionDocumentId(documentNode.toString());
            rendition.setTitle(thumbnailName);
            rendition.setKind(kind);
            rendition.setMimeType(contentData.getMimetype());
            rendition.setLength((int) contentData.getSize());

            ImageResizeOptions imageAttributes = getImageAttributes(thumbnailName);
            if (imageAttributes != null)
            {
                rendition.setWidth(imageAttributes.getWidth());
                rendition.setHeight(imageAttributes.getHeight());
            }
        }
        return rendition;
    }

    /**
     * Get custom renditions.
     * 
     * @param thumbnailFilter thumbnail filter
     * @return list of renditions
     */
    private List<CMISRendition> getCustomRenditions(ThumbnailFilter filter)
    {
        List<CMISRendition> result = new ArrayList<CMISRendition>();
        if (customRenditionsCache != null)
        {
            if (filter.isAny())
            {
                result.addAll(customRenditionsCache.getAllRenditions());
            }
            else
            {
                for (CMISRenditionKind kind : filter.getKinds())
                {
                    List<CMISRendition> renditions = customRenditionsCache.getRenditionsByKind(kind);
                    if (renditions != null)
                    {
                        result.addAll(renditions);
                    }
                }
                for (String mimetype : filter.getMimetypes())
                {
                    List<CMISRendition> renditions = customRenditionsCache.getRenditionsByMimeType(mimetype);
                    if (renditions != null)
                    {
                        result.addAll(renditions);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all renditions for a document.
     * 
     * @param node document node ref
     * @return list of renditions
     */
    private List<CMISRendition> getAllRenditions(NodeRef node)
    {
        return getRenditionByMimetype(node, null);
    }

    /**
     * Set rendition kind mapping.
     * 
     * @param RenditionKind to Thumbnail Definition mapping
     */
    public void setRenditionKindMapping(Map<String, List<String>> renditionKinds)
    {
        this.kindToThumbnailNames = renditionKinds;
        for (Entry<String, List<String>> entry : renditionKinds.entrySet())
        {
            CMISRenditionKind kind = null;
            for (CMISRenditionKind renditionKind : CMISRenditionKind.values())
            {
                if (renditionKind.getLabel().equals(entry.getKey()))
                {
                    kind = renditionKind;
                }
            }
            for (String thumbnailName : entry.getValue())
            {
                thumbnailNamesToKind.put(thumbnailName, kind);
            }
        }
    }

    /**
     * Set custom renditions.
     * 
     * @param renditions list of renditions
     */
    public void setCustomRenditions(List<CMISRendition> renditions)
    {
        this.customRenditionsCache = new CustomRenditionsCache(renditions);
    }

    /**
     * Set the NodeService.
     * 
     * @param nodeService NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the ThumbnailService.
     * 
     * @param thumbnailService thumbnailService
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    /**
     * Cache that aggregates renditions informaition to allow fast searching by kind and mimetype    
     */
    private class CustomRenditionsCache
    {
        private Map<CMISRenditionKind, List<CMISRendition>> renditionsByKind;
        private Map<String, List<CMISRendition>> renditionsByMimeType;
        private Map<String, List<CMISRendition>> renditionsByBaseMimeType;
        private List<CMISRendition> allRenditions;

        public CustomRenditionsCache(List<CMISRendition> renditions)
        {
            allRenditions = renditions;
            renditionsByKind = new HashMap<CMISRenditionKind, List<CMISRendition>>(renditions.size());
            renditionsByMimeType = new HashMap<String, List<CMISRendition>>(renditions.size());
            renditionsByBaseMimeType = new HashMap<String, List<CMISRendition>>(renditions.size());
            for (CMISRendition rendition : renditions)
            {
                String baseType = getBaseType(rendition.getMimeType());
                if (!renditionsByKind.containsKey(rendition.getKind()))
                {
                    renditionsByKind.put(rendition.getKind(), new ArrayList<CMISRendition>(1));
                }
                if (!renditionsByMimeType.containsKey(rendition.getMimeType()))
                {
                    renditionsByMimeType.put(rendition.getMimeType(), new ArrayList<CMISRendition>(1));
                }
                if (!renditionsByBaseMimeType.containsKey(baseType))
                {
                    renditionsByBaseMimeType.put(baseType, new ArrayList<CMISRendition>(1));
                }
                renditionsByKind.get(rendition.getKind()).add(rendition);
                renditionsByMimeType.get(rendition.getMimeType()).add(rendition);
                renditionsByBaseMimeType.get(baseType).add(rendition);
            }
        }

        public List<CMISRendition> getRenditionsByKind(CMISRenditionKind kind)
        {
            return renditionsByKind.get(kind);
        }

        public List<CMISRendition> getRenditionsByMimeType(String mimetype)
        {
            if (mimetype.endsWith(SUBTYPES_POSTFIX))
            {
                String baseMimetype = mimetype.substring(0, mimetype.length() - SUBTYPES_POSTFIX.length());
                return renditionsByBaseMimeType.get(baseMimetype);
            }
            else
            {
                return renditionsByMimeType.get(mimetype);
            }
        }

        public Collection<CMISRendition> getAllRenditions()
        {
            return allRenditions;
        }

        private String getBaseType(String mimetype)
        {
            String baseMymetype = mimetype;
            int subTypeIndex = mimetype.indexOf(SUBTYPES_DELIMITER);
            if (subTypeIndex > 0 || subTypeIndex < mimetype.length())
            {
                baseMymetype = mimetype.substring(0, subTypeIndex);
            }
            return baseMymetype;
        }
    }

    /**
     * Parsed RenditionFilter     
     */
    private class ThumbnailFilter
    {
        private List<CMISRenditionKind> kinds = new ArrayList<CMISRenditionKind>();

        private List<String> thumbnailNames = new ArrayList<String>();

        private List<String> mimetypes = new ArrayList<String>();

        private boolean any = false;

        public List<String> getThumbnailNames()
        {
            return thumbnailNames;
        }

        public List<String> getMimetypes()
        {
            return mimetypes;
        }

        public List<CMISRenditionKind> getKinds()
        {
            return kinds;
        }

        public boolean isAny()
        {
            return any;
        }

        public void setAny(boolean any)
        {
            this.any = any;
        }

    }
}
