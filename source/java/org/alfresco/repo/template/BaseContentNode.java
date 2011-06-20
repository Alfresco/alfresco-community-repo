/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Base class for Template API objects that supply content functionality.
 * 
 * @author Kevin Roast
 */
public abstract class BaseContentNode implements TemplateContent
{
    protected final static String CONTENT_GET_URL      = "/d/d/{0}/{1}/{2}/{3}";
    protected final static String CONTENT_GET_PROP_URL = "/d/d/{0}/{1}/{2}/{3}?property={4}";
    protected final static String CONTENT_DOWNLOAD_URL      = "/d/a/{0}/{1}/{2}/{3}";
    protected final static String CONTENT_DOWNLOAD_PROP_URL = "/d/a/{0}/{1}/{2}/{3}?property={4}";
    protected final static String CONTENT_SERVICE_GET_URL        = "/api/node/content/{0}/{1}/{2}/{3}";
    protected final static String CONTENT_SERVICE_GET_PROP_URL   = "/api/node/content;{4}/{0}/{1}/{2}/{3}";
    protected final static String FOLDER_BROWSE_URL    = "/n/browse/{0}/{1}/{2}";
    
    protected final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    
    /** The children of this node */
    protected List<TemplateProperties> children = null;
    
    protected ServiceRegistry services = null;
    protected TemplateImageResolver imageResolver = null;
    protected Set<QName> aspects = null;
    private String displayPath = null;
    
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    private Boolean isLinkToDocument = null;
    private Boolean isLinkToContainer = null;
    private Boolean hasChildren = null;
    private String siteName;
    private boolean siteNameResolved = false;
    
    /**
     * @return true if this Node is a container (i.e. a folder)
     */
    public boolean getIsContainer()
    {
        if (isContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isContainer = Boolean.valueOf( (dd.isSubClass(getType(), ContentModel.TYPE_FOLDER) == true && 
                    dd.isSubClass(getType(), ContentModel.TYPE_SYSTEM_FOLDER) == false) );
        }
        
        return isContainer.booleanValue();
    }
    
    /**
     * @return true if this Node is a Document (i.e. with content)
     */
    public boolean getIsDocument()
    {
        if (isDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isDocument = Boolean.valueOf(dd.isSubClass(getType(), ContentModel.TYPE_CONTENT));
        }
        
        return isDocument.booleanValue();
    }

    /**
     * @return true if this Node is a Link to a Container (i.e. a folderlink)
     */
    public boolean getIsLinkToContainer()
    {
        if (isLinkToContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isLinkToContainer = Boolean.valueOf(dd.isSubClass(getType(), ApplicationModel.TYPE_FOLDERLINK));
        }
        
        return isLinkToContainer.booleanValue();
    }

    /**
     * @return true if this Node is a Link to a Document (i.e. a filelink)
     */
    public boolean getIsLinkToDocument()
    {
        if (isLinkToDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isLinkToDocument = Boolean.valueOf(dd.isSubClass(getType(), ApplicationModel.TYPE_FILELINK));
        }
        
        return isLinkToDocument.booleanValue();
    }

