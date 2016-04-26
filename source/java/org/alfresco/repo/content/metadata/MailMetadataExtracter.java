package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * Outlook MAPI format email meta-data extractor extracting the following values:
 * <pre>
 *   <b>sentDate:</b>               --      cm:sentdate
 *   <b>originator:</b>             --      cm:originator,    cm:author
 *   <b>addressee:</b>              --      cm:addressee
 *   <b>addressees:</b>             --      cm:addressees
 *   <b>subjectLine:</b>            --      cm:subjectline,   cm:description
 *   <b>toNames:</b>                --
 *   <b>ccNames:</b>                --
 *   <b>bccNames:</b>               --
 * </pre>
 * 
 * TIKA note - to/cc/bcc go into the html part, not the metadata.
 *  Also, email addresses not included as yet.
 * 
 * @since 2.1
 * @author Kevin Roast
 */
public class MailMetadataExtracter extends TikaPoweredMetadataExtracter
{
    private static final String KEY_SENT_DATE = "sentDate";
    private static final String KEY_ORIGINATOR = "originator";
    private static final String KEY_ADDRESSEE = "addressee";
    private static final String KEY_ADDRESSEES = "addressees";
    private static final String KEY_SUBJECT = "subjectLine";
    private static final String KEY_TO_NAMES = "toNames";
    private static final String KEY_CC_NAMES = "ccNames";
    private static final String KEY_BCC_NAMES = "bccNames";

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes( 
          new String[] {MimetypeMap.MIMETYPE_OUTLOOK_MSG},
          (Parser[])null
    );
    
    public MailMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() 
    {
       // The office parser does Outlook as well as Word, Excel etc
       return new OfficeParser();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) 
    {
       putRawValue(KEY_ORIGINATOR, metadata.get(Metadata.AUTHOR), properties);
       putRawValue(KEY_SUBJECT, metadata.get(Metadata.TITLE), properties);
       putRawValue(KEY_DESCRIPTION, metadata.get(Metadata.SUBJECT), properties);
       putRawValue(KEY_SENT_DATE, metadata.get(Metadata.LAST_SAVED), properties);
       
       // Store the TO, but not cc/bcc in the addressee field
       putRawValue(KEY_ADDRESSEE, metadata.get(Metadata.MESSAGE_TO), properties); 
       
       // Store each of To, CC and BCC in their own fields
       putRawValue(KEY_TO_NAMES, metadata.getValues(Metadata.MESSAGE_TO), properties);
       putRawValue(KEY_CC_NAMES, metadata.getValues(Metadata.MESSAGE_CC), properties);
       putRawValue(KEY_BCC_NAMES, metadata.getValues(Metadata.MESSAGE_BCC), properties);
       
       // But store all email addresses (to/cc/bcc) in the addresses field
       putRawValue(KEY_ADDRESSEES, metadata.getValues(Metadata.MESSAGE_RECIPIENT_ADDRESS), properties); 
       
       return properties;
    }
}
