/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;

/**
 * Outlook format email meta-data extractor
 * 
 * @author Kevin Roast
 */
public class MailMetadataExtracter extends AbstractMetadataExtracter
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {
        "message/rfc822"};
    
    private static final String STREAM_PREFIX = "__substg1.0_";
    private static final int STREAM_PREFIX_LENGTH = STREAM_PREFIX.length();

    // the CC: email addresses
    private ThreadLocal<List<String>> receipientEmails = new ThreadLocal<List<String>>();
    
    public MailMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)), 1.0, 1000);
    }

    public void extractInternal(ContentReader reader, final Map<QName, Serializable> destination) throws Throwable
    {
        POIFSReaderListener readerListener = new POIFSReaderListener()
        {
            public void processPOIFSReaderEvent(final POIFSReaderEvent event)
            {
                try
                {
                    if (event.getName().startsWith(STREAM_PREFIX))
                    {
                        StreamHandler handler = new StreamHandler(event.getName(), event.getStream());
                        handler.process(destination);
                    }
                }
                catch (Exception ex)
                {
                    throw new ContentIOException("Property set stream: " + event.getPath() + event.getName(), ex);
                }
            }
        };
        
        InputStream is = null;
        try
        {
            this.receipientEmails.set(new ArrayList<String>());
            
            is = reader.getContentInputStream();
            POIFSReader poiFSReader = new POIFSReader();
            poiFSReader.registerListener(readerListener);
            
            try
            {
                poiFSReader.read(is);
            }
            catch (IOException err)
            {
                // probably not an Outlook format MSG - ignore for now
                logger.warn("Unable to extract meta-data from message: " + err.getMessage());
            }
            
            // store multi-value extracted property
            if (receipientEmails.get().size() != 0)
            {
                destination.put(ContentModel.PROP_ADDRESSEES, (Serializable)receipientEmails.get());
            }
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
    }
    
    private static String convertExchangeAddress(String email)
    {
        if (email.lastIndexOf("/CN=") == -1)
        {
            return email;
        }
        else
        {
            // found a full Exchange format To header
            return email.substring(email.lastIndexOf("/CN=") + 4);
        }
    }
    
    private static final String ENCODING_TEXT = "001E";
    private static final String ENCODING_BINARY = "0102";
    private static final String ENCODING_UNICODE = "001F";
    
    private static final String SUBSTG_MESSAGEBODY = "1000";
    private static final String SUBSTG_RECIPIENTEMAIL = "39FE";      // 7bit email address
    private static final String SUBSTG_RECIPIENTSEARCH = "300B";     // address 'search' variant
    private static final String SUBSTG_RECEIVEDEMAIL = "0076";
    private static final String SUBSTG_SENDEREMAIL = "0C1F";
    private static final String SUBSTG_DATE = "0047";
    private static final String SUBSTG_SUBJECT = "0037";
    
    /**
     * Class to handle stream types. Can process and extract specific streams.
     */
    private class StreamHandler
    {
        StreamHandler(String name, DocumentInputStream stream)
        {
            this.type = name.substring(STREAM_PREFIX_LENGTH, STREAM_PREFIX_LENGTH + 4);
            this.encoding = name.substring(STREAM_PREFIX_LENGTH + 4, STREAM_PREFIX_LENGTH + 8);
            this.stream = stream;
        }
        
        void process(final Map<QName, Serializable> destination)
            throws IOException
        {
            if (type.equals(SUBSTG_SENDEREMAIL))
            {
                destination.put(ContentModel.PROP_ORIGINATOR, convertExchangeAddress(extractText()));
            }
            else if (type.equals(SUBSTG_RECIPIENTEMAIL))
            {
                receipientEmails.get().add(convertExchangeAddress(extractText()));
            }
            else if (type.equals(SUBSTG_RECIPIENTSEARCH))
            {
                String email = extractText(ENCODING_TEXT);
                int smptIndex = email.indexOf("SMTP:");
                if (smptIndex != -1)
                {
                    /* also may be used for SUBSTG_RECIPIENTTRANSPORT = "5FF7"; 
                       with search for SMPT followed by a null char */
                    
                    // this is a secondary mechanism for encoding a receipient email address
                    // the 7 bit email address may not have been set by Outlook - so this is needed instead
                    // handle null character at end of string
                    int endIndex = email.length();
                    if (email.codePointAt(email.length() - 1) == 0)
                    {
                        endIndex--;
                    }
                    email = email.substring(smptIndex + 5, endIndex);
                    receipientEmails.get().add(email);
                }
            }
            else if (type.equals(SUBSTG_RECEIVEDEMAIL))
            {
                destination.put(ContentModel.PROP_ADDRESSEE, convertExchangeAddress(extractText()));
            }
            else if (type.equals(SUBSTG_SUBJECT))
            {
                destination.put(ContentModel.PROP_SUBJECT, extractText());
            }
            else if (type.equals(SUBSTG_DATE))
            {
                // the date is not "really" plain text - but it's appropriate to parse as such
                String date = extractText(ENCODING_TEXT);
                int valueIndex = date.indexOf("l=");
                if (valueIndex != -1)
                {
                    int dateIndex = date.indexOf('-', valueIndex);
                    if (dateIndex != -1)
                    {
                        dateIndex++;
                        String strYear = date.substring(dateIndex, dateIndex + 2);
                        int year = Integer.parseInt(strYear) + (2000 - 1900);
                        String strMonth = date.substring(dateIndex + 2, dateIndex + 4);
                        int month = Integer.parseInt(strMonth) - 1;
                        String strDay = date.substring(dateIndex + 4, dateIndex + 6);
                        int day = Integer.parseInt(strDay);
                        String strHour = date.substring(dateIndex + 6, dateIndex + 8);
                        int hour = Integer.parseInt(strHour);
                        String strMinute = date.substring(dateIndex + 10, dateIndex + 12);
                        int minute = Integer.parseInt(strMinute);
                        destination.put(ContentModel.PROP_SENTDATE, new Date(year, month, day, hour, minute));
                    }
                }
            }
        }
        
        /**
         * Extract the text from the stream based on the encoding
         * 
         * @return String
         * 
         * @throws IOException
         */
        private String extractText()
            throws IOException
        {
            return extractText(this.encoding);
        }
        
        /**
         * Extract the text from the stream based on the encoding
         * 
         * @return String
         * 
         * @throws IOException
         */
        private String extractText(String encoding)
            throws IOException
        {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            
            if (encoding.equals(ENCODING_TEXT) || encoding.equals(ENCODING_BINARY))
            {
                return new String(data);
            }
            else if (encoding.equals(ENCODING_UNICODE))
            {
                // convert double-byte encoding to single byte for String conversion
                byte[] b = new byte[data.length >> 1];
                for (int i=0; i<b.length; i++)
                {
                    b[i] = data[i << 1];
                }
                return new String(b);
            }
            else
            {
                return new String(data);
            }
        }
        
        private String type;
        private String encoding;
        private DocumentInputStream stream;
    }
}
