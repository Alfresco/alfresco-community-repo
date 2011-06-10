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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

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
        
        
        ZipFile iworksZipfile = null;
        try 
        {
            File iWorksTempFile = TempFileProvider.createTempFile(this.getClass().getSimpleName() + "_iWorks", sourceExtension);
            reader.getContent(iWorksTempFile);
            
            // iWorks files are zip files (at least in recent versions, iWork 09).
            // If it's not a zip file, the resultant ZipException will be caught as an IOException below.
            iworksZipfile = new ZipFile(iWorksTempFile);
            
            File tempOutFile = null;
            if (MimetypeMap.MIMETYPE_IMAGE_JPEG.equals(targetMimetype))
            {
                tempOutFile = copyZipEntryToTempFile(QUICK_LOOK_THUMBNAIL_JPG, iworksZipfile);
            }
            else if (MimetypeMap.MIMETYPE_PDF.equals(targetMimetype))
            {
                tempOutFile = copyZipEntryToTempFile(QUICK_LOOK_PREVIEW_PDF, iworksZipfile);
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file to " + targetMimetype);
            }
            
            writer.putContent(tempOutFile);
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
            if (iworksZipfile != null)
            {
                iworksZipfile.close();
            }
        }
    }
    
    /**
     * This method copies the contents of the specified zip-entry in the specified zip file
     * to a temporary file.
     * 
     * @return the File object for the just-created temporary file.
     */
    private File copyZipEntryToTempFile(String zipEntryName, ZipFile iworksZipfile) throws IOException
    {
        final String extension = zipEntryName.endsWith(".jpg") ? ".jpg" : ".pdf";
        ZipEntry embeddedQuicklookResource = iworksZipfile.getEntry(zipEntryName);
        
        if (embeddedQuicklookResource == null)
        {
            throw new AlfrescoRuntimeException("Unable to transform iWorks file as there was no embedded preview.");
        }
        
        File outputFile = TempFileProvider.createTempFile(this.getClass().getSimpleName() + "_ZipEntry", extension);
        
        BufferedOutputStream bufOut = null;
        
        try
        {
            InputStream zin = iworksZipfile.getInputStream(embeddedQuicklookResource);
            bufOut = new BufferedOutputStream(new FileOutputStream(outputFile));
            StreamUtils.copy(zin, bufOut);
        }
        finally
        {
            // zin closed in calling method
            if (bufOut != null)
            {
                bufOut.close();
            }
        }
        
        return outputFile;
    }
}
