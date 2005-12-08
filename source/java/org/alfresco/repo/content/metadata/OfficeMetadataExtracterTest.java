package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.alfresco.repo.content.transform.OfficeMetadataExtracter
 * @author Jesper Steen Møller
 */
public class OfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static final Log logger = LogFactory.getLog(OfficeMetadataExtracterTest.class);
    private MetadataExtracter extracter;

    public void onSetUpInTransaction() throws Exception
    {
        extracter = new OfficeMetadataExtracter();
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
        assertEquals("Mimetype text should not be supported", 0.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_WORD);
        assertEquals("Word should be supported", 1.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_EXCEL);
        assertEquals("Excel should be supported", 1.0, reliability);

        reliability = extracter.getReliability(MimetypeMap.MIMETYPE_PPT);
        assertEquals("PowerPoint should be supported", 1.0, reliability);
    }

    public void testWordExtraction() throws Exception
    {
        testCommonMetadata(extractFromExtension("doc", MimetypeMap.MIMETYPE_WORD));
    }

    public void testExcelExtraction() throws Exception
    {
        testCommonMetadata(extractFromExtension("xls", MimetypeMap.MIMETYPE_EXCEL));
    }

    public void testPowerPointExtraction() throws Exception
    {
        testCommonMetadata(extractFromExtension("ppt", MimetypeMap.MIMETYPE_PPT));
    }

}
