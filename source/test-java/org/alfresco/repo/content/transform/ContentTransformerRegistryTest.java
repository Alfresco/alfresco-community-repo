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

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.ContentTransformerRegistry
 * 
 * @author Derek Hulley
 */
public class ContentTransformerRegistryTest extends AbstractContentTransformerTest
{
    private static final String A = MimetypeMap.MIMETYPE_TEXT_PLAIN;
    private static final String B = MimetypeMap.MIMETYPE_XML;
    private static final String C = MimetypeMap.MIMETYPE_WORD;
    private static final String D = MimetypeMap.MIMETYPE_HTML;

    private static final TransformationOptions OPTIONS = new TransformationOptions();
    
    /** a real registry with real transformers */
    private ContentTransformerRegistry registry;
    /** a fake registry with fake transformers */
    private ContentTransformerRegistry dummyRegistry;
    
    private ContentReader reader;
    private ContentWriter writer;
    
    private DummyTransformer ad20;
    private DummyTransformer ad30;
    private DummyTransformer ad10;
    private DummyTransformer ad25a;
    private DummyTransformer ad25b;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        registry = (ContentTransformerRegistry) ctx.getBean("contentTransformerRegistry");
        
        reader = new FileContentReader(TempFileProvider.createTempFile(getName(), ".txt"));
        reader.setMimetype(A);
        writer = new FileContentWriter(TempFileProvider.createTempFile(getName(), ".txt"));
        writer.setMimetype(D);
        
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++)
        {
            bytes[i] = (byte)i;
        }
        // create the dummyRegistry
        TransformerSelectorImpl transformerSelector = new TransformerSelectorImpl();
        transformerSelector.setTransformerConfig(transformerConfig);
        transformerSelector.setContentTransformerRegistry(dummyRegistry);
        dummyRegistry = new ContentTransformerRegistry(transformerSelector);
        transformerSelector.setContentTransformerRegistry(dummyRegistry);
        // create some dummy transformers for reliability tests
        new DummyTransformer(mimetypeService, "transformer.testAB10a", transformerDebug, transformerConfig, dummyRegistry, A, B, 10L);
        new DummyTransformer(mimetypeService, "transformer.testAB10b", transformerDebug, transformerConfig, dummyRegistry, A, B, 10L);
        new DummyTransformer(mimetypeService, "transformer.testAC10a", transformerDebug, transformerConfig, dummyRegistry, A, C, 10L);
        new DummyTransformer(mimetypeService, "transformer.testAC10b", transformerDebug, transformerConfig, dummyRegistry, A, C, 10L);
        new DummyTransformer(mimetypeService, "transformer.testBC10",  transformerDebug, transformerConfig, dummyRegistry, B, C, 10L);
        // create some dummy transformers for speed tests
        ad20 = new DummyTransformer(mimetypeService, "transformer.testAD20",  transformerDebug, transformerConfig, dummyRegistry, A, D, 20L);
        ad30 = new DummyTransformer(mimetypeService, "transformer.testAD30",  transformerDebug, transformerConfig, dummyRegistry, A, D, 30L);
        ad10 = new DummyTransformer(mimetypeService, "transformer.testAD10",  transformerDebug, transformerConfig, dummyRegistry, A, D, 10L);  // the fast one
        ad25a = new DummyTransformer(mimetypeService, "transformer.testAD25a", transformerDebug, transformerConfig, dummyRegistry, A, D, 25L);
        ad25b = new DummyTransformer(mimetypeService, "transformer.testAD25b", transformerDebug, transformerConfig, dummyRegistry, A, D, 25L);
    }

    /**
     * Checks that required objects are present
     */
    public void testSetUp() throws Exception
    {
        super.testSetUp();
        assertNotNull(registry);
    }

    /**
     * @return Returns the transformer provided by the <b>real</b> registry
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return registry.getTransformer(sourceMimetype, -1, targetMimetype, options);
    }

    public void testNullRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        transformer = dummyRegistry.getTransformer(C, -1, B, OPTIONS);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(C, -1, A, OPTIONS);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(B, -1, A, OPTIONS);
        assertNull("No transformer expected", transformer);
    }
    
    public void testSimpleRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // B -> C expect true
        transformer = dummyRegistry.getTransformer(B, -1, C, OPTIONS);
        //transformer = dummyRegistry.getTransformer(B, C, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(B, -1, C, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(C, -1, B, OPTIONS));
    }
    
    /**
     * Force some equally reliant transformers to do some work and develop
     * different average transformation times.  Check that the registry
     * copes with the new averages after a reset.
     */
    public void testPerformanceRetrieval() throws Exception
    {
        // Until the threshold (3) is reached by each transformer with the same priority it will
        // be tried that many times in the order defined. 20, 30, 10, 25a, 25b
        for (int i=1; i<=3; i++)
        {
            long expectedTime = i == 1 ? 0L : 20L;
            ContentTransformer transformer1 = dummyRegistry.getTransformer(A, -1, D, OPTIONS);
            assertEquals(i+" incorrect transformation time", expectedTime, transformer1.getTransformationTime(A, D));
            ad20.transformInternal(null, null, null);

            expectedTime = i == 1 ? 0L : 30L;
            transformer1 = dummyRegistry.getTransformer(A, -1, D, OPTIONS);
            assertEquals(i+" incorrect transformation time", expectedTime, transformer1.getTransformationTime(A, D));
            ad30.transformInternal(null, null, null);

            ad10.transformInternal(null, null, null);
            ad25a.transformInternal(null, null, null);
            ad25b.transformInternal(null, null, null);
        }
        
        // Now the average times are set up, it should find the fastest one
        
        // A -> D expect 1.0, 10ms
        ContentTransformer transformer1 = dummyRegistry.getTransformer(A, -1, D, OPTIONS);
        assertTrue("Incorrect reliability", transformer1.isTransformable(A, -1, D, OPTIONS));
        assertFalse("Incorrect reliability", transformer1.isTransformable(D, -1, A, OPTIONS));
        assertEquals("Incorrect transformation time", 10L, transformer1.getTransformationTime(A, D));
        
        // A -> D has 10, 20, 25, 25, 30
        List<ContentTransformer> activeTransformers = dummyRegistry.getActiveTransformers(A, -1, D, OPTIONS);
        assertEquals("Not all found", 5, activeTransformers.size());
        assertEquals("Incorrect order", 10L, activeTransformers.get(0).getTransformationTime(A, D));
        assertEquals("Incorrect order", 20L, activeTransformers.get(1).getTransformationTime(A, D));
        assertEquals("Incorrect order", 25L, activeTransformers.get(2).getTransformationTime(A, D));
        assertEquals("Incorrect order", 25L, activeTransformers.get(3).getTransformationTime(A, D));
        assertEquals("Incorrect order", 30L, activeTransformers.get(4).getTransformationTime(A, D));
        
        // Disable two of them, and re-test
        ((DummyTransformer)activeTransformers.get(2)).disable();
        ((DummyTransformer)activeTransformers.get(4)).disable();

        activeTransformers = dummyRegistry.getActiveTransformers(A, -1, D, OPTIONS);
        assertEquals("Not all found", 3, activeTransformers.size());
        assertEquals("Incorrect order", 10L, activeTransformers.get(0).getTransformationTime(A, D));
        assertEquals("Incorrect order", 20L, activeTransformers.get(1).getTransformationTime(A, D));
        assertEquals("Incorrect order", 25L, activeTransformers.get(2).getTransformationTime(A, D));
    }
    
    public void testScoredRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // A -> B expect 0.6
        transformer = dummyRegistry.getTransformer(A, -1, B, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(A, -1, B, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(B, -1, A, OPTIONS));
        // A -> C expect 1.0
        transformer = dummyRegistry.getTransformer(A, -1, C, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(A, -1, C, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(C, -1, A, OPTIONS));
    }
    
    /**
     * Set an explicit, and bizarre, transformation.  Check that it is used.
     *
     */
    public void testExplicitTransformation()
    {
        AbstractContentTransformer2 dummyTransformer = new DummyTransformer(
                mimetypeService,
                "transformer.testExplicit",
                transformerDebug, transformerConfig,
                dummyRegistry, MimetypeMap.MIMETYPE_FLASH, MimetypeMap.MIMETYPE_EXCEL, 12345);
        // set an explicit transformation
        ExplictTransformationDetails key =
            new ExplictTransformationDetails(
                        MimetypeMap.MIMETYPE_FLASH, 
                        MimetypeMap.MIMETYPE_EXCEL);
        dummyTransformer.setExplicitTransformations(Collections.singletonList(key));
        // register again
        dummyTransformer.register();
        
        // get the appropriate transformer for the bizarre mapping
        ContentTransformer checkTransformer = dummyRegistry.getTransformer(MimetypeMap.MIMETYPE_FLASH, -1, MimetypeMap.MIMETYPE_EXCEL, OPTIONS);
        
        assertNotNull("No explicit transformer found", checkTransformer);
        assertTrue("Expected explicit transformer", dummyTransformer == checkTransformer);
    }
    
    /**
     * Dummy transformer that does no transformation and scores exactly as it is
     * told to in the constructor.  It enables the tests to be sure of what to expect.
     */
    private static class DummyTransformer extends AbstractContentTransformer2
    {
        private String sourceMimetype;
        private String targetMimetype;
        private long transformationTime;
        private boolean disable = false;
        
        public DummyTransformer(
                MimetypeService mimetypeService,
                String name,
                TransformerDebug transformerDebug, TransformerConfig transformerConfig,
                ContentTransformerRegistry registry, String sourceMimetype, String targetMimetype, long transformationTime)
        {
            super.setMimetypeService(mimetypeService);
            super.setTransformerDebug(transformerDebug);
            super.setTransformerConfig(transformerConfig);
            super.setRegistry(registry);
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.transformationTime = transformationTime;
            setRegisterTransformer(true);
            setBeanName(name+'.'+System.currentTimeMillis()%100000);

            // register
            register();
        }
        
        protected void disable()
        {
            disable = true;
        }

        public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
        {
            if (disable) {
                return false;
            }
            
            if (this.sourceMimetype.equals(sourceMimetype)
                    && this.targetMimetype.equals(targetMimetype))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * Just notches up some average times
         */
        public void transformInternal(
                ContentReader reader,
                ContentWriter writer,
                TransformationOptions options) throws Exception
        {
            // just update the transformation time
            super.recordTime(sourceMimetype, targetMimetype, transformationTime);
        }
    }

    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
