/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

/**
 * Extracts out Thumbnail JPEGs from OOXML files for thumbnailing & previewing.
 * This transformer will only work for OOXML files where thumbnailing was enabled,
 *  which isn't on by default on Windows, but is more common on Mac. 
 * 
 * @author Nick Burch
 * @since 4.0.1
 */
public class OOXMLThumbnailContentTransformer extends AbstractContentTransformer2
{
    public static final String NO_THUMBNAIL_PRESENT_IN_FILE = "No thumbnail present in file, unable to generate ";

    private static final Log log = LogFactory.getLog(OOXMLThumbnailContentTransformer.class);
    
    private static final List<String> OOXML_MIMETYPES = Arrays.asList(new String[]{
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_ADDIN,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDE,
            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDE_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_ADDIN_MACRO,
            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_BINARY_MACRO});
    
    private static final List<String> TARGET_MIMETYPES = Arrays.asList(new String[]{MimetypeMap.MIMETYPE_IMAGE_JPEG});
    
    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // only support [OOXML] -> JPEG
        return TARGET_MIMETYPES.contains(targetMimetype) && OOXML_MIMETYPES.contains(sourceMimetype);
    }
    
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append("# Only supports extraction of embedded jpegs from OOXML formats\n");
        return sb.toString();
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
        
        
        OPCPackage pkg = null;
        try 
        {
            File ooxmlTempFile = TempFileProvider.createTempFile(this.getClass().getSimpleName() + "_ooxml", sourceExtension);
            reader.getContent(ooxmlTempFile);
            
            // Load the file
            pkg = OPCPackage.open(ooxmlTempFile.getPath());
            
            // Does it have a thumbnail?
            PackageRelationshipCollection rels = 
                pkg.getRelationshipsByType(PackageRelationshipTypes.THUMBNAIL);
            if (rels.size() > 0)
            {
                // Get the thumbnail part
                PackageRelationship tRel = rels.getRelationship(0);
                PackagePart tPart = pkg.getPart(tRel);
                
                // Write it to the target
                InputStream tStream = tPart.getInputStream();
                writer.putContent( tStream );
                tStream.close();
            }
            else
            {
                log.debug("No thumbnail present in " + reader.toString());
                throw new UnimportantTransformException(NO_THUMBNAIL_PRESENT_IN_FILE + targetMimetype);
            }
        } 
        catch (IOException e) 
        {
           throw new AlfrescoRuntimeException("Unable to transform " + sourceExtension + " file.", e);
        }
        finally
        {
            if (pkg != null)
            {
                pkg.close();
            }
        }
    }
}
