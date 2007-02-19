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

import java.io.File;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.beans.StringBean;

/**
 * @see http://htmlparser.sourceforge.net/
 * @see org.htmlparser.beans.StringBean
 * 
 * @author Derek Hulley
 */
public class HtmlParserContentTransformer extends AbstractContentTransformer
{
    private static final Log logger = LogFactory.getLog(HtmlParserContentTransformer.class);
    
    /**
     * Only support HTML to TEXT.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!MimetypeMap.MIMETYPE_HTML.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support HTML -> TEXT
            return 0.0;
        }
        else
        {
            return 1.0;
        }
    }

    public void transformInternal(ContentReader reader, ContentWriter writer,  Map<String, Object> options)
            throws Exception
    {
        // we can only work from a file
        File htmlFile = TempFileProvider.createTempFile("HtmlParserContentTransformer_", ".html");
        reader.getContent(htmlFile);
        
        // create the extractor
        StringBean extractor = new StringBean();
        extractor.setCollapse(false);
        extractor.setLinks(false);
        extractor.setReplaceNonBreakingSpaces(false);
        extractor.setURL(htmlFile.getAbsolutePath());

        // get the text
        String text = extractor.getStrings();
        // write it to the writer
        writer.putContent(text);
    }
}
