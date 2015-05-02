/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.content.transform;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;


/**
 * Uses javax.mail.MimeMessage to generate plain text versions of RFC822 email
 * messages. Searches for all text content parts, and returns them. Any
 * attachments are ignored. TIKA Note - could be replaced with the Tika email
 * parser. Would require a recursing parser to be specified, but not the full
 * Auto one (we don't want attachments), just one containing text and html
 * related parsers.
 */
public class EMLTransformer extends AbstractContentTransformer2

{
    private static final String CHARSET = "charset";
    private static final String DEFAULT_ENCODING = "UTF-8";

    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!MimetypeMap.MIMETYPE_RFC822.equals(sourceMimetype)
                || !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support RFC822 -> TEXT
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getComments(boolean available)
    {
        return onlySupports(MimetypeMap.MIMETYPE_RFC822, MimetypeMap.MIMETYPE_TEXT_PLAIN, available);
    }

    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception
    {
        InputStream contentInputStream = null;
        try{
            contentInputStream = reader.getContentInputStream();
            MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()), contentInputStream);

            final StringBuilder sb = new StringBuilder();
            Object content = mimeMessage.getContent();
            if (content instanceof Multipart)
            {
                processMultiPart((Multipart) content,sb);
            }
            else
            {
                sb.append(content.toString());
            }
            writer.putContent(sb.toString());
        }
        finally
        {
            if (contentInputStream != null)
            {
                try
                {
                contentInputStream.close();
                }
                catch ( IOException e)
                {
                    //stop exception propagation
                }
            }
        }
    }

    /**
     * Find "text" parts of message recursively and appends it to sb StringBuilder
     * 
     * @param multipart Multipart to process
     * @param sb StringBuilder 
     * @throws MessagingException
     * @throws IOException
     */
    private void processMultiPart(Multipart multipart, StringBuilder sb) throws MessagingException, IOException
    {
        boolean isAlternativeMultipart = multipart.getContentType().contains(MimetypeMap.MIMETYPE_MULTIPART_ALTERNATIVE);
        if (isAlternativeMultipart)
        {
            processAlternativeMultipart(multipart, sb);
        }
        else
        {
            for (int i = 0, n = multipart.getCount(); i < n; i++)
            {
                Part part = multipart.getBodyPart(i);
                if (part.getContent() instanceof Multipart)
                {
                    processMultiPart((Multipart) part.getContent(), sb);
                }
                else
                {
                    processPart(part, sb);
                }
            }
        }
    }
    
    
    /**
     * Finds the suitable part from an multipart/alternative and appends it's text content to StringBuilder sb
     * 
     * @param multipart
     * @param sb
     * @throws IOException
     * @throws MessagingException
     */
    private void processAlternativeMultipart(Multipart multipart, StringBuilder sb) throws IOException, MessagingException
    {
        Part partToUse = null;
        for (int i = 0, n = multipart.getCount(); i < n; i++)
        {
            Part part = multipart.getBodyPart(i);
            if (part.getContentType().contains(MimetypeMap.MIMETYPE_TEXT_PLAIN))
            {
                partToUse = part;
                break;
            }
            else if  (part.getContentType().contains(MimetypeMap.MIMETYPE_HTML)){
                partToUse = part;
            }
        }
        if (partToUse != null)
        {
            processPart(partToUse, sb);
        }
    }

    /**
     * Finds text on a given mail part. Accepted parts types are text/html and text/plain.
     * Attachments are ignored
     * 
     * @param part
     * @param sb
     * @throws IOException
     * @throws MessagingException
     */
    private void processPart(Part part, StringBuilder sb) throws IOException, MessagingException
    {
        boolean isAttachment = Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition());
        if (isAttachment)
        {
            return;
        }
        if (part.getContentType().contains(MimetypeMap.MIMETYPE_TEXT_PLAIN))
        {
            sb.append(part.getContent().toString());
        }
        else if (part.getContentType().contains(MimetypeMap.MIMETYPE_HTML))
        {
            String mailPartContent = part.getContent().toString();
            
            //create a temporary html file with same mail part content and encoding
            File tempHtmlFile = TempFileProvider.createTempFile("EMLTransformer_", ".html");
            ContentWriter contentWriter = new FileContentWriter(tempHtmlFile);
            contentWriter.setEncoding(getMailPartContentEncoding(part));
            contentWriter.setMimetype(MimetypeMap.MIMETYPE_HTML);
            contentWriter.putContent(mailPartContent);
            
            //transform html file's content to plain text
            EncodingAwareStringBean extractor = new EncodingAwareStringBean();
            extractor.setCollapse(false);
            extractor.setLinks(false);
            extractor.setReplaceNonBreakingSpaces(false);
            extractor.setURL(tempHtmlFile, contentWriter.getEncoding());
            sb.append(extractor.getStrings());
            
            tempHtmlFile.delete();
        }
    }
    
    private String getMailPartContentEncoding(Part part) throws MessagingException
    {
        String encoding = DEFAULT_ENCODING;
        String contentType = part.getContentType();
        int startIndex = contentType.indexOf(CHARSET);
        if (startIndex > 0)
        {
            encoding = contentType.substring(startIndex + CHARSET.length() + 1).replaceAll("\"", "");
        }
        return encoding;
    }

}