    /**
     * Override Object.toString() to provide useful debug output
     */
    public String toString()
    {
        if (this.services.getNodeService().exists(getNodeRef()))
        {
            return "Node Type: " + getType() + 
                   "\tNode Ref: " + getNodeRef().toString();
        }
        else
        {
            return "Node no longer exists: " + getNodeRef();
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Content display API 
    
    /**
     * @return QName path to this node. This can be used for Lucene PATH: style queries
     */
    public String getQnamePath()
    {
        return this.services.getNodeService().getPath(getNodeRef()).toPrefixString(this.services.getNamespaceService());
    }
    
    /**
     * @return the small icon image for this node
     */
    public String getIcon16()
    {
        if (this.imageResolver != null)
        {
            if (getIsDocument())
            {
                return this.imageResolver.resolveImagePathForName(getName(), FileTypeImageSize.Small);
            }
            else
            {
                String icon = (String)getProperties().get("app:icon");
                if (icon != null)
                {
                    return "/images/icons/" + icon + "-16.gif";
                }
                else
                {
                    return "/images/icons/space-icon-default-16.gif";
                }
            }
        }
        else
        {
            return "/images/filetypes/_default.gif";
        }
    }
    
    /**
     * @return the medium icon image for this node
     */
    public String getIcon32()
    {
        if (this.imageResolver != null)
        {
            if (getIsDocument())
            {
                return this.imageResolver.resolveImagePathForName(getName(), FileTypeImageSize.Medium);
            }
            else
            {
                String icon = (String)getProperties().get("app:icon");
                if (icon != null)
                {
                    return "/images/icons/" + icon + ".gif";
                }
                else
                {
                    return "/images/icons/space-icon-default.gif";
                }
            }
        }
        else
        {
            return "/images/filetypes32/_default.gif";
        }
    }
    
    /**
     * @return the large icon image for this node
     */
    public String getIcon64()
    {
        if (this.imageResolver != null)
        {
            if (getIsDocument())
            {
                return this.imageResolver.resolveImagePathForName(getName(), FileTypeImageSize.Large);
            }
            else
            {
                String icon = (String)getProperties().get("app:icon");
                if (icon != null)
                {
                    return "/images/icons/" + icon + "-64.png";
                }
                else
                {
                    return "/images/icons/space-icon-default-64.png";
                }
            }
        }
        else
        {
            return "/images/filetypes64/_default.gif";
        }
    }
    
    /**
     * @return Display path to this node - the path built of 'cm:name' attribute values.
     */
    public String getDisplayPath()
    {
        if (displayPath == null)
        {
            displayPath = this.services.getNodeService().getPath(getNodeRef()).toDisplayPath(
                    services.getNodeService(), services.getPermissionService());
        }
        
        return displayPath;
    }
    
    
    // ------------------------------------------------------------------------------
    // TemplateProperties contract impl
    
    /**
     * @return The children of this Node as objects that support the TemplateProperties contract.
     */
    public List<TemplateProperties> getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.services.getNodeService().getChildAssocs(getNodeRef());
            this.children = new ArrayList<TemplateProperties>(childRefs.size());
            for (ChildAssociationRef ref : childRefs)
            {
                // create our Node representation from the NodeRef
                TemplateNode child = new TemplateNode(ref.getChildRef(), this.services, this.imageResolver);
                this.children.add(child);
            }
            this.hasChildren = (childRefs.size() != 0);
        }
        
        return this.children;
    }
    
    /**
     * @return true if the node has the children false otherwise
     */
    public boolean getHasChildren()
    {
        if (this.hasChildren == null)
        {
             this.hasChildren = !this.services.getNodeService().getChildAssocs(
                   getNodeRef(), RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false).isEmpty();
        }
        return this.hasChildren;
    }
    
    /**
     * @return The list of aspects applied to this node
     */
    public Set<QName> getAspects()
    {
        if (this.aspects == null)
        {
            this.aspects = this.services.getNodeService().getAspects(getNodeRef());
        }
        
        return this.aspects;
    }
    
