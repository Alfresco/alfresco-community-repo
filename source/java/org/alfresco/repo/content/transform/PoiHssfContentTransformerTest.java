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
 * @see org.alfresco.repo.content.transform.PoiHssfContentTransformer
 * 
 * @author Derek Hulley
 */
public class PoiHssfContentTransformerTest extends AbstractContentTransformerTest
{
    private static final Log logger = LogFactory.getLog(PoiHssfContentTransformerTest.class);

    private ContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new PoiHssfContentTransformer();
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
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_EXCEL);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
    
    /**
     * Tests a specific failure in the library
     */
    public void xtestBugFixAR114() throws Exception
    {
        File tempFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_",
                ".xls");
        FileContentWriter writer = new FileContentWriter(tempFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
        // get the test resource and write it (Excel)
        InputStream is = getClass().getClassLoader().getResourceAsStream("Plan270904b.xls");
        assertNotNull("Test resource not found: Plan270904b.xls");
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
