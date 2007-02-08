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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;

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
    
    public void onSetUpInTransaction() throws Exception
    {
        ContentTransformer unoTransformer = (ContentTransformer) applicationContext.getBean("transformer.OpenOffice");
        ContentTransformer pdfBoxTransformer = (ContentTransformer) applicationContext.getBean("transformer.PdfBox");
        // make sure that they are working for this test
        if (unoTransformer.getReliability(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_PDF) == 0.0)
        {
            isAvailable = false;
            return;
        }
        else if (pdfBoxTransformer.getReliability(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_TEXT_PLAIN) == 0.0)
        {
            isAvailable = false;
            return;
        }
        else
        {
            isAvailable = true;
        }
        
        transformer = new ComplexContentTransformer();
        transformer.setMimetypeService(mimetypeMap);
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
        double reliability = 0.0;
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_PDF);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
}
