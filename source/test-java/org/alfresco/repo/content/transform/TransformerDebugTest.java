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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockMimetypes;
import static org.alfresco.repo.content.transform.TransformerLogTest.assertLogEntriesEquals;
import static org.alfresco.repo.content.transform.TransformerDebugLogTest.assertDebugEntriesEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerDebug.
 * 
 * @author Alan Davis
 */
public class TransformerDebugTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private MimetypeService mimetypeService; 

    @Mock
    private ContentTransformerRegistry transformerRegistry;

    @Mock
    private TransformerConfig transformerConfig;

    @Mock
    private TransformationOptions options;
    
    @Mock
    private AbstractContentTransformerLimits transformer1;
    
    @Mock
    private AbstractContentTransformerLimits transformer2;
    
    @Mock
    private AbstractContentTransformerLimits transformer3;
    
    @Mock
    private AbstractContentTransformerLimits transformer4;
    
    private TransformerDebug transformerDebug;
    
    private TransformerLog log;

    private TransformerDebugLog debug;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        log = new TransformerLog();
        debug = new TransformerDebugLog();

        when(transformerConfig.getProperty("transformer.log.entries")).thenReturn("10");
        when(transformerConfig.getProperty("transformer.debug.entries")).thenReturn("10");

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "text/plain",      "txt");
        
        when(transformer1.getName()).thenReturn("transformer1");
        when(transformer2.getName()).thenReturn("transformer2");
        when(transformer3.getName()).thenReturn("transformer3");
        when(transformer4.getName()).thenReturn("transformer4");

        transformerDebug = new TransformerDebug(nodeService, mimetypeService, transformerRegistry, transformerConfig, log, debug);

        log.setTransformerDebug(transformerDebug);
        log.setTransformerConfig(transformerConfig);

        debug.setTransformerDebug(transformerDebug);
        debug.setTransformerConfig(transformerConfig);
    }

    // Replaces any times with " NN ms" before checking
    private String[] untimed(String[] actual)
    {
        for (int i = actual.length-1; i >= 0; i--)
        {
            actual[i] = actual[i].replaceAll(" \\d+ ms", " NN ms");
        }
        return actual;
    }

    @Test
    public void alf18373Test()
    {
        long sourceSize = 1024*1024*3/2;
        
        transformerDebug.pushAvailable("sourceUrl", "application/pdf", "text/plain", options);
        
        transformerDebug.unavailableTransformer(transformer1, "application/pdf", "text/plain", 50);
        transformerDebug.unavailableTransformer(transformer2, "application/pdf", "text/plain", 0);
        transformerDebug.unavailableTransformer(transformer3, "application/pdf", "text/plain", 50);
        transformerDebug.unavailableTransformer(transformer4, "application/pdf", "text/plain", 50);
        
        List<ContentTransformer> transformers = Arrays.asList(new ContentTransformer[] {});
        
        transformerDebug.availableTransformers(transformers, sourceSize, options, "ContentService.transform(...)");

        transformerDebug.popAvailable();
        
        // Prior to the fix the following we returned:
        //   "0             pdf  txt  1.5 MB ContentService.transform(...) NO transformers\n"+
        //   "0             --b) [---] transformer1<<Component>> > 50 KB\n"+
        //   "0             --c) [---] transformer4<<Component>> > 50 KB\n"+
        //   "0             --d) [---] transformer3<<Component>> > 50 KB\n"+
        //   "0             Finished in NN ms Transformer NOT called\n"
        //   
        //   "0 pdf  txt  WARN  1.5 MB NN ms No transformers as file is > 0 bytes"
        assertDebugEntriesEquals(new String[] {
        "0             pdf  txt  1.5 MB ContentService.transform(...) NO transformers\n"+
        "0             --a) [---] transformer1<<Component>> > 50 KB\n"+
        "0             --b) [---] transformer4<<Component>> > 50 KB\n"+
        "0             --c) [---] transformer3<<Component>> > 50 KB\n"+
        "0             Finished in NN ms Transformer NOT called\n"}, untimed(debug.getEntries(10)));
        assertLogEntriesEquals(new String[] {
        "0 pdf  txt  WARN  1.5 MB NN ms No transformers as file is > 50 KB"}, untimed(log.getEntries(10)));
    }
}
