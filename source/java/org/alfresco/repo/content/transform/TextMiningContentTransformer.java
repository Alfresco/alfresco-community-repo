/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.textmining.text.extraction.WordExtractor;

/**
 * Makes use of the {@link http://www.textmining.org/ TextMining} library to
 * perform conversions from MSWord documents to text.
 * 
 * @author Derek Hulley
 */
public class TextMiningContentTransformer extends AbstractContentTransformer
{
    private WordExtractor wordExtractor;
    
    public TextMiningContentTransformer()
    {
        this.wordExtractor = new WordExtractor();
    }
    
    /**
     * Currently the only transformation performed is that of text extraction from Word documents.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!MimetypeMap.MIMETYPE_WORD.equals(sourceMimetype) ||
                !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support DOC -> Text
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
        InputStream is = null;
        String text = null;
        try
        {
            is = reader.getContentInputStream();
            text = wordExtractor.extractText(is);
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
