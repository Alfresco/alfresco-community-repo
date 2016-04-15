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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author sglover
 *
 */
public class DictionaryLoadDAOTest
{
    public static final String TEST_RESOURCE_MESSAGES = "alfresco/messages/dictionary-messages";

//    private DictionaryService service;
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

//        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
//        {
//			@Override
//			public Void doWork() throws Exception
//			{
//		        dictionaryDAO.init();
//				return null;
//			}
//        }, "user1", "tenant1");

        initDictionaryCaches(dictionaryDAO, tenantService);

        new AuthenticationUtil().afterPropertiesSet();

        // Populate with appropriate models
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("alfresco/model/contentModel.xml");
        bootstrapModels.add("org/alfresco/repo/security/authentication/userModel.xml");
        bootstrapModels.add("alfresco/model/bpmModel.xml");
        bootstrapModels.add("alfresco/model/wcmModel.xml");
        bootstrapModels.add("alfresco/model/forumModel.xml");
        bootstrapModels.add("alfresco/model/imapModel.xml");
        bootstrapModels.add("alfresco/model/transferModel.xml");
        bootstrapModels.add("alfresco/model/applicationModel.xml");
        bootstrapModels.add("alfresco/model/wcmAppModel.xml");
        bootstrapModels.add("org/alfresco/repo/action/actionModel.xml");
        bootstrapModels.add("org/alfresco/repo/rule/ruleModel.xml");
        bootstrapModels.add("org/alfresco/repo/version/version_model.xml");
        bootstrapModels.add("org/alfresco/repo/version/version2_model.xml");
        bootstrapModels.add("alfresco/model/emailServerModel.xml");
        bootstrapModels.add("alfresco/model/calendarModel.xml");
        bootstrapModels.add("alfresco/model/blogIntegrationModel.xml");
        bootstrapModels.add("alfresco/model/linksModel.xml");
        bootstrapModels.add("alfresco/model/remoteCredentialsModel.xml");
        bootstrapModels.add("alfresco/model/datalistModel.xml");
        bootstrapModels.add("alfresco/model/quickShareModel.xml");
        bootstrapModels.add("alfresco/model/surfModel.xml");
        bootstrapModels.add("alfresco/model/siteModel.xml");
        bootstrapModels.add("alfresco/model/publishingModel.xml");

        List<String> labels = new ArrayList<String>();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
    }

    private void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO, TenantService tenantService) throws Exception
    {
        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDAO);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(true);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY);

        ThreadPoolExecutor threadPoolExecutor = new DynamicallySizedThreadPoolExecutor(20, 20, 90, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
        compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);
        dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDAO.init();
    }

    @Test
    public void test1()
    {
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				dictionaryDAO.init();

				return null;
			}
		}, "user1", "tenant1");
    }
}
