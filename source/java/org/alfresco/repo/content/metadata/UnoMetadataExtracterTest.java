/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;

/**
 * @see org.alfresco.repo.content.transform.UnoMetadataExtracter
 * @author Jesper Steen Møller
 */
public class UnoMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private UnoMetadataExtracter extracter;

    public void onSetUpInTransaction() throws Exception
    {
        extracter = new UnoMetadataExtracter(mimetypeMap);
    }

    /**
     * @return Returns the same extracter regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testReliability() throws Exception
    {
        if (!extracter.isConnected())
        {
            return;
        }
        
        double reliability = 0.0;
        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype text should not be supported", 0.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT);
        assertEquals("OpenOffice 2.0 Writer (OpenDoc) should be supported", 1.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER);
        assertEquals("OpenOffice 1.0 Writer should be supported", 1.0, reliability);
    }

    public void testOOo20WriterExtraction() throws Exception
    {
        if (!extracter.isConnected())
        {
            return;
        }
        
        testCommonMetadata(extractFromExtension("odt", MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT));
    }

    public void testOOo10WriterExtraction() throws Exception
    {
        if (!extracter.isConnected())
        {
            return;
        }
        
        testCommonMetadata(extractFromExtension("sxw", MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER));
    }
}
