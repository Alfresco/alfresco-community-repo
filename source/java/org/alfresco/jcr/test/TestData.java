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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class TestData
{
    public static final String TEST_WORKSPACE = "test";

    /**
     * Generate Test Workspace within Repository
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("org/alfresco/jcr/test/test-context.xml");
        generateTestData(context, TEST_WORKSPACE);
        System.out.println("Generated TCK test data to workspace: " + TEST_WORKSPACE);
        System.exit(0);
    }

    /**
     * Bootstrap Repository with JCR Test Data
     * 
     * @param applicationContext
     * @param workspaceName
     */
    public static void generateTestData(ApplicationContext applicationContext, String workspaceName)
    {
        {
            // Bootstrap Users
            MutableAuthenticationDao authDAO = (MutableAuthenticationDao) applicationContext.getBean("alfDaoImpl");
            if (authDAO.userExists("superuser") == false)
            {
                authDAO.createUser("superuser", "".toCharArray());
            }
            if (authDAO.userExists("user") == false)
            {
                authDAO.createUser("user", "".toCharArray());
            }
            if (authDAO.userExists("anonymous") == false)
            {
                authDAO.createUser("anonymous", "".toCharArray());
            }
        }

        try
        {
            AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
            authenticationComponent.setSystemUserAsCurrentUser();

            try
            {
                // Bootstrap Workspace Test Data
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, workspaceName);

                ImporterBootstrap bootstrap = new ImporterBootstrap();
                bootstrap.setAuthenticationComponent((AuthenticationComponent) applicationContext.getBean("authenticationComponent"));
                bootstrap.setImporterService((ImporterService) applicationContext.getBean(ServiceRegistry.IMPORTER_SERVICE.getLocalName()));
                bootstrap.setNodeService((NodeService) applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName()));
                bootstrap.setNamespaceService((NamespaceService) applicationContext.getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName()));
                bootstrap.setTransactionService((TransactionService) applicationContext.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName()));
                bootstrap.setStoreUrl(storeRef.toString());

                List<Properties> views = new ArrayList<Properties>();
                Properties testView = new Properties();
                testView.setProperty("path", "/");
                testView.setProperty("location", "org/alfresco/jcr/test/testData.xml");
                views.add(testView);
                bootstrap.setBootstrapViews(views);
                bootstrap.bootstrap();

                // Bootstrap clears security context
                authenticationComponent.setSystemUserAsCurrentUser();
                
                PermissionService permissionService = (PermissionService)applicationContext.getBean(ServiceRegistry.PERMISSIONS_SERVICE.getLocalName());
                NodeService nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());

//                permissionService.setPermission(nodeService.getRootNode(storeRef), PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
                permissionService.setPermission(nodeService.getRootNode(storeRef), "superuser", PermissionService.ALL_PERMISSIONS, true);
                permissionService.setPermission(nodeService.getRootNode(storeRef), "anonymous", PermissionService.READ, true);
                permissionService.setPermission(nodeService.getRootNode(storeRef), "user", PermissionService.READ, true);
                permissionService.setPermission(nodeService.getRootNode(storeRef), "user", PermissionService.WRITE, true);
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
            }
        }
        catch (RuntimeException e)
        {
            System.out.println("Exception: " + e);
            e.printStackTrace();
            throw e;
        }
    }

}
