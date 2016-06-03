package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://poi.apache.org/">Apache POI</a> to transform
 *  Outlook email msg files.
 * 
 * @author Nick Burch
 */
public class MailContentTransformer extends TikaPoweredContentTransformer
{
    public MailContentTransformer() {
       super(new String[] {
             MimetypeMap.MIMETYPE_OUTLOOK_MSG
       });
    }

    @Override
    protected Parser getParser() {
       return new OfficeParser();
    }
}
