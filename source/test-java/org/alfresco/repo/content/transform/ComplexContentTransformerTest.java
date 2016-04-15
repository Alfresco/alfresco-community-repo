/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Tests a transformation from Powerpoint->PDF->Text.
 * 
 * @see org.alfresco.repo.content.transform.ComplexContentTransformer
 * 
 * @author Derek Hulley
 */
public class ComplexContentTransformerTest extends AbstractContentTransformerTest
{
    private ComplexContentTransformer transformer;
    private boolean isAvailable;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        ContentTransformer unoTransformer = (ContentTransformer) ctx.getBean("transformer.OpenOffice");
        ContentTransformer pdfBoxTransformer = (ContentTransformer) ctx.getBean("transformer.PdfBox");
        // make sure that they are working for this test
        if (unoTransformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()) == false)
        {
            isAvailable = false;
            return;
        }
        else if (pdfBoxTransformer.isTransformable(MimetypeMap.MIMETYPE_PDF, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()) == false)
        {
            isAvailable = false;
            return;
        }
        else
        {
            isAvailable = true;
        }
        
        transformer = new ComplexContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        // set the transformer list
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(2);
        transformers.add(unoTransformer);
        transformers.add(pdfBoxTransformer);
        transformer.setTransformers(transformers);
        // set the intermediate mimetypes
        List<String> intermediateMimetypes = Collections.singletonList(MimetypeMap.MIMETYPE_PDF);
        transformer.setIntermediateMimetypes(intermediateMimetypes);
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
        if (!isAvailable)
        {
            return;
        }
        boolean reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
}