    /**
     * @param aspect The aspect name to test for
     * 
     * @return true if the node has the aspect false otherwise
     */
    public boolean hasAspect(String aspect)
    {
        if (this.aspects == null)
        {
            getAspects();
        }
        
        if (aspect.startsWith(NAMESPACE_BEGIN))
        {
            return this.aspects.contains((QName.createQName(aspect)));
        }
        else
        {
            boolean found = false;
            for (QName qname : this.aspects)
            {
                if (qname.toPrefixString(this.services.getNamespaceService()).equals(aspect))
                {
                    found = true;
                    break;
                }
            }
            return found;
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Content API 
    
    /**
     * @return the content String for this node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return content != null ? content.getContent() : "";
    }
    
    /**
     * @return For a content document, this method returns the URL to the content stream for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        if (getIsDocument() == true)
        {
            TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            return content != null ? content.getUrl() : "";
        }
        else
        {
            return MessageFormat.format(FOLDER_BROWSE_URL, new Object[] {
                    getNodeRef().getStoreRef().getProtocol(),
                    getNodeRef().getStoreRef().getIdentifier(),
                    getNodeRef().getId() } );
        }
    }
    
    /**
     * @return For a content document, this method returns the download URL to the content for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method returns an empty string
     */
    public String getDownloadUrl()
    {
        if (getIsDocument() == true)
        {
            TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            return content != null ? content.getDownloadUrl() : "";
        }
        else
        {
            return "";
        }
    }
    
    public String getServiceUrl()
    {
        if (getIsDocument() == true)
        {
            TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            return content != null ? content.getServiceUrl() : "";
        }
        else
        {
            return "";
        }
    }
    
    /**
     * @return The WebDav cm:name based path to the content for the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getWebdavUrl()
    {
        try
        {
            if (getIsContainer() || getIsDocument())
            {
                List<FileInfo> paths = this.services.getFileFolderService().getNamePath(null, getNodeRef());
                
                // build up the webdav url
                StringBuilder path = new StringBuilder(128);
                path.append("/webdav");
                
                // build up the path skipping the first path as it is the root folder
                for (int i=1; i<paths.size(); i++)
                {
                    path.append("/")
                        .append(URLEncoder.encode(paths.get(i).getName()));
                }
                return path.toString();
            }
        }
        catch (FileNotFoundException nodeErr)
        {
            // cannot build path if file no longer exists
            return "";
        }
        return "";
    }
    
    /**
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getMimetype() : null);
    }
    
    /**
     * @return The display label of the mimetype encoding for content attached to the node from the default
     *         content property (@see ContentModel.PROP_CONTENT)
     */
    public String getDisplayMimetype()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getDisplayMimetype() : null);
    }
    
    /**
     * @return The character encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getEncoding()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getEncoding() : null);
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getSize() : 0L);
    }
    
    /**
     * Helper to return true if the supplied property value is a TemplateContentData object
     * 
     * @param o     Object to test
     * 
     * @return true if instanceof TemplateContentData, false otherwise
     */
    public boolean isTemplateContent(Object o)
    {
        return (o instanceof TemplateContentData);
    }
    
    /**
     * Helper to return true if the supplied property value is a TemplateNodeRef object
     * 
     * @param o     Object to test
     * 
     * @return true if instanceof isTemplateNodeRef, false otherwise
     */
    public boolean isTemplateNodeRef(Object o)
    {
        return (o instanceof TemplateNodeRef);
    }
    
    // ------------------------------------------------------------------------------
    // Site methods
    
    /**
     * Returns the short name of the site this node is located within. If the 
     * node is not located within a site null is returned.
     * 
     * @return The short name of the site this node is located within, null
     *         if the node is not located within a site.
     */
    public String getSiteShortName()
    {
        if (!this.siteNameResolved)
        {
            this.siteNameResolved = true;
            
            Path path = this.services.getNodeService().getPath(getNodeRef());
            
            for (int i = 0; i < path.size(); i++)
            {
                if ("st:sites".equals(path.get(i).getPrefixedString(this.services.getNamespaceService())))
                {
                    // we now know the node is in a site, find the next element in the array (if there is one)
                    if ((i+1) < path.size())
                    {
                        // get the site name
                        Path.Element siteName = path.get(i+1);
                     
                        // remove the "cm:" prefix and add to result object
                        this.siteName = ISO9075.decode(siteName.getPrefixedString(
                                    this.services.getNamespaceService()).substring(3));
                    }
                  
                    break;
                }
            }
        }
        
        return this.siteName;
    }
    
    // ------------------------------------------------------------------------------
    // Inner classes 
    
