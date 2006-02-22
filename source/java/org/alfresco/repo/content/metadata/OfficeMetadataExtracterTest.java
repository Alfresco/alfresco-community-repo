package org.alfresco.repo.content.metadata;


/**
 * @see org.alfresco.repo.content.transform.OfficeMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class OfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private MetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
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
        for (String mimetype : OfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            double reliability = extracter.getReliability(mimetype);
            assertTrue("Expected above zero reliability", reliability > 0.0);
        }
    }

    /**
     * Test all the supported mimetypes
     */
    public void testSupportedMimetypes() throws Exception
    {
        for (String mimetype : OfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }
}
