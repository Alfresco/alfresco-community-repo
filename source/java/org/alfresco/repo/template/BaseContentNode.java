/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

/**
 * Base class for Template API objects that supply content functionality.
 * 
 * @author Kevin Roast
 */
public abstract class BaseContentNode implements TemplateContent
{
    protected final static String CONTENT_DEFAULT_URL = "/download/direct/{0}/{1}/{2}/{3}";
    protected final static String CONTENT_PROP_URL    = "/download/direct/{0}/{1}/{2}/{3}?property={4}";
    protected final static String FOLDER_BROWSE_URL   = "/navigate/browse/{0}/{1}/{2}";
    
    protected final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    
    /** The children of this node */
    protected List<TemplateProperties> children = null;
    
    protected ServiceRegistry services = null;
    protected TemplateImageResolver imageResolver = null;
    private Set<QName> aspects = null;
    private String displayPath = null;
    
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    
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
            try
            {
                displayPath = this.services.getNodeService().getPath(getNodeRef()).toDisplayPath(this.services.getNodeService());
            }
            catch (AccessDeniedException err)
            {
                displayPath = "";
            }
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
        }
        
        return this.children;
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
            this.aspects = this.services.getNodeService().getAspects(getNodeRef());
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
            try
            {
                return MessageFormat.format(CONTENT_DEFAULT_URL, new Object[] {
                        getNodeRef().getStoreRef().getProtocol(),
                        getNodeRef().getStoreRef().getIdentifier(),
                        getNodeRef().getId(),
                        StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20") } );
            }
            catch (UnsupportedEncodingException err)
            {
                throw new TemplateException("Failed to encode content URL for node: " + getNodeRef(), err);
            }
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
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getMimetype() : null);
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
        public String getContent(int length)
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
                result = getContent(length);
            }
            else
            {
                // get the content reader
                ContentService contentService = services.getContentService();
                ContentReader reader = contentService.getReader(getNodeRef(), property);
                
                // get the writer and set it up for text convert
                ContentWriter writer = contentService.getWriter(null, ContentModel.PROP_CONTENT, true);
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
        
        /**
         * @return 
         */
        public String getUrl()
        {
            try
            {
                return MessageFormat.format(CONTENT_PROP_URL, new Object[] {
                       getNodeRef().getStoreRef().getProtocol(),
                       getNodeRef().getStoreRef().getIdentifier(),
                       getNodeRef().getId(),
                       StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20"),
                       StringUtils.replace(URLEncoder.encode(property.toString(), "UTF-8"), "+", "%20") } );
            }
            catch (UnsupportedEncodingException err)
            {
                throw new TemplateException("Failed to encode content URL for node: " + getNodeRef(), err);
            }
        }
        
        public long getSize()
        {
            return contentData.getSize();
        }
        
        public String getMimetype()
        {
            return contentData.getMimetype();
        }
        
        private ContentData contentData;
        private QName property;
    }
}
