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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ThreadPoolExecutorFactoryBean;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author sglover
 *
 */
public class DictionaryDAOTest
{
    public static final String TEST_RESOURCE_MESSAGES = "alfresco/messages/dictionary-messages";

    private DictionaryService service;
    private DictionaryDAOImpl dictionaryDAO;

    @Before
    public void setUp() throws Exception
    {   
        // register resource bundles for messages
        I18NUtil.registerResourceBundle(TEST_RESOURCE_MESSAGES);

        // Instantiate Dictionary Service
        TenantService tenantService = new MultiTServiceImpl();

        this.dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);

        initDictionaryCaches(dictionaryDAO, tenantService);

        new AuthenticationUtil().afterPropertiesSet();

        // Populate with appropriate models
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("alfresco/model/contentModel.xml");
        List<String> labels = new ArrayList<String>();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();

        DictionaryComponent component = new DictionaryComponent();
        component.setDictionaryDAO(dictionaryDAO);
        component.setMessageLookup(new StaticMessageLookup());
        service = component;
    }
    
    private void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO, TenantService tenantService) throws Exception
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

    @Test
    public void testBootstrap() throws Exception
    {
        TenantService tenantService = new SingleTServiceImpl();   
        
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);
        initDictionaryCaches(dictionaryDAO, tenantService);
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
    }

    @Test
    public void test1()
    {
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				M2Model customModel = M2Model.createModel(
						Thread.currentThread().getContextClassLoader().
						getResourceAsStream("dictionary/dictionarydaotest_model1.xml"));
				dictionaryDAO.putModel(customModel);
				assertNotNull(service.getType(ContentModel.TYPE_CONTENT));
				QName qname = QName.createQName("{http://www.alfresco.org/test/dictionarydaotest1/1.0}type1");
				TypeDefinition td = service.getType(qname);
				assertNotNull(td);
				return null;
			}
		}, "user1", "tenant1");

        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				assertNotNull(service.getType(ContentModel.TYPE_CONTENT));
				QName qname = QName.createQName("{http://www.alfresco.org/test/dictionarydaotest1/1.0}type1");
				TypeDefinition td = service.getType(qname);
				assertNull(td);
				return null;
			}
		}, "user2", "tenant2");
    }
}
