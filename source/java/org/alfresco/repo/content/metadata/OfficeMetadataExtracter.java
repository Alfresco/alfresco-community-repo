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
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;

/**
 * 
 * @author Jesper Steen Møller
 */
public class OfficeMetadataExtracter extends AbstractMetadataExtracter
{

    private static final Log logger = LogFactory.getLog(OfficeMetadataExtracter.class);
    private static String[] mimeTypes = new String[] { MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_EXCEL,
            MimetypeMap.MIMETYPE_PPT };

    public OfficeMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(mimeTypes)), 1.0, 1000);
    }

    public void extract(ContentReader reader, final Map<QName, Serializable> destination) throws ContentIOException
    {
        POIFSReaderListener readerListener = new POIFSReaderListener()
        {
            public void processPOIFSReaderEvent(final POIFSReaderEvent event)
            {
                try
                {
                    PropertySet ps = PropertySetFactory.create(event.getStream());
                    if (ps instanceof SummaryInformation)
                    {
                        SummaryInformation si = (SummaryInformation) ps;
                        // Titled aspect
                        trimPut(ContentModel.PROP_TITLE, si.getTitle(), destination);
                        trimPut(ContentModel.PROP_DESCRIPTION, si.getSubject(), destination);

                        // Auditable aspect
                        trimPut(ContentModel.PROP_CREATED, si.getCreateDateTime(), destination);
                        trimPut(ContentModel.PROP_CREATOR, si.getAuthor(), destination);
                        trimPut(ContentModel.PROP_MODIFIED, si.getLastSaveDateTime(), destination);
                        trimPut(ContentModel.PROP_MODIFIER, si.getLastAuthor(), destination);
                    }
                    else if (ps instanceof DocumentSummaryInformation)
                    {
                        DocumentSummaryInformation dsi = (DocumentSummaryInformation) ps;

                        // These are not really interesting to any aspect:
                        // trimPut(ContentModel.PROP_xxx, dsi.getCompany(),
                        // destination);
                        // trimPut(ContentModel.PROP_yyy, dsi.getManager(),
                        // destination);
                    }
                }
                catch (Exception ex)
                {
                    throw new ContentIOException("Property set stream: " + event.getPath() + event.getName(), ex);
                }
            }
        };
        try
        {
            POIFSReader r = new POIFSReader();
            r.registerListener(readerListener, SummaryInformation.DEFAULT_STREAM_NAME);
            r.read(reader.getContentInputStream());
        }
        catch (IOException e)
        {
            throw new ContentIOException("Compound Document SummaryInformation metadata extraction failed: \n"
                    + "   reader: " + reader,
                    e);
        }
    }
}
