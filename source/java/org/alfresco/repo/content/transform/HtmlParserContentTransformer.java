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
