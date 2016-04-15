package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TIFF;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;

/**
 * A Metadata Extractor which makes use of the Apache
 *  Tika auto-detection to select the best parser
 *  to extract the metadata from your document.
 * This will be used for all files which Tika can
 *  handle, but where no other more explicit
 *  extractor is defined. 

 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>comments:</b>
 *   <p>geo:lat:</b>                --      cm:latitude
 *   <p>geo:long:</b>               --      cm:longitude
 * </pre>
 * 
 * @since 3.4
 * @author Nick Burch
 */
public class TikaAutoMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(TikaAutoMetadataExtracter.class);
    private static AutoDetectParser parser;
    private static TikaConfig config;
    private static String EXIF_IMAGE_HEIGHT_TAG = "Exif Image Height";
    private static String EXIF_IMAGE_WIDTH_TAG = "Exif Image Width";
    private static String JPEG_IMAGE_HEIGHT_TAG = "Image Height";
    private static String JPEG_IMAGE_WIDTH_TAG = "Image Width";

    public static ArrayList<String> SUPPORTED_MIMETYPES;
    private static ArrayList<String> buildMimeTypes(TikaConfig tikaConfig)
    {
       config = tikaConfig;
       parser = new AutoDetectParser(config);

       SUPPORTED_MIMETYPES = new ArrayList<String>();
       for(MediaType mt : parser.getParsers().keySet()) 
       {
          // Add the canonical mime type
          SUPPORTED_MIMETYPES.add( mt.toString() );
          
          // And add any aliases of the mime type too - Alfresco uses some
          //  non canonical forms of various mimetypes, so we need all of them
          for(MediaType alias : config.getMediaTypeRegistry().getAliases(mt)) 
          {
              SUPPORTED_MIMETYPES.add( alias.toString() );
          }
       }
       return SUPPORTED_MIMETYPES;
    }
    
    public TikaAutoMetadataExtracter(TikaConfig tikaConfig)
    {
       super( buildMimeTypes(tikaConfig) );
    }
    
    /**
     * Does auto-detection to select the best Tika
     *  Parser.
     */
    @Override
    protected Parser getParser() 
    {
       return parser;
    }
    
    /**
     * Because some editors use JPEG_IMAGE_HEIGHT_TAG when
     * saving JPEG images , a more reliable source for
     * image size are the values provided by Tika
     * and not the exif/tiff metadata read from the file
     * This will override the tiff:Image size 
     * which gets embedded into the alfresco node properties
     * for jpeg files that contain such exif information
     */
    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) 
    {
        
        if(MimetypeMap.MIMETYPE_IMAGE_JPEG.equals(metadata.get(Metadata.CONTENT_TYPE)))                
        {
            //check if the image has exif information
            if(metadata.get(EXIF_IMAGE_WIDTH_TAG) != null && metadata.get(EXIF_IMAGE_HEIGHT_TAG) != null )
            {    
                //replace the exif size properties that will be embedded in the node with
                //the guessed dimensions from Tika
                putRawValue(TIFF.IMAGE_LENGTH.getName(), extractSize(metadata.get(JPEG_IMAGE_HEIGHT_TAG)), properties);
                putRawValue(TIFF.IMAGE_WIDTH.getName(), extractSize(metadata.get(JPEG_IMAGE_WIDTH_TAG)), properties);
            }
        }
        return properties;
    }
    
}
