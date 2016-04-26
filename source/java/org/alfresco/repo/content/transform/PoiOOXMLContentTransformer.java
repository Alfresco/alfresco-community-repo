package org.alfresco.repo.content.transform;

import java.util.ArrayList;

import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://poi.apache.org/">Apache POI</a> to perform
 *  conversions from the newer OOXML Office documents.
 *
 * @author Nick Burch
 */
public class PoiOOXMLContentTransformer extends TikaPoweredContentTransformer
{
   /** 
    * We support all the office mimetypes that the Tika
    *  office parser can handle
    */
   public static ArrayList<String> SUPPORTED_MIMETYPES;
   static {
      SUPPORTED_MIMETYPES = new ArrayList<String>();
      Parser p = new OOXMLParser();
      for(MediaType mt : p.getSupportedTypes(null)) {
         SUPPORTED_MIMETYPES.add( mt.toString() );
      }
   }
    
    public PoiOOXMLContentTransformer() {
       super(SUPPORTED_MIMETYPES);
       setUseTimeoutThread(true);
    }

    @Override
    protected Parser getParser() {
       return new OOXMLParser();
    }
}
