/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.PdfBoxContentTransformer
 * 
 * @author Derek Hulley
 */
public class PdfBoxContentTransformerTest extends AbstractContentTransformerTest
{
    private PdfBoxContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PdfBoxContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
    }

    /**
     * Added to test a single transform that appeared to have problems.
     * Commented out once issue was fixed, but left in the code to help with
     * future issues.
     * @throws Exception
     */
    public void testPdfToTextConversions() throws Exception
    {
        final String sourceMimetype = MimetypeMap.MIMETYPE_PDF;
        final String targetMimetype = MimetypeMap.MIMETYPE_TEXT_PLAIN;
        int transforms = 100;
        final String filename = "svn-book.pdf";
        
        final CountDownLatch doneSignal = new CountDownLatch(transforms);
        
        int threadCount = 8;
        final ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        long time = System.currentTimeMillis();
        for (int i=0; i<transforms; i++)
            
        {
            threadPool.submit(new Runnable() {
                public void run()
                {
                    try
                    {
                        pdfToTextTransform(filename, sourceMimetype, targetMimetype);
                        doneSignal.countDown();
                    }
                    catch (IOException e)
                    {
                        threadPool.shutdown();
                        e.printStackTrace();
                    }
                }});
            if (i < threadCount)
            {
                Thread.sleep(1000);
            }
        }
        boolean okay = doneSignal.await(100, TimeUnit.SECONDS);
        
        time = System.currentTimeMillis() - time;
        transforms = transforms - (int)doneSignal.getCount();
        String message = "Total time "+time+" ms   "+(transforms > 0 ? "average="+(time/transforms)+" ms" : "")+"  threads="+threadCount+"  transforms="+transforms;
        System.out.println(message);
        
        if (!okay)
        {
            // Before the changes to PDFBox, this would fail having only done about 50 transforms.
            // After the change, this takes about 55 seconds
            fail("********** Transforms did not finish ********** "+message);
        }
    }

    private void pdfToTextTransform(String filename, String sourceMimetype, String targetMimetype) throws IOException
    {
        ContentWriter targetWriter = null;
        String sourceExtension = filename.substring(filename.lastIndexOf('.') + 1);
        String targetExtension = mimetypeService.getExtension(targetMimetype);

        File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(filename);
        ContentReader sourceReader = new FileContentReader(sourceFile);

        AbstractContentTransformer2 transformer = (AbstractContentTransformer2) getTransformer(
                sourceMimetype, targetMimetype);

        // make a writer for the target file
        File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_"
                + getName() + "_" + sourceExtension + "_", "." + targetExtension);
        targetWriter = new FileContentWriter(targetFile);

        // do the transformation
        sourceReader.setMimetype(sourceMimetype);
        targetWriter.setMimetype(targetMimetype);
        transformer.transform(sourceReader.getReader(), targetWriter);
    }
}
