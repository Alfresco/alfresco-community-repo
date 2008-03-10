package org.alfresco.repo.content.metadata;


/**
 * @see OfficeMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class OfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OfficeMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new OfficeMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
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
        for (String mimetype : OfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
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
