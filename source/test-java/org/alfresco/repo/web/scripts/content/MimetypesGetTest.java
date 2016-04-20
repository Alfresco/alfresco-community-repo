package org.alfresco.repo.web.scripts.content;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.content.transform.PdfBoxContentTransformer;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.context.ApplicationContext;

/**
 * Tests the {@link MimetypesGet} endpoint
 */
public class MimetypesGetTest extends BaseWebScriptTest
{
    
    private ApplicationContext ctx;
    private ContentTransformerRegistry contentTransformerRegistry;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        contentTransformerRegistry = (ContentTransformerRegistry) ctx.getBean("contentTransformerRegistry");
    }
    
    /**
     * Tests the <code>mimetypesGet.getTransformer</code> method directly for
     * varefication of label text
     * 
     * @throws Exception
     */
    public void testGetTransformer() throws Exception
    {
        MimetypesGet mimetypesGet = new MimetypesGet();
        mimetypesGet.setApplicationContext(ctx);
        mimetypesGet.setContentTransformerRegistry(contentTransformerRegistry);
        mimetypesGet.afterPropertiesSet();
        
        // Test a Java transformer name
        String transformerName = mimetypesGet.getTransformer(MimetypeMap.MIMETYPE_PDF, 1000, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals(PdfBoxContentTransformer.class.getCanonicalName(), transformerName);
        
        // Test a generic proxy transformer name
        transformerName = mimetypesGet.getTransformer(MimetypeMap.MIMETYPE_IMAGE_JPEG, 1000, MimetypeMap.MIMETYPE_IMAGE_PNG);
        assertNotNull(transformerName);
        assertTrue("Expected transformerName to contain 'Proxy' but was " + transformerName,
                transformerName.contains("Proxy via"));
        
        boolean oodirectPresent = ctx.containsBean(MimetypesGet.OODIRECT_WORKER_BEAN);
        boolean jodPresent = ctx.containsBean(MimetypesGet.JOD_WORKER_BEAN);
        
        // Test the office transformer name
        transformerName = mimetypesGet.getTransformer(MimetypeMap.MIMETYPE_WORD, 1000, MimetypeMap.MIMETYPE_PDF);
        assertNotNull(transformerName);
        if (oodirectPresent)
        {
            assertEquals("Using a Direct Open Office Connection", transformerName);
        }
        else if (jodPresent)
        {
            assertEquals("Using JOD Converter / Open Office", transformerName);
        }
    }

}
