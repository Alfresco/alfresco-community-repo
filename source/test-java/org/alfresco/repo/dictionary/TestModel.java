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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.dictionary.DictionaryDAOImpl.DictionaryRegistry;
import org.alfresco.repo.dictionary.NamespaceDAOImpl.NamespaceRegistry;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;


/**
 * Test Model Definitions
 */
public class TestModel
{
    /**
     * Test model
     * 
     * Java command line client
     * <br />
     * Syntax:
     * <br />
     * TestModel [-h] [model filename]*
     * <p>
     * Returns 0 for success.
     */
    public static void main(String[] args)
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
        bootstrapModels.add("alfresco/model/wcmModel.xml");
        bootstrapModels.add("alfresco/model/applicationModel.xml");
        bootstrapModels.add("alfresco/model/bpmModel.xml");
        bootstrapModels.add("alfresco/model/wcmAppModel.xml");

        // include models specified on command line
        for (String arg: args)
        {
            bootstrapModels.add(arg);
        }
        
        for (String model : bootstrapModels)
        {
            System.out.println(" " + model);
        }
        
        // construct dictionary dao        
        TenantService tenantService = new SingleTServiceImpl();
        
        NamespaceDAOImpl namespaceDAO = new NamespaceDAOImpl();
        namespaceDAO.setTenantService(tenantService);
        
        initNamespaceCaches(namespaceDAO);
        
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);
        dictionaryDAO.setTenantService(tenantService);
        
        initDictionaryCaches(dictionaryDAO);

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
        catch(Exception e)
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
    
    private static void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO)
    {
        SimpleCache<String, DictionaryRegistry> dictionaryCache = new DefaultSimpleCache<String, DictionaryRegistry>();
        dictionaryDAO.setDictionaryRegistryCache(dictionaryCache);
    }
    
    private static void initNamespaceCaches(NamespaceDAOImpl namespaceDAO)
    {
        SimpleCache<String, NamespaceRegistry> namespaceCache = new DefaultSimpleCache<String, NamespaceRegistry>();
        namespaceDAO.setNamespaceRegistryCache(namespaceCache);
    }
}