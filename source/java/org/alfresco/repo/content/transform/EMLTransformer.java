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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.Icu4jEncodingDetector;

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
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        TikaInputStream tikaInputStream = null;
        try
        {
            // wrap the given stream to a TikaInputStream instance
            tikaInputStream = TikaInputStream.get(reader.getContentInputStream());

            final Icu4jEncodingDetector encodingDetector = new Icu4jEncodingDetector();
            final Charset charset = encodingDetector.detect(tikaInputStream, new Metadata());

            MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()), tikaInputStream);
            if (charset != null)
            {
                mimeMessage.setHeader("Content-Type", "text/plain; charset=" + charset.name());
                mimeMessage.setHeader("Content-Transfer-Encoding", "quoted-printable");
            }
            final StringBuilder sb = new StringBuilder();
            Object content = mimeMessage.getContent();
            if (content instanceof Multipart)
            {
                sb.append(processMultiPart((Multipart) content));
            }
            else
            {
                sb.append(content.toString());
            }
            writer.putContent(sb.toString());
        }
        finally
        {
            if (tikaInputStream != null)
            {
                try
                {
                    // it closes any other resources associated with it
                    tikaInputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find "text" parts of message recursively
     * 
     * @param multipart Multipart to process
     * @return "text" parts of message
     * @throws MessagingException
     * @throws IOException
     */
    private StringBuilder processMultiPart(Multipart multipart) throws MessagingException, IOException
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = multipart.getCount(); i < n; i++)
        {
            Part part = multipart.getBodyPart(i);
            if (part.getContent() instanceof Multipart)
            {
                sb.append(processMultiPart((Multipart) part.getContent()));

            }
            else if (part.getContentType().contains("text"))
            {
                sb.append(part.getContent().toString()).append("\n");

            }

        }

        return sb;
    }

}
