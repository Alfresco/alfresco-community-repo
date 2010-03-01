/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.BinaryPassThroughContentTransformer
 * 
 * @author Derek Hulley
 */
public class BinaryPassThroughContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new BinaryPassThroughContentTransformer();
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        TransformationOptions options = new TransformationOptions();
        boolean reliability = false;
        
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_TEXT_PLAIN, options);
        assertFalse("Mimetype should not be supported", reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_XML, MimetypeMap.MIMETYPE_XML, options);
        assertFalse("Mimetype should not be supported", reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_WORD, options);
        assertTrue("Mimetype should be supported", reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_EXCEL, options);
        assertTrue("Mimetype should be supported", reliability);
    }
}
