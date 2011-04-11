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
        dummyRegistry = new ContentTransformerRegistry();
        // create some dummy transformers for reliability tests
        new DummyTransformer(mimetypeService, dummyRegistry, A, B, 10L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, B, 10L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, C, 10L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, C, 10L);
        new DummyTransformer(mimetypeService, dummyRegistry, B, C, 10L);
        // create some dummy transformers for speed tests
        new DummyTransformer(mimetypeService, dummyRegistry, A, D, 20L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, D, 30L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, D, 10L);  // the fast one
        new DummyTransformer(mimetypeService, dummyRegistry, A, D, 25L);
        new DummyTransformer(mimetypeService, dummyRegistry, A, D, 25L);
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
        return registry.getTransformer(sourceMimetype, targetMimetype, options);
    }

    public void testNullRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        transformer = dummyRegistry.getTransformer(C, B, OPTIONS);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(C, A, OPTIONS);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(B, A, OPTIONS);
        assertNull("No transformer expected", transformer);
    }
    
    public void testSimpleRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // B -> C expect true
        transformer = dummyRegistry.getTransformer(B, C, OPTIONS);
        //transformer = dummyRegistry.getTransformer(B, C, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(B, C, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(C, B, OPTIONS));
    }
    
    /**
     * Force some equally reliant transformers to do some work and develop
     * different average transformation times.  Check that the registry
     * copes with the new averages after a reset.
     */
    public void testPerformanceRetrieval() throws Exception
    {
        // A -> D expect 1.0, 10ms
        ContentTransformer transformer1 = dummyRegistry.getTransformer(A, D, OPTIONS);
        assertTrue("Incorrect reliability", transformer1.isTransformable(A, D, OPTIONS));
        assertFalse("Incorrect reliability", transformer1.isTransformable(D, A, OPTIONS));
        assertEquals("Incorrect transformation time", 10L, transformer1.getTransformationTime());
        
        // A -> D has 10, 20, 25, 25, 30
        List<ContentTransformer> activeTransformers = dummyRegistry.getActiveTransformers(A, D, OPTIONS);
        assertEquals("Not all found", 5, activeTransformers.size());
        assertEquals("Incorrect order", 10L, activeTransformers.get(0).getTransformationTime());
        assertEquals("Incorrect order", 20L, activeTransformers.get(1).getTransformationTime());
        assertEquals("Incorrect order", 25L, activeTransformers.get(2).getTransformationTime());
        assertEquals("Incorrect order", 25L, activeTransformers.get(3).getTransformationTime());
        assertEquals("Incorrect order", 30L, activeTransformers.get(4).getTransformationTime());
        
        // Disable two of them, and re-test
        ((DummyTransformer)activeTransformers.get(2)).disable();
        ((DummyTransformer)activeTransformers.get(4)).disable();

        activeTransformers = dummyRegistry.getActiveTransformers(A, D, OPTIONS);
        assertEquals("Not all found", 3, activeTransformers.size());
        assertEquals("Incorrect order", 10L, activeTransformers.get(0).getTransformationTime());
        assertEquals("Incorrect order", 20L, activeTransformers.get(1).getTransformationTime());
        assertEquals("Incorrect order", 25L, activeTransformers.get(2).getTransformationTime());
    }
    
    public void testScoredRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // A -> B expect 0.6
        transformer = dummyRegistry.getTransformer(A, B, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(A, B, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(B, A, OPTIONS));
        // A -> C expect 1.0
        transformer = dummyRegistry.getTransformer(A, C, OPTIONS);
        assertNotNull("No transformer found", transformer);
        assertTrue("Incorrect reliability", transformer.isTransformable(A, C, OPTIONS));
        assertFalse("Incorrect reliability", transformer.isTransformable(C, A, OPTIONS));
    }
    
    /**
     * Set an explicit, and bizarre, transformation.  Check that it is used.
     *
     */
    public void testExplicitTransformation()
    {
        AbstractContentTransformer2 dummyTransformer = new DummyTransformer(
                mimetypeService,
                dummyRegistry,
                MimetypeMap.MIMETYPE_FLASH, MimetypeMap.MIMETYPE_EXCEL,
                12345);
        // set an explicit transformation
        ExplictTransformationDetails key =
            new ExplictTransformationDetails(
                        MimetypeMap.MIMETYPE_FLASH, 
                        MimetypeMap.MIMETYPE_EXCEL);
        dummyTransformer.setExplicitTransformations(Collections.singletonList(key));
        // register again
        dummyTransformer.register();
        
        // get the appropriate transformer for the bizarre mapping
        ContentTransformer checkTransformer = dummyRegistry.getTransformer(MimetypeMap.MIMETYPE_FLASH, MimetypeMap.MIMETYPE_EXCEL, OPTIONS);
        
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
                ContentTransformerRegistry registry,
                String sourceMimetype, String targetMimetype,
                long transformationTime)
        {
            super.setMimetypeService(mimetypeService);
            super.setRegistry(registry);
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.transformationTime = transformationTime;
            // register
            register();
        }
        
        protected void enable()
        {
            disable = false;
        }
        
        protected void disable()
        {
            disable = true;
        }

        public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
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
            super.recordTime(transformationTime);
        }

        /**
         * @return Returns the fixed dummy average transformation time
         */
        public synchronized long getTransformationTime()
        {
            return transformationTime;
        }
    }

    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
