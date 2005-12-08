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
package org.alfresco.repo.content;

import java.util.Map;

import org.alfresco.util.BaseSpringTest;

/**
 * @see org.alfresco.repo.content.MimetypeMap
 * 
 * @author Derek Hulley
 */
public class MimetypeMapTest extends BaseSpringTest
{
    private MimetypeMap mimetypeMap;

    public void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }
    
    public void testExtensions() throws Exception
    {
        Map<String, String> extensionsByMimetype = mimetypeMap.getExtensionsByMimetype();
        Map<String, String> mimetypesByExtension = mimetypeMap.getMimetypesByExtension();
        
        // plain text
        assertEquals("txt", extensionsByMimetype.get("text/plain"));
        assertEquals("text/plain", mimetypesByExtension.get("txt"));
        assertEquals("text/plain", mimetypesByExtension.get("csv"));
        assertEquals("text/plain", mimetypesByExtension.get("java"));
        
        // JPEG
        assertEquals("jpg", extensionsByMimetype.get("image/jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpe"));
        
        // MS Word
        assertEquals("doc", extensionsByMimetype.get("application/msword"));
        assertEquals("application/msword", mimetypesByExtension.get("doc"));
    }
}
