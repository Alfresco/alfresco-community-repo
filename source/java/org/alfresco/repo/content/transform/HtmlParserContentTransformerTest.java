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

import org.alfresco.repo.content.MimetypeMap;

/**
 * @see org.alfresco.repo.content.transform.HtmlParserContentTransformer
 * 
 * @author Derek Hulley
 */
public class HtmlParserContentTransformerTest extends AbstractContentTransformerTest
{
    private static final String SOME_CONTENT = "azAz10!£$%^&*()\t\r\n";
    
    private ContentTransformer transformer;
    
    @Override
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new HtmlParserContentTransformer();
    }
    
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        assertNotNull(transformer);
    }
    
    public void checkReliability() throws Exception
    {
        // check reliability
        double reliability = transformer.getReliability(MimetypeMap.MIMETYPE_HTML, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Reliability incorrect", 1.0, reliability);   // plain text to plain text is 100%
        
        // check other way around
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_HTML);
        assertEquals("Reliability incorrect", 0.0, reliability);   // plain text to plain text is 0%
    }
}
