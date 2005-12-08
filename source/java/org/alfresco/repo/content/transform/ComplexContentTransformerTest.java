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
