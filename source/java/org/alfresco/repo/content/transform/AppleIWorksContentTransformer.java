/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converts Apple iWorks files to PDFs or JPEGs for thumbnailing & previewing.
 * The transformer will only work for iWorks 09 files and later as previous versions of those files
 * were actually saved as directories.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class AppleIWorksContentTransformer extends AbstractContentTransformer2
{
    private static final Log log = LogFactory.getLog(AppleIWorksContentTransformer.class);
    
    // Apple's zip entry names for the front-page and all-doc previews in iWorks.
    // We don't attempt to parse the XML 'manifest' (index.xml) within the iWorks file.
    private static final String QUICK_LOOK_PREVIEW_PDF   = "QuickLook/Preview.pdf";
    private static final String QUICK_LOOK_THUMBNAIL_JPG = "QuickLook/Thumbnail.jpg";
    
    private static final List<String> IWORKS_MIMETYPES = Arrays.asList(new String[]{MimetypeMap.MIMETYPE_IWORK_KEYNOTE,
                                                                                    MimetypeMap.MIMETYPE_IWORK_NUMBERS,
                                                                                    MimetypeMap.MIMETYPE_IWORK_PAGES});
    private static final List<String> TARGET_MIMETYPES = Arrays.asList(new String[]{MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                                                    MimetypeMap.MIMETYPE_PDF});
    
    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // only support [iWorks] -> JPEG | PDF
        // This is because iWorks 09+ files are zip files containing embedded jpeg/pdf previews.
        return TARGET_MIMETYPES.contains(targetMimetype) && IWORKS_MIMETYPES.contains(sourceMimetype);
    }

    @Override
    public String getComments(boolean available)
    {
        return getCommentsOnlySupports(IWORKS_MIMETYPES, TARGET_MIMETYPES, available);
    }

    @Override
    protected void transformInternal(ContentReader reader,
                                     ContentWriter writer,
                                     TransformationOptions options) throws Exception
    {
        final String sourceMimetype = reader.getMimetype();
        final String sourceExtension = getMimetypeService().getExtension(sourceMimetype);
        final String targetMimetype = writer.getMimetype();
        
        
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Transforming from ").append(sourceMimetype)
               .append(" to ").append(targetMimetype);
            log.debug(msg.toString());
        }
        
        
        ZipArchiveInputStream iWorksZip = null;
        try 
        {
            // iWorks files are zip files (at least in recent versions, iWork 09).
            // If it's not a zip file, the resultant ZipException will be caught as an IOException below.
            iWorksZip = new ZipArchiveInputStream(reader.getContentInputStream());
            
            ZipArchiveEntry entry = null;
            boolean found = false;
            while ( !found && (entry = iWorksZip.getNextZipEntry()) != null )
            {
                if (MimetypeMap.MIMETYPE_IMAGE_JPEG.equals(targetMimetype) && 
                    entry.getName().equals(QUICK_LOOK_THUMBNAIL_JPG))
                {
                    writer.putContent( iWorksZip );
                    found = true;
                }
                else if (MimetypeMap.MIMETYPE_PDF.equals(targetMimetype) &&
                         entry.getName().equals(QUICK_LOOK_PREVIEW_PDF))
                {
                    writer.putContent( iWorksZip );
                    found = true;
                }
            }
            
            if (! found)
            {
                throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file to " + targetMimetype);
            }
        } 
        catch (FileNotFoundException e1) 
        {
           throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file.", e1);
        } 
        catch (IOException e) 
        {
           throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file.", e);
        }
        finally
        {
            if (iWorksZip != null)
            {
                iWorksZip.close();
            }
        }
    }
}
