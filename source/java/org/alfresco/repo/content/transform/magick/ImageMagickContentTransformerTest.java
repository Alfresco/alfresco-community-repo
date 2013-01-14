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
package org.alfresco.repo.content.transform.magick;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ProxyContentTransformer;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.magick.JMagickContentTransformer
 * 
 * @author Derek Hulley
 */
public class ImageMagickContentTransformerTest extends AbstractContentTransformerTest
{
    private ProxyContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // Setup a mimetype service that will return a truncated set of mimetypes
        MockMimetypeService testMimetypeService = new MockMimetypeService();
        testMimetypeService.setConfigService(((MimetypeMap) mimetypeService).getConfigService());
        testMimetypeService.setContentCharsetFinder(((MimetypeMap) mimetypeService).getContentCharsetFinder());
        testMimetypeService.init();
        this.mimetypeService = testMimetypeService;
        
        transformer = (ProxyContentTransformer) ctx.getBean("transformer.ImageMagick");
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
        if (!this.transformer.getWorker().isAvailable())
        {
            return;
        }
        boolean reliability = transformer.isTransformable(
                MimetypeMap.MIMETYPE_IMAGE_GIF, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(
                MimetypeMap.MIMETYPE_IMAGE_GIF, -1, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
    
    /**
     * Mock mimetype service which returns a limited set of mimetypes
     * as {@link AbstractContentTransformerTest#testAllConversions()} will
     * fail if delegates are not available on the test agent. 
     * 
     * @author rgauss
     */
    public class MockMimetypeService extends MimetypeMap
    {
        private List<String> testMimetypes;
        
        public void init()
        {
            super.init();
            testMimetypes = new ArrayList<String>(10);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_GIF);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_JPEG);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_PNG);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_TIFF);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_EPS);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_PHOTOSHOP);
            testMimetypes.add(MimetypeMap.MIMETYPE_PDF);
        }

        @Override
        public List<String> getMimetypes()
        {
            return testMimetypes;
        }
        
    }
}
