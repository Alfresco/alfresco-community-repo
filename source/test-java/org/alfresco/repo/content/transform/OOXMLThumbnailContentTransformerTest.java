package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Test case for {@link OOXMLThumbnailContentTransformer} content transformer.
 * 
 * @author Nick Burch
 * @since 4.0.1
 */
public class OOXMLThumbnailContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new OOXMLThumbnailContentTransformer();
        
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
        // Does support Thumbnails
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        
        // Unlike iWorks, it doesn't handle PDF previews
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
    }
}
