package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.alfresco.repo.content.transform.PdfBoxContentTransformer
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static final Log logger = LogFactory.getLog(PdfBoxMetadataExtracterTest.class);
    private MetadataExtracter extracter;

    public void onSetUpInTransaction() throws Exception
    {
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
        testCommonMetadata(extractFromExtension("pdf", MimetypeMap.MIMETYPE_PDF));
    }
}
