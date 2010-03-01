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
package org.alfresco.jcr.test;

import javax.jcr.Repository;

import junit.framework.TestCase;

import org.alfresco.jcr.repository.RepositoryFactory;
import org.alfresco.jcr.repository.RepositoryImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base JCR Test
 * 
 * @author David Caruana
 */
public class BaseJCRTest extends TestCase
{
    private RepositoryImpl repositoryImpl;
    protected Repository repository;
    protected StoreRef storeRef;
    protected String adminUserName;
    
    protected static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:org/alfresco/jcr/test/test-context.xml");
    
    protected String getWorkspace()
    {
        return storeRef.getIdentifier();
    }
    
    protected String getAdminUserName()
    {
        return adminUserName;
    }

    @Override
    protected void setUp() throws Exception
    {
        storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        TestData.generateTestData(applicationContext, storeRef.getIdentifier());
        repositoryImpl = (RepositoryImpl)applicationContext.getBean(RepositoryFactory.REPOSITORY_BEAN);
        repositoryImpl.setDefaultWorkspace(storeRef.getIdentifier());
        repository = repositoryImpl;
        adminUserName = AuthenticationUtil.getAdminUserName();
    }

}
