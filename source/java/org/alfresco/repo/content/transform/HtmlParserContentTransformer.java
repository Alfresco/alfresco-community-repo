/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.io.File;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Content transformer which wraps the HTML Parser library for 
 * parsing HTML content.
 * 
 * <p>
 * Since HTML Parser was updated from v1.6 to v2.1, META tags
 * defining an encoding for the content via http-equiv=Content-Type
 * will ONLY be respected if the encoding of the content item
 * itself is set to ISO-8859-1.
 * </p>
 * 
 * <p>
 * Tika Note - could be converted to use the Tika HTML parser,
 *  but we'd potentially need a custom text handler to replicate
 *  the current settings around links and non-breaking spaces.
 * </p>
 * 
 * @see <a href="http://htmlparser.sourceforge.net/">http://htmlparser.sourceforge.net</a>
 * @see org.htmlparser.beans.StringBean
 * @see <a href="http://sourceforge.net/tracker/?func=detail&aid=1644504&group_id=24399&atid=381401">HTML Parser</a>
 * 
 * @author Derek Hulley
 */
public class HtmlParserContentTransformer extends AbstractContentTransformer2
{
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(HtmlParserContentTransformer.class);
    
    /**
     * Only support HTML to TEXT.
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!MimetypeMap.MIMETYPE_HTML.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support HTML -> TEXT
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getComments(boolean available)
    {
        return onlySupports(MimetypeMap.MIMETYPE_HTML, MimetypeMap.MIMETYPE_TEXT_PLAIN, available);
    }

    public void transformInternal(ContentReader reader, ContentWriter writer,  TransformationOptions options)
            throws Exception
    {
        // We can only work from a file
        File htmlFile = TempFileProvider.createTempFile("HtmlParserContentTransformer_", ".html");
        reader.getContent(htmlFile);
        
        // Fetch the encoding of the HTML, as set in the ContentReader
        // This will default to 'UTF-8' if not specifically set
        String encoding = reader.getEncoding();
        
        // Create the extractor
        EncodingAwareStringBean extractor = new EncodingAwareStringBean();
        extractor.setCollapse(false);
        extractor.setLinks(false);
        extractor.setReplaceNonBreakingSpaces(false);
        extractor.setURL(htmlFile, encoding);        
        // get the text
        String text = extractor.getStrings();
        // write it to the writer
        writer.putContent(text);
        
        // Tidy up
        htmlFile.delete();
    }
}
