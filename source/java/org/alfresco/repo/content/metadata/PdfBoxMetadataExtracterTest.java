package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;

/**
 * @see org.alfresco.repo.content.metadata.PdfBoxMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private MetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new PdfBoxMetadataExtracter();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testReliability() throws Exception
    {
        double reliability = 0.0;
        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should not be supported", 0.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_PDF);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }

    public void testPdfExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_PDF);
    }
}
