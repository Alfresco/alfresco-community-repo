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

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.NamespaceService;
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
    
    private static final String SUBSTG_MESSAGEBODY = "__substg1.0_1000001E";
    private static final String SUBSTG_RECIPIENTEMAIL = "__substg1.0_39FE001E";
    private static final String SUBSTG_RECEIVEDEMAIL = "__substg1.0_0076001E";
    private static final String SUBSTG_SENDEREMAIL = "__substg1.0_0C1F001E";
    private static final String SUBSTG_DATE = "__substg1.0_00470102";
    
    private static final QName ASPECT_MAILED = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "emailed");
    private static final QName PROP_SENTDATE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "sentdate");
    private static final QName PROP_ORIGINATOR = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "originator");
    private static final QName PROP_ADDRESSEE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressee");
    private static final QName PROP_ADDRESSEES = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "addressees");

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
                    String name = event.getName();
                    
                    if (name.equals(SUBSTG_RECIPIENTEMAIL))         // a recipient email address
                    {
                        String emailAddress = readPlainTextStream(event.getStream());
                        receipientEmails.get().add(convertExchangeAddress(emailAddress));
                    }
                    else if (name.equals(SUBSTG_RECEIVEDEMAIL))     // receiver email address
                    {
                        String emailAddress = readPlainTextStream(event.getStream());
                        destination.put(PROP_ADDRESSEE, convertExchangeAddress(emailAddress));
                    }
                    else if (name.equals(SUBSTG_SENDEREMAIL))       // sender email - NOTE either email OR full Exchange data e.g. : /O=HOSTEDSERVICE2/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=MIKE.FARMAN@BEN
                    {
                        String emailAddress = readPlainTextStream(event.getStream());
                        destination.put(PROP_ORIGINATOR, convertExchangeAddress(emailAddress));
                    }
                    else if (name.equals(SUBSTG_DATE))
                    {
                        // the date is not really plain text - but it's easier to parse as such
                        String date = readPlainTextStream(event.getStream());
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
                                destination.put(PROP_SENTDATE, new Date(year, month, day, hour, minute));
                            }
                        }
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
                destination.put(PROP_ADDRESSEES, (Serializable)receipientEmails.get());
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
    
    private static String readPlainTextStream(DocumentInputStream stream)
        throws IOException
    {
        byte[] data = new byte[stream.available()];
        int read = stream.read(data);
        return new String(data);
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
}
