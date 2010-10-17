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
package org.alfresco.repo.audit;

import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelException;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.audit.model.AuditApplication.DataExtractorDefinition;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PathMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * Tests that auditing is loaded properly on repository startup.
 * 
 * @see AuditBootstrap
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditBootstrapTest extends TestCase
{
    private static final String APPLICATION_TEST = "Alfresco Test";
    private static final String KEY_TEST = "test";
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private static final Log logger = LogFactory.getLog(AuditBootstrapTest.class);
    
    private AuditModelRegistryImpl auditModelRegistry;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistryImpl) ctx.getBean("auditModel.modelRegistry");
        auditModelRegistry.setProperty(AuditModelRegistryImpl.PROPERTY_AUDIT_CONFIG_STRICT, Boolean.TRUE.toString());
        
        // Register a new model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
    }
            
    @Override
    protected void tearDown() throws Exception
    {
        // Throw away the reconfigured registry state
        auditModelRegistry.setProperty(AuditModelRegistryImpl.PROPERTY_AUDIT_CONFIG_STRICT, Boolean.FALSE.toString());
        auditModelRegistry.destroy();
    }

    public void testSetUp()
    {
        // Just here to fail if the basic startup fails
    }
    
    private void loadBadModel(String url) throws Exception
    {
        try
        {
            URL testModelUrl = ResourceUtils.getURL(url);
            auditModelRegistry.registerModel(testModelUrl);
            auditModelRegistry.loadAuditModels();
            fail("Expected model loading to fail.");
        }
        catch (AuditModelException e)
        {
            // Expected
            logger.error("Expected AuditModelException: " + e.getMessage());
        }
    }
    
    public void testModelLoading_NoDataExtractor() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-01.xml");
    }
    
    public void testModelLoading_NoDataGenerator() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-02.xml");
    }
    
    public void testModelLoading_DuplicatePath() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-03.xml");
    }
    
    public void testModelLoading_UppercasePath() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-04.xml");
    }
    
    public void testModelLoading_InvalidDataGeneratorName() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-05.xml");
    }
    
    public void testModelLoading_BadGeneratorRegisteredName() throws Exception
    {
        loadBadModel("classpath:alfresco/testaudit/alfresco-audit-test-bad-06.xml");
    }
    
    public void testGetApplicationId()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByName(APPLICATION_TEST);
        assertNotNull(app);
        Long appId = app.getApplicationId();
        assertNotNull("No audit application ID for " + APPLICATION_TEST, appId);
    }
    
    public void testGetApplications()
    {
        Map<String, AuditApplication> apps = auditModelRegistry.getAuditApplications();
        assertTrue("Application map not complete", apps.containsKey(APPLICATION_TEST));
    }
    
    public void testGetApplicationByKey()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByKey(KEY_TEST);
        assertNotNull(app);
    }
    
    public void testGetPathMappings()
    {
        PathMapper pathMapper = auditModelRegistry.getAuditPathMapper();
        assertNotNull(pathMapper);
        try
        {
            pathMapper.addPathMap("x", "y");
            fail("Should not be allowed to update the path mappings.");
        }
        catch (Throwable e)
        {
            // Expected
        }
    }
    
    private void testBadPath(AuditApplication app, String path)
    {
        try
        {
            app.checkPath(path);
            fail("Expected path check to fail.");
        }
        catch (AuditModelException e)
        {
            // Expected
        }
    }
    
    public void testAuditApplication_Path()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByName(APPLICATION_TEST);
        assertNotNull(app);
        
        // Check that path checks are working
        testBadPath(app, null);
        testBadPath(app, "");
        testBadPath(app, "test");
        testBadPath(app, "/Test");
        testBadPath(app, "/test/");
    }
    
    public void testAuditApplication_GetDataExtractors()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByName(APPLICATION_TEST);
        assertNotNull(app);
        
        List<DataExtractorDefinition> extractors = app.getDataExtractors();
        assertNotNull("Should never get a null list", extractors);
        assertEquals("Expected 13 extractors", 13, extractors.size());
    }
    
    public void testAuditApplication_GetDataGenerators()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByName(APPLICATION_TEST);
        assertNotNull(app);
        
        Map<String, DataGenerator> generators = app.getDataGenerators("/blah");
        assertNotNull("Should never get a null map", generators);
        assertTrue("Expected no generators", generators.isEmpty());

        generators = app.getDataGenerators("/test/1.1/2.1/3.1/4.1");
        assertEquals(1, generators.size());
        assertTrue(generators.containsKey("/test/time"));

        generators = app.getDataGenerators("/test/1.1/2.1/3.1/4.1");
        assertEquals(1, generators.size());
        assertTrue(generators.containsKey("/test/time"));

        generators = app.getDataGenerators("/test/1.1/2.2/3.2/4.1");
        assertEquals(2, generators.size());
        assertTrue(generators.containsKey("/test/time"));
        assertTrue(generators.containsKey("/test/1.1/2.2/3.2/4.1/time"));
    }
}
