/*
 * Copyright (C) 2005 Jesper Steen Møller
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;

/**
 * 
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracter extends AbstractMetadataExtracter
{
    public PdfBoxMetadataExtracter()
    {
        super(MimetypeMap.MIMETYPE_PDF, 1.0, 1000);
    }

    public void extractInternal(ContentReader reader, Map<QName, Serializable> destination) throws Throwable
    {
        PDDocument pdf = null;
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            // stream the document in
            pdf = PDDocument.load(is);
            if (!pdf.isEncrypted())
            {
                // Scoop out the metadata
                PDDocumentInformation docInfo = pdf.getDocumentInformation();
    
                trimPut(ContentModel.PROP_AUTHOR, docInfo.getAuthor(), destination);
                trimPut(ContentModel.PROP_TITLE, docInfo.getTitle(), destination);
                trimPut(ContentModel.PROP_DESCRIPTION, docInfo.getSubject(), destination);
    
                Calendar created = docInfo.getCreationDate();
                if (created != null)
                    destination.put(ContentModel.PROP_CREATED, created.getTime());
            }
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
            if (pdf != null)
            {
                try { pdf.close(); } catch (Throwable e) { e.printStackTrace(); }
            }
        }
    }
}
