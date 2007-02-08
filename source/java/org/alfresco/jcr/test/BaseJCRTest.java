/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.test;

import javax.jcr.Repository;

import junit.framework.TestCase;

import org.alfresco.jcr.repository.RepositoryFactory;
import org.alfresco.jcr.repository.RepositoryImpl;
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
    
    private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:org/alfresco/jcr/test/test-context.xml");
    
    protected String getWorkspace()
    {
        return storeRef.getIdentifier();
    }

    @Override
    protected void setUp() throws Exception
    {
        storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        TestData.generateTestData(applicationContext, storeRef.getIdentifier());
        repositoryImpl = (RepositoryImpl)applicationContext.getBean(RepositoryFactory.REPOSITORY_BEAN);
        repositoryImpl.setDefaultWorkspace(storeRef.getIdentifier());
        repository = repositoryImpl;
    }

}
