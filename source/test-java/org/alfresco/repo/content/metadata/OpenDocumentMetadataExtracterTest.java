package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;


/**
 * @see OpenDocumentMetadataExtracter
 * 
 * @author Derek Hulley
 */
public class OpenDocumentMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OpenDocumentMetadataExtracter extracter;
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new OpenDocumentMetadataExtracter();
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
        for (String mimetype : OpenDocumentMetadataExtracter.SUPPORTED_MIMETYPES)
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
        for (String mimetype : OpenDocumentMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }
    
    @Override
    protected boolean skipAuthorCheck(String mimetype) { return true; }

   /**
    * We also provide the creation date - check that
    */
   protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) 
   {
       try
       {
          // Check for two cases
          if(mimetype.equals("application/vnd.oasis.opendocument.text")) 
          {
             assertEquals(
                   "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
                   DATE_FORMAT.parse("2005-09-06T23:34:00.000+0000"),
                   DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED)));
          } 
          else if(mimetype.equals("application/vnd.oasis.opendocument.graphics")) 
          {
             assertEquals(
                   "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
                   DATE_FORMAT.parse("2006-01-27T11:46:11.000+0000"),
                   DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED)));
          }
       }
       catch (ParseException e)
       {
           fail(e.getMessage());
       }
   }
    
}