    /**
     * Inner class wrapping and providing access to a ContentData property 
     */
    public class TemplateContentData implements Serializable
    {
       /**
        * Constructor
        * 
        * @param contentData  The ContentData object this object wraps 
        * @param property     The property the ContentData is attached too
        */
        public TemplateContentData(ContentData contentData, QName property)
        {
            this.contentData = contentData;
            this.property = property;
        }
        
        /**
         * @return the content stream
         */
        public String getContent()
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(getNodeRef(), property);
            
            return (reader != null && reader.exists()) ? reader.getContentString() : "";
        }
        
        /**
         * @return the content stream to the specified maximum length in characters
         */
        public String getContentMaxLength(int length)
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(getNodeRef(), property);
            
            return (reader != null && reader.exists()) ? reader.getContentString(length) : "";
        }
        
        /**
         * @param length      Length of the character stream to return, or -1 for all
         * 
         * @return the binary content stream converted to text using any available transformer
         *         if fails to convert then null will be returned
         */
        public String getContentAsText(int length)
        {
            String result = null;
            
            if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(getMimetype()))
            {
                result = getContentMaxLength(length);
            }
            else
            {
                // get the content reader
                ContentService contentService = services.getContentService();
                ContentReader reader = contentService.getReader(getNodeRef(), property);
                
                // get the writer and set it up for text convert
                ContentWriter writer = contentService.getTempWriter();
                writer.setMimetype("text/plain"); 
                writer.setEncoding(reader.getEncoding());
                
                // try and transform the content
                if (contentService.isTransformable(reader, writer))
                {
                    contentService.transform(reader, writer);
                    
                    ContentReader resultReader = writer.getReader();
                    if (resultReader != null && reader.exists())
                    {
                       if (length != -1)
                       {
                           result = resultReader.getContentString(length);
                       }
                       else
                       {
                           result = resultReader.getContentString();
                       }
                    }
                }
            }
            return result;
        }
        
        public String getUrl()
        {
            if (ContentModel.PROP_CONTENT.equals(property))
            {
                return buildUrl(CONTENT_GET_URL);
            }
            else
            {
                return buildPropUrl(CONTENT_GET_PROP_URL);
            }
        }
        
        public String getDownloadUrl()
        {
            if (ContentModel.PROP_CONTENT.equals(property))
            {
                return buildUrl(CONTENT_DOWNLOAD_URL);
            }
            else
            {
                return buildPropUrl(CONTENT_DOWNLOAD_PROP_URL);
            }
        }
        
        public String getServiceUrl()
        {
            if (ContentModel.PROP_CONTENT.equals(property))
            {
                return buildUrl(CONTENT_SERVICE_GET_URL);
            }
            else
            {
                return buildPropUrl(CONTENT_SERVICE_GET_PROP_URL);
            }
        }
        
        private String buildUrl(String format)
        {
           return MessageFormat.format(format, new Object[] {
                     getNodeRef().getStoreRef().getProtocol(),
                     getNodeRef().getStoreRef().getIdentifier(),
                     getNodeRef().getId(),
                     URLEncoder.encode(getName()) } );
        }
        
        private String buildPropUrl(String pformat)
        {
            return MessageFormat.format(pformat, new Object[] {
                     getNodeRef().getStoreRef().getProtocol(),
                     getNodeRef().getStoreRef().getIdentifier(),
                     getNodeRef().getId(),
                     URLEncoder.encode(getName()),
                     URLEncoder.encode(property.toString()) } );
        }
        
        public long getSize()
        {
            return contentData.getSize();
        }
        
        public String getMimetype()
        {
            return contentData.getMimetype();
        }
        
        public String getDisplayMimetype()
        {
            return services.getMimetypeService().getDisplaysByMimetype().get(getMimetype());
        }
        
        public String getEncoding()
        {
            return contentData.getEncoding();
        }
        
        private ContentData contentData;
        private QName property;
    }
}
