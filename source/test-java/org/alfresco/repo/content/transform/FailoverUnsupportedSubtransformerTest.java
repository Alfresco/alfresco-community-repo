/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test for the MNT-9257: html2pdf transformer is being triggered incorrectly for password protected MS files
 * 
 * @see org.alfresco.repo.content.transform.FailoverContentTransformer
 * 
 * @author Viachaslau Tsikhanovich
 */
public class FailoverUnsupportedSubtransformerTest extends AbstractContentTransformerTest
{
    private static final String sourceMimeType = MimetypeMap.MIMETYPE_EXCEL;
    private static final String targetMimeType = MimetypeMap.MIMETYPE_PDF;
    
    private static ApplicationContext failoverAppContext =
        new ClassPathXmlApplicationContext(new String[] {"classpath:org/alfresco/repo/content/transform/FailoverContentTransformerTest-context.xml"},
            ApplicationContextHelper.getApplicationContext());
    
    private TestFailoverContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ApplicationContextHelper.getApplicationContext();
        
        transformer = (TestFailoverContentTransformer) failoverAppContext.getBean("transformer.failover.Test-PasswordProtectedMSExcel2Pdf");
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.getTriggered().setValue(false);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        // The MIME types here are rather arbitrary

        boolean reliability = transformer.isTransformable(sourceMimeType, -1, targetMimeType, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
    
    @Override
    public void testAllConversions() throws Exception
    {
        // skip
    }
    

    public void testExcelToPdfConversion() throws Exception
    {
        String[] quickFiles = getQuickFilenames(sourceMimeType);
        for (String quickFile : quickFiles)
        {
            String sourceExtension = quickFile.substring(quickFile.lastIndexOf('.') + 1);

            // is there a test file for this conversion?
            File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFile);
            if (sourceFile == null)
            {
                continue; // no test file available for that extension
            }
            ContentReader sourceReader = new FileContentReader(sourceFile);

            // make a writer for the target file
            File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_" + getName() + "_" + sourceExtension + "_", ".pdf");
            ContentWriter targetWriter = new FileContentWriter(targetFile);

            // do the transformation
            sourceReader.setMimetype(sourceMimeType);
            targetWriter.setMimetype(targetMimeType);
            try
            {
                transformer.transform(sourceReader.getReader(), targetWriter);
            }
            catch (ContentIOException e)
            {
                // all transformers expected to fail for password protected MS office document
            }
            
            if (transformer.getTriggered().getValue())
            {
                org.junit.Assert.fail("final AbstractContentTransformer2.transform was called for html2pdf");
            }
        }
    }

}

/**
 * To share with DummyTestComplexSubtransformer to detect if html2pdf was triggered
 */
class TestHtml2PdfTriggeredFlag
{
    private boolean value;

    public boolean getValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }
}

class TestFailoverContentTransformer extends FailoverContentTransformer
{
    private TestHtml2PdfTriggeredFlag triggered;

    public void setTriggered(TestHtml2PdfTriggeredFlag triggered)
    {
        this.triggered = triggered;
    }

    public TestHtml2PdfTriggeredFlag getTriggered()
    {
        return triggered;
    }
}

/**
 * This dummy class is used only for test purposes within this source file.
 */
class DummyTestComplexSubtransformer extends ComplexContentTransformer
{

    private TestHtml2PdfTriggeredFlag triggered;

    public void setTriggered(TestHtml2PdfTriggeredFlag triggered)
    {
        this.triggered = triggered;
    }

    
    @Override
    protected void checkTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options)
    {
        // checkTransformable is called only from AbstractContentTransformer2.transform
        // that is final and cannot be overridden
        triggered.setValue(true);
        
        super.checkTransformable(reader, writer, options);
    }

    @Override
    public void transformInternal(ContentReader reader,
            ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        triggered.setValue(true);

        reader.getContentString();
        
        // alwaysFail
        throw new AlfrescoRuntimeException("Test code intentionally failed method call.");
    }

}

/**
 * This dummy class is used only for test purposes within this source file.
 * 
 * Supported source mimetype can be set
 */
class DummyAnyToPDFTestSubtransformer extends DummyTestContentTransformer
{
    @Override
    public boolean isTransformableMimetype(String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        // We'll arbitrarily claim to be able to transform PDF to PNG
        return MimetypeMap.MIMETYPE_PDF.equals(targetMimetype);
    }
}
