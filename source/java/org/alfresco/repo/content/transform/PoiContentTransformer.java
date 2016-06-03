package org.alfresco.repo.content.transform;

import java.util.ArrayList;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://poi.apache.org/">Apache POI</a> to perform
 *  conversions from Office documents.
 *
 * {@link PoiHssfContentTransformer} handles the Excel
 *  transformations (mostly for compatibility), while
 *  this does all the other Office file formats.
 * 
 * @author Nick Burch
 */
public class PoiContentTransformer extends TikaPoweredContentTransformer
{
   /** 
    * We support all the office mimetypes that the Tika
    *  office parser can handle, except for excel
    *  (handled by {@link PoiHssfContentTransformer}
    */
   public static ArrayList<String> SUPPORTED_MIMETYPES;
   static {
      SUPPORTED_MIMETYPES = new ArrayList<String>();
      Parser p = new OfficeParser();
      for(MediaType mt : p.getSupportedTypes(null)) {
         if(mt.toString().equals(MimetypeMap.MIMETYPE_EXCEL))
         {
            // Skip, handled elsewhere
            continue;
         }
         // Tika can probably do some useful text
         SUPPORTED_MIMETYPES.add( mt.toString() );
      }
   }
    
    public PoiContentTransformer() {
       super(SUPPORTED_MIMETYPES);
    }

    @Override
    protected Parser getParser() {
       return new OfficeParser();
    }
}
