package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * This badly named transformer turns Microsoft Word documents
 *  (Word 6, 95, 97, 2000, 2003) into plain text, using Apache Tika.
 * 
 * @author Nick Burch
 */
public class TextMiningContentTransformer extends TikaPoweredContentTransformer
{
    public TextMiningContentTransformer()
    {       
        super(new String[] {
            MimetypeMap.MIMETYPE_WORD
        });
    }

    @Override
    protected Parser getParser() {
        return new OfficeParser();
    }
}
