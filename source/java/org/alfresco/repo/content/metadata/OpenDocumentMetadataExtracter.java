/*
 * Copyright (C) 2005 Antti Jokipii
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

import com.catcode.odf.ODFMetaFileAnalyzer;
import com.catcode.odf.OpenDocumentMetadata;

/**
 * Metadata extractor for the
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_OPENDOCUMENT_TEXT MIMETYPE_OPENDOCUMENT_XXX}
 * mimetypes.
 * 
 * @author Antti Jokipii
 */
public class OpenDocumentMetadataExtracter extends AbstractMetadataExtracter
{
    private static String[] mimeTypes = new String[] {
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_MASTER,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_WEB,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_DATABASE, };

    public OpenDocumentMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(mimeTypes)), 1.00, 1000);
    }

    public void extractInternal(ContentReader reader, Map<QName, Serializable> destination) throws Throwable
    {
        ODFMetaFileAnalyzer analyzer = new ODFMetaFileAnalyzer();
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            // stream the document in
            OpenDocumentMetadata docInfo = analyzer.analyzeZip(is);

            if (docInfo != null)
            {
                // set the metadata
                destination.put(ContentModel.PROP_AUTHOR, docInfo.getCreator());
                destination.put(ContentModel.PROP_TITLE, docInfo.getTitle());
                destination.put(ContentModel.PROP_DESCRIPTION, docInfo.getDescription());
                destination.put(ContentModel.PROP_CREATED, docInfo.getCreationDate());
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
}