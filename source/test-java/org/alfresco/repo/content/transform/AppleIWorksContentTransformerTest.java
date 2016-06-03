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
        ((ContentTransformerHelper)transformer).setTransformerConfig(transformerConfig);
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
