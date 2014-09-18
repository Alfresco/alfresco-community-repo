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
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @see org.alfresco.repo.content.metadata.PoiMetadataExtracter
 * 
 * @author Neil McErlean
 * @author Dmitry Velichkevich
 */
public class PoiMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static final int MINIMAL_EXPECTED_PROPERTIES_AMOUNT = 3;

    private static final int IGNORABLE_TIMEOUT = -1;

    // private static final int TIMEOUT_FOR_QUICK_EXTRACTION = 2000;

    private static final int DEFAULT_FOOTNOTES_LIMIT = 50;

    private static final int LARGE_FOOTNOTES_LIMIT = 25000;


    private static final String ALL_MIMETYPES_FILTER = "*";

    private static final String PROBLEM_FOOTNOTES_DOCUMENT_NAME = "problemFootnotes2.docx";

    // private static final String PROBLEM_SLIDE_SHOW_DOCUMENT_NAME = "problemSlideShow.pptx";

    private static final String EXTRACTOR_POI_BEAN_NAME = "extracter.Poi";


    private PoiMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new PoiMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        resetPoiConfigurationToDefault();
        extracter.register();
    }

    @Override
    protected void tearDown() throws Exception
    {
        resetPoiConfigurationToDefault();
        super.tearDown();
    }

    /**
     * Resets POI library configuration to default. Sets allowable XSLF relationship types and footnotes limit as per 'extracter.Poi' bean configuration
     * 
     * @throws Exception
     */
    private void resetPoiConfigurationToDefault() throws Exception
    {
        PoiMetadataExtracter configuredExtractor = (PoiMetadataExtracter) ctx.getBean(EXTRACTOR_POI_BEAN_NAME);
        extracter.setPoiExtractPropertiesOnly(true);
        extracter.setPoiFootnotesLimit(DEFAULT_FOOTNOTES_LIMIT);
        extracter.setPoiAllowableXslfRelationshipTypes(configuredExtractor.getPoiAllowableXslfRelationshipTypes());
        extracter.afterPropertiesSet();
    }

    @Override
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : PoiMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testOffice2007Extraction() throws Exception
    {
        for (String mimetype : PoiMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }

    @Override
    protected boolean skipDescriptionCheck(String mimetype) 
    {
        // Our 3 OpenOffice 07 quick files have no description properties.
        return true;
    }


    @Override
    protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) 
    {
        // This test class is testing 3 files: quick.docx, quick.xlsx & quick.pptx.
        // Their created times are hard-coded here for checking.
        // Of course this means that if the files are updated, the test will break
        // but those files are rarely modified - only added to.
        if (MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING.equals(mimetype))
        {
            checkFileCreationDate(mimetype, properties, "2010-01-06T17:32:00.000Z");
        }
        else if (MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET.equals(mimetype))
        {
            checkFileCreationDate(mimetype, properties, "1996-10-14T23:33:28.000Z");
        }
        else if (MimetypeMap.MIMETYPE_OPENXML_PRESENTATION.equals(mimetype))
        {
            // Extraordinary! This document predates Isaac Newton's Principia Mathematica by almost a century. ;)
            checkFileCreationDate(mimetype, properties, "1601-01-01T00:00:00.000Z");
        }
    }

    private void checkFileCreationDate(String mimetype, Map<QName, Serializable> properties, String date)
    {
        assertEquals("Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype, date,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
    }
    
    /**
     * Tests that metadata extraction from a somewhat corrupt file with several
     * thousand footnotes times out properly.
     * 
     * @throws Exception
     */
    public void testProblemFootnotes() throws Exception
    {
        long timeoutMs = 2000;
        
        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(ALL_MIMETYPES_FILTER, limits);
        ((PoiMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);
        
        File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile("problemFootnotes.docx");
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);
        
        long startTime = System.currentTimeMillis();

        getExtracter().extract(sourceReader, properties);
        
        long extractionTime = System.currentTimeMillis() - startTime;
        
        assertTrue("Metadata extraction took (" + extractionTime + "ms) " +
                "but should have failed with a timeout at " + timeoutMs + "ms", 
                extractionTime < (timeoutMs + 100)); // bit of wiggle room for logging, cleanup, etc.
        assertFalse("Reader was not closed", sourceReader.isChannelOpen());
    }

//    /**
//     * Test for MNT-11823: Upload of PPTX causes very high memory usage leading to system instability
//     * 
//     * @throws Exception
//     */
//    public void testProblemSlideShow() throws Exception
//    {
//        PoiMetadataExtracter extractor = (PoiMetadataExtracter) getExtracter();
//        configureExtractorLimits(extractor, ALL_MIMETYPES_FILTER, TIMEOUT_FOR_QUICK_EXTRACTION);
//
//        File problemSlideShowFile = AbstractContentTransformerTest.loadNamedQuickTestFile(PROBLEM_SLIDE_SHOW_DOCUMENT_NAME);
//        ContentReader sourceReader = new FileContentReader(problemSlideShowFile);
//        sourceReader.setMimetype(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
//
//        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//        extractor.extract(sourceReader, properties);
//
//        assertExtractedProperties(properties);
//        assertFalse("Reader was not closed", sourceReader.isChannelOpen());
//
//        extractor.setPoiExtractPropertiesOnly(false);
//        extractor.afterPropertiesSet();
//        properties = new HashMap<QName, Serializable>();
//        extractor.extract(sourceReader, properties);
//
//        assertFalse("Reader was not closed", sourceReader.isChannelOpen());
//        assertTrue(("Extraction completed successfully but failure is expected! Invalid properties are: " + properties), (null == properties) || properties.isEmpty());
//    }

    /**
     * Configures timeout for given <code>extractor</code> and <code>mimetypeFilter</code>
     * 
     * @param extractor - {@link PoiMetadataExtracter} instance
     * @param mimetypeFilter - {@link String} value which specifies mimetype filter for which timeout should be applied
     * @param timeout - {@link Long} value which specifies timeout for <code>mimetypeFilter</code>
     */
    private void configureExtractorLimits(PoiMetadataExtracter extractor, String mimetypeFilter, long timeout)
    {
        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeout);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits = new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(mimetypeFilter, limits);
        extractor.setMimetypeLimits(mimetypeLimits);
    }

    /**
     * Test for MNT-577: Alfresco is running 100% CPU for over 10 minutes while extracting metadata for Word office document
     * 
     * @throws Exception
     */
    public void testFootnotesLimitParameterUsing() throws Exception
    {
        PoiMetadataExtracter extractor = (PoiMetadataExtracter) getExtracter();

        File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(PROBLEM_FOOTNOTES_DOCUMENT_NAME);
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        long startTime = System.currentTimeMillis();
        extractor.extract(sourceReader, properties);
        long extractionTimeWithDefaultFootnotesLimit = System.currentTimeMillis() - startTime;

        assertExtractedProperties(properties);
        assertFalse("Reader was not closed", sourceReader.isChannelOpen());

        // Just let the extractor do the job...
        configureExtractorLimits(extractor, ALL_MIMETYPES_FILTER, IGNORABLE_TIMEOUT);
        extractor.setPoiFootnotesLimit(LARGE_FOOTNOTES_LIMIT);
        extractor.afterPropertiesSet();
        properties = new HashMap<QName, Serializable>();
        startTime = System.currentTimeMillis();
        extractor.extract(sourceReader, properties);
        long extractionTimeWithLargeFootnotesLimit = System.currentTimeMillis() - startTime;

        assertExtractedProperties(properties);
        assertTrue("The second metadata extraction operation must be longer!", extractionTimeWithLargeFootnotesLimit > extractionTimeWithDefaultFootnotesLimit);
        assertFalse("Reader was not closed", sourceReader.isChannelOpen());
    }

    /**
     * Asserts extracted <code>properties</code>. At least {@link PoiMetadataExtracterTest#MINIMAL_EXPECTED_PROPERTIES_AMOUNT} properties are expected:
     * {@link ContentModel#PROP_TITLE}, {@link ContentModel#PROP_AUTHOR} and {@link ContentModel#PROP_CREATED}
     * 
     * @param properties - {@link Map}&lt;{@link QName}, {@link Serializable}&gt; instance which contains all extracted properties
     */
    private void assertExtractedProperties(Map<QName, Serializable> properties)
    {
        assertNotNull("Properties were not extracted at all!", properties);
        assertFalse("Extracted properties are empty!", properties.isEmpty());
        assertTrue(("Expected 3 extracted properties but only " + properties.size() + " have been extracted!"), properties.size() >= MINIMAL_EXPECTED_PROPERTIES_AMOUNT);
        assertTrue(("'" + ContentModel.PROP_TITLE + "' property is missing!"), properties.containsKey(ContentModel.PROP_TITLE));
        assertTrue(("'" + ContentModel.PROP_AUTHOR + "' property is missing!"), properties.containsKey(ContentModel.PROP_AUTHOR));
        assertTrue(("'" + ContentModel.PROP_CREATED + "' property is missing!"), properties.containsKey(ContentModel.PROP_CREATED));
    }
}
