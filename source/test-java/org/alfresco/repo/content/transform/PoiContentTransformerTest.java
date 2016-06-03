package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.PoiContentTransformer
 * 
 * @author Nick Burch
 */
public class PoiContentTransformerTest extends AbstractContentTransformerTest
{
    private PoiContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PoiContentTransformer();
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
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_WORD, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_WORD, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_WORD, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_WORD, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_PPT, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PPT, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OUTLOOK_MSG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OUTLOOK_MSG, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OUTLOOK_MSG, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OUTLOOK_MSG, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        // Doesn't claim excel
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
    }
}
