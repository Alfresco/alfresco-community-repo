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

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converts any textual format to plain text.
 * <p>
 * The transformation is sensitive to the source and target string encodings.
 * 
 * @author Derek Hulley
 */
public class StringExtractingContentTransformer extends AbstractContentTransformer
{
    public static final String PREFIX_TEXT = "text/";
    
    private static final Log logger = LogFactory.getLog(StringExtractingContentTransformer.class);
    
    /**
     * Gives a high reliability for all translations from <i>text/sometype</i> to
     * <i>text/plain</i>.  As the text formats are already text, the characters
     * are preserved and no actual conversion takes place.
     * <p>
     * Extraction of text from binary data is wholly unreliable.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN))
        {
            // can only convert to plain text
            return 0.0;
        }
        else if (sourceMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN))
        {
            // conversions from any plain text format are very reliable
            return 1.0;
        }
        else if (sourceMimetype.startsWith(PREFIX_TEXT))
        {
            // the source is text, but probably with some kind of markup
            return 0.1;
        }
        else
        {
            // extracting text from binary is not useful
            return 0.0;
        }
    }

    /**
     * Text to text conversions are done directly using the content reader and writer string
     * manipulation methods.
     * <p>
     * Extraction of text from binary content attempts to take the possible character
     * encoding into account.  The text produced from this will, if the encoding was correct,
     * be unformatted but valid. 
     */
    @Override
    public void transformInternal(ContentReader reader, ContentWriter writer,  Map<String, Object> options)
            throws Exception
    {
        // is this a straight text-text transformation
        transformText(reader, writer);
    }
    
    /**
     * Transformation optimized for text-to-text conversion
     */
    private void transformText(ContentReader reader, ContentWriter writer) throws Exception
    {
        // get a char reader and writer
        Reader charReader = null;
        Writer charWriter = null;
        try
        {
            if (reader.getEncoding() == null)
            {
                charReader = new InputStreamReader(reader.getContentInputStream());
            }
            else
            {
                charReader = new InputStreamReader(reader.getContentInputStream(), reader.getEncoding());
            }
            if (writer.getEncoding() == null)
            {
                charWriter = new OutputStreamWriter(writer.getContentOutputStream());
            }
            else
            {
                charWriter = new OutputStreamWriter(writer.getContentOutputStream(), writer.getEncoding());
            }
            // copy from the one to the other
            char[] buffer = new char[1024];
            int readCount = 0;
            while (readCount > -1)
            {
                // write the last read count number of bytes
                charWriter.write(buffer, 0, readCount);
                // fill the buffer again
                readCount = charReader.read(buffer);
            }
        }
        finally
        {
            if (charReader != null)
            {
                try { charReader.close(); } catch (Throwable e) { logger.error(e); }
            }
            if (charWriter != null)
            {
                try { charWriter.close(); } catch (Throwable e) { logger.error(e); }
            }
        }
        // done
    }
}
