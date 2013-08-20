/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.PoiOOXMLContentTransformer
 * 
 * @author Nick Burch
 */
public class PoiOOXMLContentTransformerTest extends AbstractContentTransformerTest
{
    private PoiOOXMLContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PoiOOXMLContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
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
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
    }
}
