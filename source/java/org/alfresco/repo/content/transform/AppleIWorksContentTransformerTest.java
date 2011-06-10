/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * Test case for {@link AppleIWorksContentTransformer} content transformer.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class AppleIWorksContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new AppleIWorksContentTransformer();
        
        // Ugly cast just to set the MimetypeService
        ((ContentTransformerHelper)transformer).setMimetypeService(mimetypeService);
    }
    
    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        // thumbnails
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_NUMBERS, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_PAGES, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        
        // previews
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_NUMBERS, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_PAGES, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
    }
}
