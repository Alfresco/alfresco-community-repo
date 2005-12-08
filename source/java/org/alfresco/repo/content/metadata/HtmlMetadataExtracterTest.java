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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.alfresco.repo.content.transform.OfficeMetadataExtracter
 * @author Jesper Steen Møller
 */
public class HtmlMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static final Log logger = LogFactory.getLog(HtmlMetadataExtracterTest.class);
    private MetadataExtracter extracter;

    public void onSetUpInTransaction() throws Exception
    {
        extracter = new HtmlMetadataExtracter();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testReliability() throws Exception
    {
        double reliability = 0.0;
        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype text should not be supported", 0.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_HTML);
        assertEquals("HTML should be supported", 1.0, reliability);
    }

    public void testHtmlExtraction() throws Exception
    {
        testCommonMetadata(extractFromExtension("html", MimetypeMap.MIMETYPE_HTML));
    }

}
