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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter.OverwritePolicy;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @see org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter
 * 
 * @author Derek Hulley
 */
public class MappingMetadataExtracterTest extends TestCase
{
    private DummyMappingMetadataExtracter extracter;
    private ContentReader reader;
    private Map<QName, Serializable> destination;

    @Override
    protected void setUp() throws Exception
    {
        extracter = new DummyMappingMetadataExtracter();
        extracter.register();
        reader = new FileContentReader(AbstractContentTransformerTest.loadQuickTestFile("txt"));
        reader.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        destination = new HashMap<QName, Serializable>(7);
        destination.put(DummyMappingMetadataExtracter.QNAME_A1, JunkValue.INSTANCE);
        destination.put(DummyMappingMetadataExtracter.QNAME_A2, "");
        destination.put(DummyMappingMetadataExtracter.QNAME_B, null);
    }

    public void testSetUp()
    {
        assertNotNull(reader);
        assertNotNull(extracter);
        assertTrue("Extracter not initialized.", extracter.initCheck);
    }
    
    public void testDefaultExtract() throws Exception
    {
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(4, destination.size());
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A1));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A2));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_B));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_IMG));
    }
    
    public void testPropertyMappingOverride() throws Exception
    {
        Properties props = new Properties();
        props.put("namespace.prefix.my", DummyMappingMetadataExtracter.NAMESPACE_MY);
        props.put(DummyMappingMetadataExtracter.PROP_A, " my:a1, my:a2 ");
        extracter.setMappingProperties(props);
        extracter.register();
        // Only mapped 'a'
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(2, destination.size());
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A1));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A2));
    }
    
    public void testPropertyMappingMerge() throws Exception
    {
        Properties props = new Properties();
        props.put("namespace.prefix.my", DummyMappingMetadataExtracter.NAMESPACE_MY);
        props.put(DummyMappingMetadataExtracter.PROP_A, " my:a3 ");
        extracter.setMappingProperties(props);
        extracter.setInheritDefaultMapping(true);
        extracter.register();
        // Added a3
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(5, destination.size());
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A1));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A2));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A3));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_B));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_IMG));
    }
    
    public void testPropertyMappingOverrideExtra() throws Exception
    {
        Properties props = new Properties();
        props.put("namespace.prefix.my", DummyMappingMetadataExtracter.NAMESPACE_MY);
        props.put(DummyMappingMetadataExtracter.PROP_C, " my:c ");
        props.put(DummyMappingMetadataExtracter.PROP_D, " my:d ");
        props.put(DummyMappingMetadataExtracter.PROP_E, " my:e ");
        extracter.setMappingProperties(props);
        extracter.register();
        // Added a3
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(2, destination.size());
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_C));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_D));
    }
    
    public void testOverwritePolicyEager()
    {
        extracter.setOverwritePolicy(OverwritePolicy.EAGER);
        extracter.extract(reader, destination);
        assertEquals(4, destination.size());
        assertEquals(DummyMappingMetadataExtracter.VALUE_A, destination.get(DummyMappingMetadataExtracter.QNAME_A1));
        assertEquals(DummyMappingMetadataExtracter.VALUE_A, destination.get(DummyMappingMetadataExtracter.QNAME_A2));
        assertEquals(DummyMappingMetadataExtracter.VALUE_B, destination.get(DummyMappingMetadataExtracter.QNAME_B));
        assertEquals(DummyMappingMetadataExtracter.VALUE_IMG, destination.get(DummyMappingMetadataExtracter.QNAME_IMG));
    }
    
    public void testOverwritePolicyPragmatic()
    {
        extracter.setOverwritePolicy(OverwritePolicy.PRAGMATIC);
        
        // Put some values in to start with
        destination.put(DummyMappingMetadataExtracter.QNAME_C, "Will not change");
        destination.put(DummyMappingMetadataExtracter.QNAME_IMG, "Will be changed");
        
        // Extract
        extracter.extract(reader, destination);
        assertEquals(5, destination.size());
        
        // Check the values as extracted
        assertEquals(JunkValue.INSTANCE, destination.get(DummyMappingMetadataExtracter.QNAME_A1));
        assertEquals(DummyMappingMetadataExtracter.VALUE_A, destination.get(DummyMappingMetadataExtracter.QNAME_A2));
        assertEquals(DummyMappingMetadataExtracter.VALUE_B, destination.get(DummyMappingMetadataExtracter.QNAME_B));

        // Normal values not changed
        assertEquals("Will not change", destination.get(DummyMappingMetadataExtracter.QNAME_C));
        
        // Media parts are always overridden
        assertEquals(DummyMappingMetadataExtracter.VALUE_IMG, destination.get(DummyMappingMetadataExtracter.QNAME_IMG));
    }
    
    public void testOverwritePolicyPrudent()
    {
        extracter.setOverwritePolicy(OverwritePolicy.PRUDENT);
        
        // Add a media property, won't be changed
        destination.put(DummyMappingMetadataExtracter.QNAME_IMG, "Won't be changed");
        
        // Extract and check
        extracter.extract(reader, destination);
        assertEquals(4, destination.size());
        assertEquals(JunkValue.INSTANCE, destination.get(DummyMappingMetadataExtracter.QNAME_A1));
        assertEquals(DummyMappingMetadataExtracter.VALUE_A, destination.get(DummyMappingMetadataExtracter.QNAME_A2));
        assertEquals(DummyMappingMetadataExtracter.VALUE_B, destination.get(DummyMappingMetadataExtracter.QNAME_B));
        
        // Media behaves the same as the others
        assertEquals("Won't be changed", destination.get(DummyMappingMetadataExtracter.QNAME_IMG));
    }
    
    public void testOverwritePolicyCautious()
    {
        extracter.setOverwritePolicy(OverwritePolicy.CAUTIOUS);
        extracter.extract(reader, destination);
        assertEquals(4, destination.size());
        assertEquals(JunkValue.INSTANCE, destination.get(DummyMappingMetadataExtracter.QNAME_A1));
        assertEquals("", destination.get(DummyMappingMetadataExtracter.QNAME_A2));
        assertEquals(null, destination.get(DummyMappingMetadataExtracter.QNAME_B));
    }
    
    /**
     * A spoofed-up extracter that extracts the following:
     * <pre>
     * <b>a:</b>  - A        -->  my:a1, my:a2
     * <b>b:</b>  - B        -->  my:b
     * <b>c:</b>  - C
     * <b>d:</b>  - D
     * </pre>
     * @author Derek Hulley
     */
    public static class DummyMappingMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        public static final String PROP_A = "a";
        public static final String PROP_B = "b";
        public static final String PROP_C = "c";
        public static final String PROP_D = "d";
        public static final String PROP_E = "e";
        public static final String PROP_IMG = "exif";
        public static final String VALUE_A = "AAA";
        public static final String VALUE_B = "BBB";
        public static final String VALUE_C = "CCC";
        public static final String VALUE_D = "DDD";
        public static final String VALUE_IMG = "IMAGE";
        
        public static final String NAMESPACE_MY = "http://DummyMappingMetadataExtracter";
        public static final QName QNAME_A1 = QName.createQName(NAMESPACE_MY, "a1");
        public static final QName QNAME_A2 = QName.createQName(NAMESPACE_MY, "a2");
        public static final QName QNAME_A3 = QName.createQName(NAMESPACE_MY, "a3");
        public static final QName QNAME_B = QName.createQName(NAMESPACE_MY, "b");
        public static final QName QNAME_C = QName.createQName(NAMESPACE_MY, "c");
        public static final QName QNAME_D = QName.createQName(NAMESPACE_MY, "d");
        public static final QName QNAME_E = QName.createQName(NAMESPACE_MY, "e");   // not extracted
        public static final QName QNAME_IMG = QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "test");
        private static final Set<String> MIMETYPES;
        static
        {
            MIMETYPES = new HashSet<String>(5);
            MIMETYPES.add(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            MIMETYPES.add(MimetypeMap.MIMETYPE_XML);
        }
        
        Map<String, Set<QName>> defaultMapping;
        private boolean initCheck;
        
        public DummyMappingMetadataExtracter()
        {
            super(MIMETYPES);
            initCheck = false;
        }
        
        @Override
        protected void init()
        {
            defaultMapping = new HashMap<String, Set<QName>>(7);
            defaultMapping.put(PROP_A, new HashSet<QName>(Arrays.asList(QNAME_A1, QNAME_A2)));
            defaultMapping.put(PROP_B, new HashSet<QName>(Arrays.asList(QNAME_B)));
            defaultMapping.put(PROP_IMG, new HashSet<QName>(Arrays.asList(QNAME_IMG)));
            
            initCheck = true;
            
            super.init();
        }

        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            return defaultMapping;
        }
        
        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader)
        {
            reader.getContentString();
            
            Map<String, Serializable> ret = new HashMap<String, Serializable>(7);
            ret.put(PROP_A, VALUE_A);
            ret.put(PROP_B, VALUE_B);
            ret.put(PROP_C, VALUE_C);
            ret.put(PROP_D, VALUE_D);
            ret.put(PROP_IMG, VALUE_IMG);
            return ret;
        }
    }
    
    private static class JunkValue implements Serializable
    {
        private static final JunkValue INSTANCE = new JunkValue();
        private static final long serialVersionUID = 1L;
    }
}
