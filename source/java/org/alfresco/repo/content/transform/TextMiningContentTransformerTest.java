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
import java.io.InputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.alfresco.repo.content.transform.TextMiningContentTransformer
 * 
 * @author Derek Hulley
 */
public class TextMiningContentTransformerTest extends AbstractContentTransformerTest
{
    private static final Log logger = LogFactory.getLog(TextMiningContentTransformerTest.class);

    private ContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new TextMiningContentTransformer();
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        double reliability = 0.0;
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_WORD);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
    
    /**
     * Tests a specific failure in the library
     */
    public void testBugFixAR1() throws Exception
    {
        File tempFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_",
                ".doc");
        FileContentWriter writer = new FileContentWriter(tempFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_WORD);
        // get the test resource and write it (MS Word)
        InputStream is = getClass().getClassLoader().getResourceAsStream("farmers_markets_list_2003.doc");
        assertNotNull("Test resource not found: farmers_markets_list_2003.doc");
        writer.putContent(is);
        
        // get the source of the transformation
        ContentReader reader = writer.getReader(); 
        
        // make a new location of the transform output (plain text)
        tempFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_",
                ".txt");
        writer = new FileContentWriter(tempFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // transform it
        transformer.transform(reader, writer);
    }
}
