/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.namespace.QName;


/**
 * Test Model Definitions
 */
public class TestModel
{

    public static void main(String[] args)
    {
        if (args != null && args.length > 0 && args[0].equals("-h"))
        {
            System.out.println("TestModel [model filename]*");
            System.exit(0);
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
        }
    }
    
    private static void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO)
    {
        CacheManager cacheManager = new CacheManager();
        
        Cache uriToModelsEhCache = new Cache("uriToModelsCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(uriToModelsEhCache);      
        EhCacheAdapter<String, Map<String, List<CompiledModel>>> uriToModelsCache = new EhCacheAdapter<String, Map<String, List<CompiledModel>>>();
        uriToModelsCache.setCache(uriToModelsEhCache);
        
        dictionaryDAO.setUriToModelsCache(uriToModelsCache);
        
        Cache compileModelsEhCache = new Cache("compiledModelsCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(compileModelsEhCache);
        EhCacheAdapter<String, Map<QName,CompiledModel>> compileModelCache = new EhCacheAdapter<String, Map<QName,CompiledModel>>();
        compileModelCache.setCache(compileModelsEhCache);
        
        dictionaryDAO.setCompiledModelsCache(compileModelCache);
    }
    
    private static void initNamespaceCaches(NamespaceDAOImpl namespaceDAO)
    {
        CacheManager cacheManager = new CacheManager();
        
        Cache urisEhCache = new Cache("urisCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(urisEhCache);      
        EhCacheAdapter<String, List<String>> urisCache = new EhCacheAdapter<String, List<String>>();
        urisCache.setCache(urisEhCache);
        
        namespaceDAO.setUrisCache(urisCache);
        
        Cache prefixesEhCache = new Cache("prefixesCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(prefixesEhCache);
        EhCacheAdapter<String, Map<String, String>> prefixesCache = new EhCacheAdapter<String, Map<String, String>>();
        prefixesCache.setCache(prefixesEhCache);
        
        namespaceDAO.setPrefixesCache(prefixesCache);
    }
}