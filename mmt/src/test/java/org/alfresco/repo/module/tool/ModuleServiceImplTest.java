/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.module.tool;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        when(reg.getProperty((RegistryKey) any())).thenAnswer(new Answer<Serializable>()
        {
            public Serializable answer(InvocationOnMock invocation) throws Throwable
            {
                RegistryKey key = (RegistryKey) invocation.getArguments()[0];
                return new ModuleVersionNumber("1.1");
            }
        });
        doReturn(Arrays.asList("fee", "alfresco-simple-module", "fo")).when(reg).getChildElements((RegistryKey) any());
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