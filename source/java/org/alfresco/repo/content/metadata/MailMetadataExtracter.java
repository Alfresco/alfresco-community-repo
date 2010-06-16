/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.microsoft.OutlookExtractor; // TODO fix import

/**
 * Outlook MAPI format email meta-data extractor extracting the following values:
 * <pre>
 *   <b>sentDate:</b>               --      cm:sentdate
 *   <b>originator:</b>             --      cm:originator,    cm:author
 *   <b>addressee:</b>              --      cm:addressee
 *   <b>addressees:</b>             --      cm:addressees
 *   <b>subjectLine:</b>            --      cm:subjectline,   cm:description
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

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes( 
          new String[] {MimetypeMap.MIMETYPE_OUTLOOK_MSG},
          null
    );
    
    public MailMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() {
       //return new OutlookExtractor(); // TODO fix import
       return null;
    }
    
    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties) {
       // TODO move things from extractRaw to here
       return properties;
    }

    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        // TODO remove this in favour of extractSpecific
        final Map<String, Serializable> rawProperties = newRawMap();
        
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            MAPIMessage msg;
            
            try
            {
               msg  = new MAPIMessage(is);
               msg.setReturnNullOnMissingChunk(true);
               
               putRawValue(KEY_ORIGINATOR, msg.getDisplayFrom(), rawProperties);
               putRawValue(KEY_SUBJECT, msg.getSubject(), rawProperties);
               putRawValue(KEY_SENT_DATE, msg.getMessageDate().getTime(), rawProperties);
               
               // Store the TO, but not cc/bcc in the addressee field
               putRawValue(KEY_ADDRESSEE, msg.getDisplayTo(), rawProperties);
               // But store all email addresses (to/cc/bcc) in the addresses field
               putRawValue(KEY_ADDRESSEES, msg.getRecipientEmailAddressList(), rawProperties);
            }
            catch (IOException err)
            {
                // probably not an Outlook format MSG - ignore for now
                if (logger.isWarnEnabled())
                    logger.warn("Unable to extract meta-data from message: " + err.getMessage());
            }
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
        // Done
        return rawProperties;
    }
}
