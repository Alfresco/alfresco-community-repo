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
            // Scoop out the metadata
            PDDocumentInformation docInfo = pdf.getDocumentInformation();

            trimPut(ContentModel.PROP_AUTHOR, docInfo.getAuthor(), destination);
            trimPut(ContentModel.PROP_TITLE, docInfo.getTitle(), destination);
            trimPut(ContentModel.PROP_DESCRIPTION, docInfo.getSubject(), destination);

            Calendar created = docInfo.getCreationDate();
            if (created != null)
                destination.put(ContentModel.PROP_CREATED, created.getTime());
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
