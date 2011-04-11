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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * Makes use of the {@link https://pdf-renderer.dev.java.net/ PDFRenderer} library to
 * perform conversions from PDF files to images.
 * 
 * @author Roy Wetherall
 */
public class PdfToImageContentTransformer extends AbstractContentTransformer2
{
    private static final Log logger = LogFactory.getLog(PdfToImageContentTransformer.class);
    /**
     * Currently the only transformation performed is that of text extraction from PDF documents.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) == true &&
            MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype) == true)
        {
            // only support PDF -> PNG
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        RandomAccessFile raf = null;
        try 
        {
           File file = TempFileProvider.createTempFile("pdfToImage", ".pdf");
           reader.getContent(file);
                        
           raf = new RandomAccessFile(file, "r");
           FileChannel channel = raf.getChannel();
          
           ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            
           PDFFile pdffile = new PDFFile(buf);

           // Log the PDF version of the file being transformed.
           if (logger.isInfoEnabled())
           {
        	   int pdfMajorVersion = pdffile.getMajorVersion();
        	   int pdfMinorVersion = pdffile.getMinorVersion();
        	   StringBuilder msg = new StringBuilder();
        	   msg.append("File being transformed is of pdf version ")
        	       .append(pdfMajorVersion).append(".").append(pdfMinorVersion);
        	   logger.info(msg.toString());
           }
           
           PDFPage page = pdffile.getPage(0, true);
              
           //get the width and height for the doc at the default zoom              
           int width=(int)page.getBBox().getWidth();
           int height=(int)page.getBBox().getHeight();
            
           Rectangle rect = new Rectangle(0,0,width,height);
           int rotation=page.getRotation();
           Rectangle rect1=rect;
           if (rotation==90 || rotation==270)
               rect1=new Rectangle(0,0,rect.height,rect.width);
            
           //generate the image
           BufferedImage img = (BufferedImage)page.getImage(
                        rect.width, rect.height, //width & height
                        rect1, // clip rect
                        null, // null for the ImageObserver
                        true, // fill background with white
                        true  // block until drawing is done
                );

           File outputFile = TempFileProvider.createTempFile("pdfToImageOutput", ".png");
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
            if (raf != null)
            {
		        try
		        {
		            raf.close();
		        }
		        catch (IOException ignored)
		        {
		            // Intentionally empty
		        }
            }
        }
    }
}
