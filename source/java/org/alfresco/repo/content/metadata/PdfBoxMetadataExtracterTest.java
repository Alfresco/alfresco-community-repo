package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;

/**
 * @see org.alfresco.repo.content.metadata.PdfBoxMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private PdfBoxMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new PdfBoxMetadataExtracter();
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : PdfBoxMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testPdfExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_PDF);
    }
}
