/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.Header;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.MimeMessage.RecipientType;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * Metadata extractor for the PDF documents.
 * <pre>
 *   <b>messageFrom:</b>              --      imap:messageFrom
 *   <b>messageTo:</b>                --      imap:messageTo
 *   <b>messageCc:</b>                --      imap:messageCc
 *   <b>messageSubject:</b>           --      imap:messageSubject, cm:title, cm:description
 *   <b>messageSent:</b>              --      imap:dateSent
 *   <b>All <code>{@link Header#getName() header names}:</b>
 *      <b>Thread-Index:</b>          --      imap:threadIndex
 *      <b>Message-ID:</b>            --      imap:messageId
 *      <b>date:</b>                  --      imap:dateReceived
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class RFC822MetadataExtracter extends AbstractMappingMetadataExtracter
{

    private static final String KEY_MESSAGE_FROM = "messageFrom";
    private static final String KEY_MESSAGE_TO = "messageTo";
    private static final String KEY_MESSAGE_CC = "messageCc";
    private static final String KEY_MESSAGE_SUBJECT = "messageSubject";
    private static final String KEY_MESSAGE_SENT = "messageSent";

    public static String[] SUPPORTED_MIMETYPES = new String[] { MimetypeMap.MIMETYPE_RFC822 };

    public RFC822MetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();

        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            MimeMessage mimeMessage = new MimeMessage(null, is);

            if (mimeMessage != null)
            {
                //Extract values that doesn't match to headers and need to be encoded.
                putRawValue(KEY_MESSAGE_FROM, InternetAddress.toString(mimeMessage.getFrom()), rawProperties);
                putRawValue(KEY_MESSAGE_TO, InternetAddress.toString(mimeMessage.getRecipients(RecipientType.TO)), rawProperties);
                putRawValue(KEY_MESSAGE_CC, InternetAddress.toString(mimeMessage.getRecipients(RecipientType.CC)), rawProperties);
                putRawValue(KEY_MESSAGE_SENT, mimeMessage.getSentDate(), rawProperties); 

                String[] subj = mimeMessage.getHeader("Subject");
                if (subj != null && subj.length > 0)
                {
                    String decodedSubject = subj[0];
                    try
                    {
                        decodedSubject = MimeUtility.decodeText(decodedSubject);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        logger.warn(e.toString());
                    }
                    putRawValue(KEY_MESSAGE_SUBJECT, decodedSubject, rawProperties);
                }
                
                //Extract values from headers
                Set<String> keys = getMapping().keySet();
                Enumeration<Header> headers = mimeMessage.getAllHeaders();
                while (headers.hasMoreElements())
                {
                    Header header = (Header) headers.nextElement();
                    if (keys.contains(header.getName()))
                    {
                        header.getValue();
                        putRawValue(header.getName(), header.getValue(), rawProperties);
                    }
                }
                
            }
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        // Done
        return rawProperties;
    }
    
   /**
     * Back door for RM
     * @return
     */
    public final Map<String, Set<QName>> getCurrentMapping()
    {
         return super.getMapping();
    }
    
//    /**
//     * Back door for RM
//     * @return
//     */
//    public void setMapping(Map<String, Set<QName>> mapping)
//    {
//        super.setMapping(mapping);
//    }


}
