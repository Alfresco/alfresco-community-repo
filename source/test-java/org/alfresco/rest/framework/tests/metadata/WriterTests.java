
package org.alfresco.rest.framework.tests.metadata;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.metadata.ResourceMetaDataWriter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test rest writer classes
 * 
 * @author Gethin James
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class WriterTests
{
    @Autowired
    ResourceLookupDictionary locator;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultMetaWriter")
    private ResourceMetaDataWriter defaultMetaWriter;

    private static Api api = Api.valueOf("alfrescomock", "private", "1");
    
    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
    }
    
    @Test
    public void testWriters() throws IOException
    {
        ResourceDictionary resourceDic = locator.getDictionary();
        Map<String, ResourceWithMetadata> apiResources = resourceDic.getAllResources().get(api);
        String writtenOut = testWriter(defaultMetaWriter, apiResources.get("/sheep"), apiResources);
        assertTrue(writtenOut.startsWith("{\"list\":{\"pagination\":{\"count\":4"));
        
//        ResourceMetaDataWriter wadlWriter = new WebScriptOptionsMetaData();
//        writtenOut = testWriter(wadlWriter, apiResources.get("/sheep"), apiResources);
//        assertTrue(writtenOut.startsWith("{\"list\":{\"pagination\":{\"count\":4"));
    }

    private String testWriter(ResourceMetaDataWriter defaultWriter,
                ResourceWithMetadata resourceWithMetadata,
                Map<String, ResourceWithMetadata> apiResources) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        defaultWriter.writeMetaData(out, apiResources.get("/sheep"), apiResources);
        String outResult = out.toString();
        System.out.println(outResult);
        return outResult;
    }
    

}
