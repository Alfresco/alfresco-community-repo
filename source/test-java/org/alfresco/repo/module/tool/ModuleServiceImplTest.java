package org.alfresco.repo.module.tool;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.repo.admin.registry.RegistryKey;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.module.ModuleServiceImpl;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Tests some of ModuleServiceImpl that is not covered by other tests
 * @author Gethin James
 */
public class ModuleServiceImplTest
{
    ModuleServiceImpl moduleService;

    @Before
    public void setUp() throws Exception
    {
        moduleService = new ModuleServiceImpl();
        Resource simpleMod = new PathMatchingResourcePatternResolver().getResource("classpath:alfresco/module/simplemodule.properties");
        assertNotNull(simpleMod);
        RegistryService reg = mock(RegistryService.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);

        when(reg.getProperty((RegistryKey) anyObject())).thenAnswer(new Answer<Serializable>()
        {
            public Serializable answer(InvocationOnMock invocation) throws Throwable
            {
                RegistryKey key = (RegistryKey) invocation.getArguments()[0];
                return new ModuleVersionNumber("1.1");
            }
        });
        doReturn(Arrays.asList("fee", "alfresco-simple-module", "fo")).when(reg).getChildElements((RegistryKey) anyObject());
        doReturn(new Resource[] {simpleMod}).when(applicationContext).getResources(anyString());
        moduleService.setRegistryService(reg);
        moduleService.setApplicationContext(applicationContext);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetApplyToTenants() throws Exception
    {
       moduleService.setApplyToTenants(false);
    }

    @Test
    public void testGetMissingModules() throws Exception
    {
        List<ModuleDetails> miss = moduleService.getMissingModules();
        assertNotNull(miss);
        //There are 3 modules. 2 of which are missing. "alfresco-simple-module" should be found.
        assertEquals(2, miss.size());
    }
}