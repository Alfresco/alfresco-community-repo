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
package org.alfresco.repo.content.transform;

import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.tags.ATag;

import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.htmlcleaner.ContentToken;

/**
 * MediaWiki content transformer.  Converts mediawiki markup into HTML.
 * 
 * @see http://matheclipse.org/en/Java_Wikipedia_API
 * 
 * Tika Note - Tika doesn't currently support mediawiki markup, so this
 *  transformer cannot currently be converted to use Tika.
 * 
 * @author Roy Wetherall
 */
public class MediaWikiContentTransformer extends AbstractContentTransformer2
{
    /** The file folder service */
    private FileFolderService fileFolderService;
    
    /** The node service */
    private NodeService nodeService;
    
    /**
     * Sets the file folder service
     * 
     * @param fileFolderService     the file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * Sets the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Only transform from mediawiki to html
     * 
     * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!MimetypeMap.MIMETYPE_TEXT_MEDIAWIKI.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_HTML.equals(targetMimetype))
        {
            // only support MEDIAWIKI -> HTML
            return false;
        }
        else
        {
            return true;
        }
    }
    
     /**
      * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#transformInternal(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
      */
    public void transformInternal(ContentReader reader, ContentWriter writer,  TransformationOptions options)
            throws Exception
    {
        String imageURL = "{$image}";
        String pageURL = "${title}";
        
        // If we have context about the destination of the transformation then use it
        if (options.getTargetNodeRef() != null)
        {
            NodeRef parentNodeRef = this.nodeService.getPrimaryParent(options.getTargetNodeRef()).getParentRef();
            
            StringBuffer folderPath = new StringBuffer(256);
            List<FileInfo> fileInfos = this.fileFolderService.getNamePath(null, parentNodeRef);
            for (FileInfo fileInfo : fileInfos)
            {
                folderPath.append(fileInfo.getName()).append("/");
            }
            
            pageURL = "/alfresco/d/d?path=" + folderPath + "${title}.html";
            imageURL = "/alfresco/d/d?path=" + folderPath + "Images/${image}";
        }
        
        // Create the wikiModel and set the title and image link URL's
        AlfrescoWikiModel wikiModel = new AlfrescoWikiModel(imageURL, pageURL);
        
        // Render the wiki content as HTML
        writer.putContent(wikiModel.render(reader.getContentString()));
    }
    
    /**
     * Alfresco custom Wiki model used to generate links and image references
     * 
     * @author Roy Wetherall
     */
    private class AlfrescoWikiModel extends WikiModel
    {
        public AlfrescoWikiModel(String imageBaseURL, String linkBaseURL)
        {
            super(imageBaseURL, linkBaseURL);
        }
        
        @Override
        public void appendInternalLink(String link, String hashSection, String linkText)
        {
            link = link.replaceAll(":", " - ");            
            String encodedtopic = Encoder.encodeTitleUrl(link);
            encodedtopic = encodedtopic.replaceAll("_", " ");
            
            String hrefLink = fExternalWikiBaseURL.replace("${title}", encodedtopic);
            
            ATag aTagNode = new ATag();
            append(aTagNode);
            aTagNode.addAttribute("id", "w");
            String href = hrefLink;
            if (hashSection != null) {
                href = href + '#' + hashSection;
            }
            aTagNode.addAttribute("href", href);
            aTagNode.addObjectAttribute("wikilink", hrefLink);

            ContentToken text = new ContentToken(linkText);
            aTagNode.addChild(text);
        }
    }
}
