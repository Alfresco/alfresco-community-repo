/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

import java.util.Arrays;
import java.util.List;

/**
 * Test case for {@link AppleIWorksContentTransformer} content transformer.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class AppleIWorksContentTransformerTest extends AbstractContentTransformerTest
{
    private AppleIWorksContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new AppleIWorksContentTransformer();
        
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
    }
    
    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

// Commented out rather than removed, in case we can get SHARE to fall back to using JPEG when a PDF is not available
//    @Override
//    protected String[] getQuickFilenames(String sourceMimetype)
//    {
//        List<String> filenames = Arrays.asList(super.getQuickFilenames(sourceMimetype));
//        filenames.add("quick2009.pages");
//        return (String[])filenames.toArray();
//    }
//
//    @Override
//    protected boolean doTestTransformation(String quickFile, String sourceMimetype, String targetMimetype)
//    {
//        // This transformer can only do transforms to PDF when a iWorks 2008/9 file (rather than 2013/14) file is used.
//        return !MimetypeMap.MIMETYPE_PDF.equals(targetMimetype) || !"quick2009.pages".endsWith(quickFile);
//    }
//
//    @Override
//    protected boolean isTransformationExcluded(String sourceExtension, String targetExtension)
//    {
//    	return "pdf".equals(targetExtension); // Our quick files are 2013/14 format so don't include a pdf, only jpgs.
//    }

    public void testIsTransformable() throws Exception
    {
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_NUMBERS, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_PAGES, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));

// Commented out rather than removed, in case we can get SHARE to fall back to using JPEG when a PDF is not available
//        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
//        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_NUMBERS, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
//        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_IWORK_PAGES, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
    }
}
