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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

/**
 * Makes use of the {@link http://www.pdfbox.org/ PDFBox} library to
 * perform conversions from PDF files to images.
 * 
 * @author Neil McErlean
 */
public class PdfBoxPdfToImageContentTransformer extends AbstractContentTransformer2
{
    /**
     * The PDF spec allows for a default user password of the empty string.
     * See PDF Specification section 3.5 "Password Algorithms", specifically algorithms 3.6
     */
    private static final String PDF_DEFAULT_PASSWORD = "";
    private static Log logger = LogFactory.getLog(PdfBoxPdfToImageContentTransformer.class);

    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // only support PDF -> PNG  OR Adobe Illustrator -> PNG.
        // Recent .ai file format is  a .pdf file.
        return ( (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) ||
                  MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR.equals(sourceMimetype))
             && MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype));
    }

    @SuppressWarnings("unchecked")
    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        PDDocument document = null;
        try 
        {
           File file = TempFileProvider.createTempFile("pdfToImage", ".pdf");
           reader.getContent(file);
                        
           document = PDDocument.load(file);

           if (document.isEncrypted())
           {
               // Encrypted means password-protected, but PDF allows for two passwords: "owner" and "user".
               // A "user" of a document should be able to read (but not modify) the content.
               //
               // We'll attempt to open the document using the common PDF default password.
               document.openProtection(new StandardDecryptionMaterial(PDF_DEFAULT_PASSWORD));
           }
           boolean canExtractContent = document.getCurrentAccessPermission().canExtractContent();
           if (!canExtractContent)
           {
               
               String msg = "PDF document's intrinsic permissions forbid content extraction.";
               if (logger.isDebugEnabled())
               {
                   logger.debug(msg);
               }
               throw new AlfrescoRuntimeException(msg);
           }
           
           final int resolution = 16; // A rather arbitrary number for resolution (DPI) here.

           List pages = document.getDocumentCatalog().getAllPages();
           PDPage page = (PDPage)pages.get(0);
           BufferedImage img = page.convertToImage(BufferedImage.TYPE_INT_ARGB, resolution);
           
           File outputFile = TempFileProvider.createTempFile(this.getClass().getSimpleName(), ".png");
           ImageIO.write(img, "png", outputFile);
            
           writer.putContent(outputFile);
        } 
        catch (FileNotFoundException e1) 
        {
           throw new AlfrescoRuntimeException("Unable to create image from pdf file.", e1);
        } 
        catch (IOException e) 
        {
           throw new AlfrescoRuntimeException("Unable to create image from pdf file.", e);
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
    }
}
