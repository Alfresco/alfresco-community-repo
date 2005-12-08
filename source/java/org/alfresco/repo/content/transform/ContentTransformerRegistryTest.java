/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
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
    
    /** a real registry with real transformers */
    private ContentTransformerRegistry registry;
    /** a fake registry with fake transformers */
    private ContentTransformerRegistry dummyRegistry;
    
    private ContentReader reader;
    private ContentWriter writer;
    
    /**
     * Allows dependency injection
     */
    public void setContentTransformerRegistry(ContentTransformerRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void onSetUpInTransaction() throws Exception
    {
        reader = new FileContentReader(TempFileProvider.createTempFile(getName(), ".txt"));
        reader.setMimetype(A);
        writer = new FileContentWriter(TempFileProvider.createTempFile(getName(), ".txt"));
        writer.setMimetype(D);
        
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++)
        {
            bytes[i] = (byte)i;
        }
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(5);
        // create some dummy transformers for reliability tests
        transformers.add(new DummyTransformer(A, B, 0.3, 10L));
        transformers.add(new DummyTransformer(A, B, 0.6, 10L));
        transformers.add(new DummyTransformer(A, C, 0.5, 10L));
        transformers.add(new DummyTransformer(A, C, 1.0, 10L));
        transformers.add(new DummyTransformer(B, C, 0.2, 10L));
        // create some dummy transformers for speed tests
        transformers.add(new DummyTransformer(A, D, 1.0, 20L));
        transformers.add(new DummyTransformer(A, D, 1.0, 20L));
        transformers.add(new DummyTransformer(A, D, 1.0, 10L));  // the fast one
        transformers.add(new DummyTransformer(A, D, 1.0, 20L));
        transformers.add(new DummyTransformer(A, D, 1.0, 20L));
        // create the dummyRegistry
        dummyRegistry = new ContentTransformerRegistry(mimetypeMap);
        dummyRegistry.setTransformers(transformers);
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
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return registry.getTransformer(sourceMimetype, targetMimetype);
    }

    public void testNullRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        transformer = dummyRegistry.getTransformer(C, B);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(C, A);
        assertNull("No transformer expected", transformer);
        transformer = dummyRegistry.getTransformer(B, A);
        assertNull("No transformer expected", transformer);
    }
    
    public void testSimpleRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // B -> C expect 0.2
        transformer = dummyRegistry.getTransformer(B, C);
        transformer = dummyRegistry.getTransformer(B, C);
        assertNotNull("No transformer found", transformer);
        assertEquals("Incorrect reliability", 0.2, transformer.getReliability(B, C));
        assertEquals("Incorrect reliability", 0.0, transformer.getReliability(C, B));
    }
    
    /**
     * Force some equally reliant transformers to do some work and develop
     * different average transformation times.  Check that the registry
     * copes with the new averages after a reset.
     */
    public void testPerformanceRetrieval() throws Exception
    {
        // A -> D expect 1.0, 10ms
        ContentTransformer transformer1 = dummyRegistry.getTransformer(A, D);
        assertEquals("Incorrect reliability", 1.0, transformer1.getReliability(A, D));
        assertEquals("Incorrect reliability", 0.0, transformer1.getReliability(D, A));
        assertEquals("Incorrect transformation time", 10L, transformer1.getTransformationTime());
    }
    
    public void testScoredRetrieval() throws Exception
    {
        ContentTransformer transformer = null;
        // A -> B expect 0.6
        transformer = dummyRegistry.getTransformer(A, B);
        assertNotNull("No transformer found", transformer);
        assertEquals("Incorrect reliability", 0.6, transformer.getReliability(A, B));
        assertEquals("Incorrect reliability", 0.0, transformer.getReliability(B, A));
        // A -> C expect 1.0
        transformer = dummyRegistry.getTransformer(A, C);
        assertNotNull("No transformer found", transformer);
        assertEquals("Incorrect reliability", 1.0, transformer.getReliability(A, C));
        assertEquals("Incorrect reliability", 0.0, transformer.getReliability(C, A));
    }
    
    /**
     * Set an explicit, and bizarre, transformation.  Check that it is used.
     *
     */
    public void testExplicitTransformation()
    {
        ContentTransformer dummyTransformer = new DummyTransformer(
                MimetypeMap.MIMETYPE_FLASH, MimetypeMap.MIMETYPE_EXCEL, 1.0, 12345);
        
        List<Object> transform = new ArrayList<Object>(3);
        transform.add(MimetypeMap.MIMETYPE_FLASH);
        transform.add(MimetypeMap.MIMETYPE_EXCEL);
        transform.add(dummyTransformer);
        
        List<List<Object>> explicitTransformers = Collections.singletonList(transform);
        // add it to the registry
        dummyRegistry.setExplicitTransformations(explicitTransformers);
        
        // get the appropriate transformer for the bizarre mapping
        ContentTransformer checkTransformer = dummyRegistry.getTransformer(
                MimetypeMap.MIMETYPE_FLASH, MimetypeMap.MIMETYPE_EXCEL);
        
        assertNotNull("No explicit transformer found", checkTransformer);
        assertTrue("Expected explicit transformer", dummyTransformer == checkTransformer);
    }
    
    /**
     * Dummy transformer that does no transformation and scores exactly as it is
     * told to in the constructor.  It enables the tests to be sure of what to expect.
     */
    private static class DummyTransformer extends AbstractContentTransformer
    {
        private String sourceMimetype;
        private String targetMimetype;
        private double reliability;
        private long transformationTime;
        
        public DummyTransformer(String sourceMimetype, String targetMimetype,
                double reliability, long transformationTime)
        {
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.reliability = reliability;
            this.transformationTime = transformationTime;
        }

        public double getReliability(String sourceMimetype, String targetMimetype)
        {
            if (this.sourceMimetype.equals(sourceMimetype)
                    && this.targetMimetype.equals(targetMimetype))
            {
                return reliability;
            }
            else
            {
                return 0.0;
            }
        }

        /**
         * Just notches up some average times
         */
        public void transformInternal(
                ContentReader reader,
                ContentWriter writer,
                Map<String, Object> options) throws Exception
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
}
