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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * Tests user filter.
 * 
 * @see UserAuditFilter
 * 
 * @author Vasily Olhin
 * @since 4.2
 */
public class UserAuditFilterTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuditModelRegistryImpl auditModelRegistry;
    private AuditComponent auditComponent;
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistryImpl) ctx.getBean("auditModel.modelRegistry");
        auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        
        // Register the test model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
    }
    
    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        // Throw away the reconfigured registry state
        auditModelRegistry.destroy();
    }
    
    public void testUserFilter()
    {
        Map<Boolean, String> userArr = new HashMap<Boolean, String>();
        userArr.put(false, "user1");
        userArr.put(true, "user2");
        userArr.put(true, "bob");
        UserAuditFilter userAuditFilter = new UserAuditFilter();
        userAuditFilter.setUserFilterPattern("~user1;user2;.*");
        userAuditFilter.afterPropertiesSet();
        auditComponent.setUserAuditFilter(userAuditFilter);
        
        final RetryingTransactionCallback<Map<String, Serializable>> testCallback = new RetryingTransactionCallback<Map<String, Serializable>>()
        {
            public Map<String, Serializable> execute() throws Throwable
            {
                Map<String, Serializable> values = new HashMap<String, Serializable>(13);
                values.put("/3.1/4.1", new Long(41));
                values.put("/3.1/4.2", "42");
                values.put("/3.1/4.3", new Date());
                values.put("/3.1/4.4", "");
                values.put("/3.1/4.5", null);

                return auditComponent.recordAuditValues("/test/one.one/two.one", values);
            }
        };
        RunAsWork<Map<String, Serializable>> testRunAs = new RunAsWork<Map<String, Serializable>>()
        {
            public Map<String, Serializable> doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
            }
        };
        // record audit values using different users
        Map<String, Serializable> result;
        Set<Map.Entry<Boolean, String>> userSet = userArr.entrySet();
        for(Map.Entry<Boolean, String> entry : userSet)
        {
            result = AuthenticationUtil.runAs(testRunAs, entry.getValue());
            assertEquals((boolean) entry.getKey(), !result.isEmpty());
        }
    }
    
    public void testUserFilterParseRedirectProperty()
    {
        UserAuditFilter userAuditFilter = new UserAuditFilter();
        userAuditFilter.setUserFilterPattern("~user1;${audit.test.user};.*");
        try
        {
            userAuditFilter.afterPropertiesSet();
            fail("UserAuditFilter shouldn't parse property with redirect '$'");
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Expected
        }
    }
}
