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

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This badly named transformer turns Microsoft Word documents
 *  (Word 6, 95, 97, 2000, 2003) into plain text.
 * 
 * Doesn't currently use {@link http://tika.apache.org/ Apache Tika} to
 *  do this, pending TIKA-408. When Apache POI 3.7 beta 2 has been
 *  released, we can switch to Tika and then handle Word 6,
 *  Word 95, Word 97, 2000, 2003, 2007 and 2010 formats.
 *  
 * TODO Switch to Tika in November 2010 once 3.4 is out
 * 
 * @author Nick Burch
 */
public class TextMiningContentTransformer extends AbstractContentTransformer2
{
    public TextMiningContentTransformer()
    {
    }
    
    /**
     * Currently the only transformation performed is that of text extraction from Word documents.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!MimetypeMap.MIMETYPE_WORD.equals(sourceMimetype) ||
                !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support DOC -> Text
            return false;
        }
        else
        {
            return true;
        }
    }

    public void transformInternal(ContentReader reader, ContentWriter writer,  TransformationOptions options)
            throws Exception
    {
       POIOLE2TextExtractor extractor = null;
        InputStream is = null;
        String text = null;
        try
        {
            is = reader.getContentInputStream();
            POIFSFileSystem fs = new POIFSFileSystem(is);
            try {
               extractor = new WordExtractor(fs);
            } catch(OldWordFileFormatException e) {
               extractor = new Word6Extractor(fs);
            }
            text = extractor.getText();
        }
        catch (IOException e)
        {
            // check if this is an error caused by the fact that the .doc is in fact
            // one of Word's temp non-documents
            if (e.getMessage().contains("Unable to read entire header"))
            {
                // just assign an empty string
                text = "";
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        // dump the text out.  This will close the writer automatically.
        writer.putContent(text);
    }
}
