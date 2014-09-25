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
import java.util.concurrent.TimeoutException;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.PoiOOXMLContentTransformer
 * 
 * @author Nick Burch
 * @author Dmitry Velichkevich
 */
public class PoiOOXMLContentTransformerTest extends AbstractContentTransformerTest
{
    private static final int SMALL_TIMEOUT = 30;

    private static final int ADDITIONAL_PROCESSING_TIME = 1500;


    private static final String ENCODING_UTF_8 = "UTF-8";

    private static final String TEST_PPTX_FILE_NAME = "quickImg2.pptx";


    private ContentService contentService;

    private PoiOOXMLContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PoiOOXMLContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);

        contentService = serviceRegistry.getContentService();
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
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
        
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, -1, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
    }

    /**
     * MNT-12043: CLONE - Upload of PPTX causes very high memory usage leading to system instability
     * 
     * @throws Exception
     */
    public void testMnt12043() throws Exception
    {
        transformer.setMimetypeService(mimetypeService);
        transformer.setAdditionalThreadTimout(0);
        configureExtractorLimits(transformer, SMALL_TIMEOUT);

        File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(TEST_PPTX_FILE_NAME);
        ContentReader sourceReader = new FileContentReader(sourceFile)
        {
            @Override
            public void setLimits(TransformationOptionLimits limits)
            {
                // Test without content reader input stream timeout limits
            }
        };
        sourceReader.setMimetype(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);

        ContentWriter tempWriter = AuthenticationUtil.runAs(new RunAsWork<ContentWriter>()
        {
            @Override
            public ContentWriter doWork() throws Exception
            {
                ContentWriter result = contentService.getTempWriter();
                result.setEncoding(ENCODING_UTF_8);
                result.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

                return result;
            }
        }, AuthenticationUtil.getAdminUserName());

        long startTime = System.currentTimeMillis();

        try
        {
            transformer.transform(sourceReader, tempWriter);
			// should not get here unless transform is too fast
            long transformationTime = System.currentTimeMillis() - startTime;
            fail("Content transformation took " + transformationTime + " ms, but should have failed with a timeout at " + SMALL_TIMEOUT + " ms");
        }
        catch (ContentIOException e)
        {
            long transformationTime = System.currentTimeMillis() - startTime;
            assertTrue((TimeoutException.class.getName() + " exception is expected as the cause of transformation failure"), e.getCause() instanceof TimeoutException);
            // Not sure we can have the following assert as we may have introduced an intermittent test failure. Already seen a time of 1009ms
            assertTrue(("Failed content transformation took " + transformationTime + " ms, but should have failed with a timeout at " + SMALL_TIMEOUT + " ms"),
                    transformationTime <= (SMALL_TIMEOUT + ADDITIONAL_PROCESSING_TIME));
        }

        assertFalse("Readable channel was not closed after transformation attempt!", sourceReader.isChannelOpen());
        assertFalse("Writable channel was not closed after transformation attempt!", tempWriter.isChannelOpen());
    }

    /**
     * Configures timeout for given <code>transformer</code>
     * 
     * @param extractor - {@link PoiOOXMLContentTransformer} instance
     * @param timeout - {@link Long} value which specifies timeout for <code>transformer</code>
     */
    private void configureExtractorLimits(PoiOOXMLContentTransformer transformer, final long timeout)
    {
        transformer.setTransformerConfig(new TransformerConfigImpl()
        {
            @Override
            public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype, String targetMimetype, String use)
            {
                TransformationOptionLimits result = new TransformationOptionLimits();
                result.setTimeoutMs(timeout);
                return result;
            }

            @Override
            public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype, boolean createNew)
            {
                return transformerConfig.getStatistics(transformer, sourceMimetype, targetMimetype, createNew);
            }

            @Override
            public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype, String targetMimetype, TransformationOptions options)
            {
                return transformerConfig.isSupportedTransformation(transformer, sourceMimetype, targetMimetype, options);
            }
        });
    }
}
