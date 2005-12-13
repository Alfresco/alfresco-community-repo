/*
 * Copyright (C) 2005 Jesper Steen Møller
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
import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;

/**
 * 
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracter extends AbstractMetadataExtracter
{

    private static final Log logger = LogFactory.getLog(PdfBoxMetadataExtracter.class);

    public PdfBoxMetadataExtracter()
    {
        super(MimetypeMap.MIMETYPE_PDF, 1.0, 1000);
    }

    public void extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException
    {
        if (!MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype()))
        {
            logger.debug("No metadata extracted for " + reader.getMimetype());
            return;
        }
        PDDocument pdf = null;
        try
        {
            // stream the document in
            pdf = PDDocument.load(reader.getContentInputStream());
            // Scoop out the metadata
            PDDocumentInformation docInfo = pdf.getDocumentInformation();

            trimPut(ContentModel.PROP_AUTHOR, docInfo.getAuthor(), destination);
            trimPut(ContentModel.PROP_TITLE, docInfo.getTitle(), destination);
            trimPut(ContentModel.PROP_DESCRIPTION, docInfo.getSubject(), destination);

            Calendar created = docInfo.getCreationDate();
            if (created != null)
                destination.put(ContentModel.PROP_CREATED, created.getTime());
        }
        catch (IOException e)
        {
            throw new ContentIOException("PDF metadata extraction failed: \n" +
                    "   reader: " + reader);
        }
        finally
        {
            if (pdf != null)
            {
                try
                {
                    pdf.close();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
