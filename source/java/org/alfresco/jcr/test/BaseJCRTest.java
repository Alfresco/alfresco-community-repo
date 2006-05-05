/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
