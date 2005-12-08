/*
 * Copyright (C) 2005 Antti Jokipii
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log logger = LogFactory.getLog(OpenDocumentMetadataExtracter.class);

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

    public void extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException
    {
        ODFMetaFileAnalyzer analyzer = new ODFMetaFileAnalyzer();
        try
        {
            // stream the document in
            OpenDocumentMetadata docInfo = analyzer.analyzeZip(reader.getContentInputStream());

            if (docInfo != null)
            {
                // set the metadata
                destination.put(ContentModel.PROP_CREATOR, docInfo.getCreator());
                destination.put(ContentModel.PROP_TITLE, docInfo.getTitle());
                destination.put(ContentModel.PROP_DESCRIPTION, docInfo.getDescription());
                destination.put(ContentModel.PROP_CREATED, docInfo.getCreationDate());
            }
        }
        catch (Throwable e)
        {
            String message = "Metadata extraction failed: \n" +
                    "   reader: " + reader;
            logger.debug(message, e);
            throw new ContentIOException(message, e);
        }
    }
}