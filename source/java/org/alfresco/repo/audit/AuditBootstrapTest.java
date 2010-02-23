/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit;

import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelException;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
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
        // We have to look inside the subsystem for this test
        ApplicationContextFactory subsystem = (ApplicationContextFactory) ctx.getBean("Audit");
        ApplicationContext subCtx = subsystem.getApplicationContext();
        auditModelRegistry = (AuditModelRegistryImpl) subCtx.getBean("auditModel.modelRegistry");
        
        // Register a new model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/audit/alfresco-audit-test.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
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
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-01.xml");
    }
    
    public void testModelLoading_NoDataGenerator() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-02.xml");
    }
    
    public void testModelLoading_DuplicatePath() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-03.xml");
    }
    
    public void testModelLoading_UppercasePath() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-04.xml");
    }
    
    public void testModelLoading_InvalidDataGeneratorName() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-05.xml");
    }
    
    public void testModelLoading_BadGeneratorRegisteredName() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-06.xml");
    }
    
    public void testGetApplicationId()
    {
        AuditApplication app = auditModelRegistry.getAuditApplicationByName(APPLICATION_TEST);
        assertNotNull(app);
        Long appId = app.getApplicationId();
        assertNotNull("No audit application ID for " + APPLICATION_TEST, appId);
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
        
        Map<String, DataExtractor> extractors = app.getDataExtractors("/blah");
        assertNotNull("Should never get a null map", extractors);
        assertTrue("Expected no extractors", extractors.isEmpty());

        extractors = app.getDataExtractors("/test/1.1/2.1/3.1/4.1");
        assertEquals(1, extractors.size());
        assertTrue(extractors.containsKey("/test/1.1/2.1/3.1/4.1/value.1"));

        extractors = app.getDataExtractors("/test/1.1/2.1/3.1");
        assertEquals(1, extractors.size());
        assertTrue(extractors.containsKey("/test/1.1/2.1/3.1/value.1"));
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
