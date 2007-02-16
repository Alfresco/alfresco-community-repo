/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.alfresco.repo.content.transform.PdfBoxContentTransformer
 * 
 * @author Derek Hulley
 */
public class PdfBoxContentTransformerTest extends AbstractContentTransformerTest
{
    private static final Log logger = LogFactory.getLog(PdfBoxContentTransformerTest.class);

    private ContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new PdfBoxContentTransformer();
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
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_PDF);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
}
