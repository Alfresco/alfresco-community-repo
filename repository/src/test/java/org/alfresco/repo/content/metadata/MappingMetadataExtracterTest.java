/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;

import org.alfresco.MiscContextTestSuite;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.metadata.MetadataExtracter.OverwritePolicy;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;

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

    /** Test the new alfresco/metadata properties location */
    public void testSetUpPropertiesLocationMetadata()
    {
        DummyPropertiesInMetadataLocationMappingMetadataExtracter metadataLocationExtracter = new DummyPropertiesInMetadataLocationMappingMetadataExtracter();
        metadataLocationExtracter.register();
        assertNotNull("Extracter not initialized.", metadataLocationExtracter.getMapping());
        assertNotNull("Mapping not found",
                metadataLocationExtracter.getMapping().get(DummyMappingMetadataExtracter.PROP_A));
    }

    /** Test that the old package-based properties location still works */
    public void testSetUpPropertiesLocationPackage()
    {
        DummyPropertiesInPackageLocationMappingMetadataExtracter packageLocationExtracter = new DummyPropertiesInPackageLocationMappingMetadataExtracter();
        packageLocationExtracter.register();
        assertNotNull("Extracter not initialized.", packageLocationExtracter.getMapping());
        assertNotNull("Mapping not found",
                packageLocationExtracter.getMapping().get(DummyMappingMetadataExtracter.PROP_A));
    }

    /** Test that an extract with missing location throws the correct error */
    public void testSetUpPropertiesMissing()
    {
        DummyPropertiesMissingMappingMetadataExtracter propertiesMissingExtracter = new DummyPropertiesMissingMappingMetadataExtracter();
        try
        {
            propertiesMissingExtracter.register();
        }
        catch (AlfrescoRuntimeException e)
        {
            assertTrue(e.getMessage().contains("alfresco/metadata/"));
        }
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

    public void testPropertyMappingGlobalOverride() throws Exception
    {
        String propertyPrefix = AbstractMappingMetadataExtracter.PROPERTY_PREFIX_METADATA +
                DummyMappingMetadataExtracter.EXTRACTER_NAME +
                AbstractMappingMetadataExtracter.PROPERTY_COMPONENT_EXTRACT;

        ApplicationContext ctx = MiscContextTestSuite.getMinimalContext();
        Properties globalProperties = (Properties) ctx.getBean("global-properties");
        globalProperties.setProperty(
                propertyPrefix + "namespace.prefix.my",
                DummyMappingMetadataExtracter.NAMESPACE_MY);
        globalProperties.setProperty(
                propertyPrefix + DummyMappingMetadataExtracter.PROP_A,
                " my:a1, my:a2, my:c ");

        extracter.setApplicationContext(ctx);

        extracter.register();
        // Only mapped 'a'
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(DummyMappingMetadataExtracter.VALUE_A, destination.get(DummyMappingMetadataExtracter.QNAME_C));
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
        assertEquals(3, destination.size());
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_A3));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_B));
        assertTrue(destination.containsKey(DummyMappingMetadataExtracter.QNAME_IMG));
    }

    public void testPropertyMappingDisable() throws Exception
    {
        Properties props = new Properties();
        props.put("namespace.prefix.my", DummyMappingMetadataExtracter.NAMESPACE_MY);
        props.put(DummyMappingMetadataExtracter.PROP_A, "");
        extracter.setMappingProperties(props);
        extracter.setInheritDefaultMapping(true);
        extracter.register();
        // Added a3
        destination.clear();
        extracter.extract(reader, destination);
        assertEquals(2, destination.size());
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
     * @see <a href="https://issues.alfresco.com/jira/browse/MNT-13919">MNT-13919</a>
     */
    public void testEmbedSupportDifferentFromExtract()
    {
        DummyMetadataEmbedder embedder = new DummyMetadataEmbedder();
        Map<QName, Serializable> propertiesToEmbed = new HashMap<QName, Serializable>();

        // make a writer for the target of the embed, we won't actually use it
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_embed",
                ".txt");
        FileContentWriter writer = new FileContentWriter(targetFile);
        writer.setMimetype(DummyMetadataEmbedder.MIMETYPE_EMBEDDABLE);

        try
        {
            embedder.embed(propertiesToEmbed, reader, writer);
        }
        catch (AlfrescoRuntimeException e)
        {
            if (e.getMessage().contains("Metadata extracter does not support embedding mimetype"))
            {
                fail("Embed mimetype should not be tied to extracter's extract mimetypes");
            }
            else
            {
                fail(e.getMessage());
            }
        }
        finally
        {
            if (targetFile != null && targetFile.exists())
            {
                targetFile.delete();
            }
        }
    }

    /**
     * A spoofed-up extracter that extracts the following:
     * 
     * <pre>
     * <b>a:</b>  - A        -->  my:a1, my:a2
     * <b>b:</b>  - B        -->  my:b
     * <b>c:</b>  - C
     * <b>d:</b>  - D
     * </pre>
     * 
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

        public static final String EXTRACTER_NAME = "extracter.Dummy";
        public static final String NAMESPACE_MY = "http://DummyMappingMetadataExtracter";
        public static final QName QNAME_A1 = QName.createQName(NAMESPACE_MY, "a1");
        public static final QName QNAME_A2 = QName.createQName(NAMESPACE_MY, "a2");
        public static final QName QNAME_A3 = QName.createQName(NAMESPACE_MY, "a3");
        public static final QName QNAME_B = QName.createQName(NAMESPACE_MY, "b");
        public static final QName QNAME_C = QName.createQName(NAMESPACE_MY, "c");
        public static final QName QNAME_D = QName.createQName(NAMESPACE_MY, "d");
        public static final QName QNAME_E = QName.createQName(NAMESPACE_MY, "e"); // not extracted
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
            setBeanName(EXTRACTER_NAME);
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

    public static class DummyPropertiesInPackageLocationMappingMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            return null;
        }
    }

    public static class DummyPropertiesInMetadataLocationMappingMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            return null;
        }
    }

    public static class DummyPropertiesMissingMappingMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            return null;
        }
    }

    private static class JunkValue implements Serializable
    {
        private static final JunkValue INSTANCE = new JunkValue();
        private static final long serialVersionUID = 1L;
    }

    /**
     * Mock metadata embedder which has a set of supported embed mimetypes different than the supported extract mimetypes.
     */
    private class DummyMetadataEmbedder extends AbstractMappingMetadataExtracter
    {
        private static final String MIMETYPE_EXTRACTABLE = "extractableMimetype";
        private static final String MIMETYPE_EMBEDDABLE = "embeddableMimetype";

        public DummyMetadataEmbedder()
        {
            super(Collections.singleton(MIMETYPE_EXTRACTABLE),
                    Collections.singleton(MIMETYPE_EMBEDDABLE));
            init();
        }

        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            return null;
        }
    }
}
