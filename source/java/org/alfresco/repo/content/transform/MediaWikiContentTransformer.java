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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform;

import info.bliki.wiki.model.WikiModel;

import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * MediaWiki content transformer.  Converts mediawiki markup into HTML.
 * 
 * @see http://matheclipse.org/en/Java_Wikipedia_API
 * 
 * @author Roy Wetherall
 */
public class MediaWikiContentTransformer extends AbstractContentTransformer
{
    //private static final Log logger = LogFactory.getLog(MediaWikiContentTransformer.class);
    
    /**
     * Only support TEXT to HTML
     */    
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_HTML.equals(targetMimetype))
        {
            // only support TEXT -> HTML
            return 0.0;
        }
        else
        {
            return 1.0;
        }
    }
    
    /**
     * @see org.alfresco.repo.content.transform.AbstractContentTransformer#transformInternal(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, java.util.Map)
     */
    public void transformInternal(ContentReader reader, ContentWriter writer,  Map<String, Object> options)
            throws Exception
    {
        // Create the wikiModel and set the title and image link URL's
        WikiModel wikiModel = new WikiModel("${image}", "${title}");
        
        // Render the wiki content as HTML
        writer.putContent(wikiModel.render(reader.getContentString()));
    }
}
