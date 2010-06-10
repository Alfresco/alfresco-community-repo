/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class transforms archive files (currently only ZIPs) to text, which enables indexing
 * and searching of archives as well as webpreviewing.
 * The transformation simply lists the names of the entries within the zip file and does not consider their content.
 * 
 * @author Neil McErlean
 * @since Swift
 */
public class ArchiveContentTransformer extends AbstractContentTransformer2
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ArchiveContentTransformer.class);

    /**
     * Currently the only transformation performed is that of text extraction from PDF documents.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // TODO: Expand to other archive types e.g. tar.
        if (!MimetypeMap.MIMETYPE_ZIP.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // Currently only support ZIP -> Text
            return false;
        }
        else
        {
            return true;
        }
    }

    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();

            List<String> zipEntryNames = new ArrayList<String>();
        	ZipInputStream zin = new ZipInputStream(is);

            // Enumerate each entry
        	ZipEntry nextZipEntry = null;
        	while ((nextZipEntry = zin.getNextEntry()) != null)
        	{
        		String entryName = nextZipEntry.getName();
        		zipEntryNames.add(entryName);
        		
        		// Currently we do not recurse into 'zips within zips'.
        	}
        	
        	if (logger.isDebugEnabled())
        	{
        	    StringBuilder msg = new StringBuilder();
        	    msg.append("Transformed ")
       	           .append(zipEntryNames.size())
       	           .append(zipEntryNames.size() == 1 ? " zip entry" : " zip entries");
        	    logger.debug(msg.toString());
        	}

            String text = createTextContentFrom(zipEntryNames);
            
            // dump it all to the writer
            writer.putContent(text);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {e.printStackTrace(); }
            }
        }
    }

    private String createTextContentFrom(List<String> zipEntryNames)
    {
        StringBuilder result = new StringBuilder();
        for (String entryName : zipEntryNames)
        {
            result.append(entryName)
                  .append('\n');
        }
        return result.toString();
    }
}
