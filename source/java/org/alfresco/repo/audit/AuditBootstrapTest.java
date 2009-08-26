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
import org.alfresco.repo.audit.generator.DataGenerator.DataGeneratorScope;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelException;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.util.ApplicationContextHelper;
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
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuditModelRegistry auditModelRegistry;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistry) ctx.getBean("auditModel.modelRegistry");
        
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
    
    public void testModelLoading_InvalidScope() throws Exception
    {
        loadBadModel("classpath:alfresco/audit/alfresco-audit-test-bad-05.xml");
    }
    
    public void testGetModelId()
    {
        Long repoId = auditModelRegistry.getAuditModelId(APPLICATION_TEST);
        assertNotNull("No audit model ID for " + APPLICATION_TEST, repoId);
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
        AuditApplication app = auditModelRegistry.getAuditApplication(APPLICATION_TEST);
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
        AuditApplication app = auditModelRegistry.getAuditApplication(APPLICATION_TEST);
        assertNotNull(app);
        
        Map<String, DataExtractor> extractors = app.getDataExtractors("/blah");
        assertNotNull("Should never get a null map", extractors);
        assertTrue("Expected no extractors", extractors.isEmpty());

        extractors = app.getDataExtractors("/test/1.1/2.1/3.1/4.1");
        assertEquals(2, extractors.size());
        assertTrue(extractors.containsKey("/test/1.1/2.1/3.1/value.1"));
        assertTrue(extractors.containsKey("/test/1.1/2.1/3.1/4.1/value.1"));
    }
    
    public void testAuditApplication_GetDataGenerators_AnyScope()
    {
        AuditApplication app = auditModelRegistry.getAuditApplication(APPLICATION_TEST);
        assertNotNull(app);
        
        Map<String, DataGenerator> generators = app.getDataGenerators("/blah", DataGeneratorScope.ALL);
        assertNotNull("Should never get a null map", generators);
        assertTrue("Expected no generators", generators.isEmpty());

        generators = app.getDataGenerators("/test/1.1/2.1/3.1/4.1", DataGeneratorScope.ALL);
        assertEquals(1, generators.size());
        assertTrue(generators.containsKey("/test/time"));

        generators = app.getDataGenerators("/test/1.1/2.1/3.1/4.1", DataGeneratorScope.SESSION);
        assertEquals(1, generators.size());
        assertTrue(generators.containsKey("/test/time"));

        generators = app.getDataGenerators("/test/1.1/2.1/3.1/4.1", DataGeneratorScope.AUDIT);
        assertEquals(0, generators.size());

        generators = app.getDataGenerators("/test/1.1/2.2/3.2/4.1", DataGeneratorScope.ALL);
        assertEquals(2, generators.size());
        assertTrue(generators.containsKey("/test/time"));
        assertTrue(generators.containsKey("/test/1.1/2.2/3.2/4.1/time"));
    }
}
