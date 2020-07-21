/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Converts Apple iWorks files to JPEGs for thumbnailing & previewing.
 * The transformer will only work for iWorks 2013/14 files. Support for iWorks 2008/9 has been dropped as we cannot
 * support both, because the newer format does not contain a PDF. If we say this transformer supports PDF, Share will
 * assume incorrectly that we can convert to PDF and we would only get a preview for the older format and never the
 * newer one. Both formats have the same mimetype.
 *
 * @author Neil Mc Erlean
 * @since 4.0
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class AppleIWorksContentTransformer extends AbstractRemoteContentTransformer
{
    private static final Log logger = LogFactory.getLog(AppleIWorksContentTransformer.class);

    // Apple's zip entry names for previews in iWorks have changed over time.
    private static final List<String> PDF_PATHS = Arrays.asList(
            "QuickLook/Preview.pdf");  // iWorks 2008/9
    private static final List<String> JPG_PATHS = Arrays.asList(
            "QuickLook/Thumbnail.jpg", // iWorks 2008/9
            "preview.jpg");            // iWorks 2013/14 (720 x 552) We use the best quality image. Others are:
    //                (225 x 173) preview-web.jpg
    //                 (53 x  41) preview-micro.jpg

    private static final List<String> IWORKS_MIMETYPES = Arrays.asList(MimetypeMap.MIMETYPE_IWORK_KEYNOTE,
            MimetypeMap.MIMETYPE_IWORK_NUMBERS,
            MimetypeMap.MIMETYPE_IWORK_PAGES);
    private static final List<String> TARGET_MIMETYPES = Arrays.asList(MimetypeMap.MIMETYPE_IMAGE_JPEG
// Commented out rather than removed, in case we can get SHARE to fall back to using JPEG when a PDF is not available
//                                                                    ,MimetypeMap.MIMETYPE_PDF
    );

    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // only support [iWorks] -> JPEG but only if these are embedded in the file.
        // This is because iWorks 13+ files are zip files containing embedded jpeg previews.
        return TARGET_MIMETYPES.contains(targetMimetype) && IWORKS_MIMETYPES.contains(sourceMimetype);
    }

    @Override
    public String getComments(boolean available)
    {
        return getCommentsOnlySupports(IWORKS_MIMETYPES, TARGET_MIMETYPES, available);
    }

    @Override
    protected Log getLogger()
    {
        return logger;
    }

    @Override
    protected void transformLocal(ContentReader reader,
                                  ContentWriter writer,
                                  TransformationOptions options) throws Exception
    {
        final String sourceMimetype = reader.getMimetype();
        final String sourceExtension = getMimetypeService().getExtension(sourceMimetype);
        final String targetMimetype = writer.getMimetype();
        final String targetExtension = getMimetypeService().getExtension(targetMimetype);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Transforming from ").append(sourceMimetype)
                    .append(" to ").append(targetMimetype);
            logger.debug(msg.toString());
        }

        ZipArchiveInputStream iWorksZip = null;
        try
        {
            // iWorks files are zip (or package) files.
            // If it's not a zip file, the resultant ZipException will be caught as an IOException below.
            iWorksZip = new ZipArchiveInputStream(reader.getContentInputStream());

            // Look through the zip file entries for the preview/thumbnail.
            List<String> paths = MimetypeMap.MIMETYPE_IMAGE_JPEG.equals(targetMimetype) ? JPG_PATHS : PDF_PATHS;
            ZipArchiveEntry entry;
            boolean found = false;
            while ((entry=iWorksZip.getNextZipEntry()) != null)
            {
                String name = entry.getName();
                if (paths.contains(name))
                {
                    writer.putContent( iWorksZip );
                    found = true;
                    break;
                }
            }

            if (! found)
            {
                throw new AlfrescoRuntimeException("The source " + sourceExtension + " file did not contain a " + targetExtension + " preview");
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file. It should have been a zip format file.", e);
        }
        finally
        {
            if (iWorksZip != null)
            {
                iWorksZip.close();
            }
        }
    }

    @Override
    protected void transformRemote(RemoteTransformerClient remoteTransformerClient, ContentReader reader,
                                   ContentWriter writer, TransformationOptions options, String sourceMimetype,
                                   String targetMimetype, String sourceExtension, String targetExtension,
                                   String targetEncoding) throws Exception
    {
        long timeoutMs = options.getTimeoutMs();

        remoteTransformerClient.request(reader, writer, sourceMimetype, sourceExtension, targetExtension,
                timeoutMs, logger,
                "transformName", "appleIWorks",
                "sourceMimetype", sourceMimetype,
                "sourceExtension", sourceExtension,
                "targetMimetype", targetMimetype);
    }
}
