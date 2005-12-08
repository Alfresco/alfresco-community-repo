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
 * @see org.alfresco.repo.content.transform.UnoContentTransformer
 * 
 * @author Derek Hulley
 */
public class UnoContentTransformerTest extends AbstractContentTransformerTest
{
    private static String MIMETYPE_RUBBISH = "text/rubbish";
    
    private UnoContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new UnoContentTransformer();
        transformer.setMimetypeService(mimetypeMap);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        super.testSetUp();
        assertNotNull(mimetypeMap);
    }
    
    public void testReliability() throws Exception
    {
        if (!transformer.isConnected())
        {
            // no connection
            return;
        }
        double reliability = 0.0;
        reliability = transformer.getReliability(MIMETYPE_RUBBISH, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MIMETYPE_RUBBISH);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_WORD);
        assertEquals("Mimetype should be supported", 1.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 0.8, reliability);
    }
}
