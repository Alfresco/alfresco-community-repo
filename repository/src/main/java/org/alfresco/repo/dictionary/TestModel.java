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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.ThreadPoolExecutorFactoryBean;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;

/**
 * Test Model Definitions
 */
public class TestModel
{
    /**
     * Test model
     * 
     * Java command line client <br />
     * Syntax: <br />
     * TestModel [-h] [model filename]*
     * <p>
     * Returns 0 for success.
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        if (args != null && args.length > 0 && args[0].equals("-h"))
        {
            System.out.println("TestModel [model filename]*");
            System.exit(1);
        }

        System.out.println("Testing dictionary model definitions...");

        // construct list of models to test
        // include alfresco defaults
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("org/alfresco/repo/security/authentication/userModel.xml");
        bootstrapModels.add("alfresco/model/contentModel.xml");
        bootstrapModels.add("alfresco/model/applicationModel.xml");
        bootstrapModels.add("alfresco/model/bpmModel.xml");

        // include models specified on command line
        for (String arg : args)
        {
            bootstrapModels.add(arg);
        }

        for (String model : bootstrapModels)
        {
            System.out.println(" " + model);
        }

        // construct dictionary dao
        TenantService tenantService = new SingleTServiceImpl();

        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);

        initDictionaryCaches(dictionaryDAO, tenantService);

        // bootstrap dao
        try
        {
            DictionaryBootstrap bootstrap = new DictionaryBootstrap();
            bootstrap.setModels(bootstrapModels);
            bootstrap.setDictionaryDAO(dictionaryDAO);
            bootstrap.bootstrap();
            System.out.println("Models are valid.");

            System.exit(0); // Success

        }
        catch (Exception e)
        {
            System.out.println("Found an invalid model...");
            Throwable t = e;
            while (t != null)
            {
                System.out.println(t.getMessage());
                t = t.getCause();
            }
            System.exit(2); // Not Success
        }
    }

    private static void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO, TenantService tenantService) throws Exception
    {
        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDAO);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
        ThreadPoolExecutorFactoryBean threadPoolfactory = new ThreadPoolExecutorFactoryBean();
        threadPoolfactory.afterPropertiesSet();
        compiledModelsCache.setThreadPoolExecutor((ThreadPoolExecutor) threadPoolfactory.getObject());
        dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDAO.init();
    }
}
