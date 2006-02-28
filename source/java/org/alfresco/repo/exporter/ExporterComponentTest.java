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
 */package org.alfresco.repo.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.debug.NodeStoreInspector;


public class ExporterComponentTest extends BaseSpringTest
{

    private NodeService nodeService;
    private ExporterService exporterService;
    private ImporterService importerService;
    private StoreRef storeRef;
    private AuthenticationComponent authenticationComponent;

    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        exporterService = (ExporterService)applicationContext.getBean("exporterComponent");
        importerService = (ImporterService)applicationContext.getBean("importerComponent");
        
        // Create the store
//        this.storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
//        this.storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "test");
//      this.storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        
        
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        this.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    
    public void testExport()
        throws Exception
    {
        TestProgress testProgress = new TestProgress();
        Location location = new Location(storeRef);

        // import
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        importerService.importView(testReader, location, null, null);        
        System.out.println(NodeStoreInspector.dumpNodeStore((NodeService)applicationContext.getBean("NodeService"), storeRef));
        
        // now export
        location.setPath("/system");
        File tempFile = TempFileProvider.createTempFile("xmlexporttest", ".xml");
        OutputStream output = new FileOutputStream(tempFile);
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(location);
        exporterService.exportView(output, parameters, testProgress);
        output.close();
    }

    
    private static class TestProgress
        implements Exporter
    {

        public void start(ExporterContext exportNodeRef)
        {
            System.out.println("TestProgress: start");
        }

        public void startNamespace(String prefix, String uri)
        {
            System.out.println("TestProgress: start namespace prefix = " + prefix + " uri = " + uri);
        }

        public void endNamespace(String prefix)
        {
            System.out.println("TestProgress: end namespace prefix = " + prefix);
        }

        public void startNode(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: start node " + nodeRef);
        }

        public void endNode(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: end node " + nodeRef);
        }

        public void startAspect(NodeRef nodeRef, QName aspect)
        {
//            System.out.println("TestProgress: start aspect " + aspect);
        }

        public void endAspect(NodeRef nodeRef, QName aspect)
        {
//            System.out.println("TestProgress: end aspect " + aspect);
        }

        public void startProperty(NodeRef nodeRef, QName property)
        {
//            System.out.println("TestProgress: start property " + property);
        }

        public void endProperty(NodeRef nodeRef, QName property)
        {
//            System.out.println("TestProgress: end property " + property);
        }

        public void startValueCollection(NodeRef nodeRef, QName property)
        {
//          System.out.println("TestProgress: start value collection: node " + nodeRef + " , property " + property);
        }

        public void endValueCollection(NodeRef nodeRef, QName property)
        {
//          System.out.println("TestProgress: end value collection: node " + nodeRef + " , property " + property);
        }
        
        public void value(NodeRef nodeRef, QName property, Object value)
        {
//            System.out.println("TestProgress: single value " + value);
        }

        public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData)
        {
//            System.out.println("TestProgress: content stream ");
        }

        public void startAssoc(NodeRef nodeRef, QName assoc)
        {
//            System.out.println("TestProgress: start association " + assocDef.getName());
        }

        public void endAssoc(NodeRef nodeRef, QName assoc)
        {
//            System.out.println("TestProgress: end association " + assocDef.getName());
        }

        public void warning(String warning)
        {
            System.out.println("TestProgress: warning " + warning);   
        }

        public void end()
        {
            System.out.println("TestProgress: end");
        }

        public void startProperties(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: startProperties: " + nodeRef);
        }

        public void endProperties(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: endProperties: " + nodeRef);
        }

        public void startAspects(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startAspects: " + nodeRef);
        }

        public void endAspects(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endAspects: " + nodeRef);
        }

        public void startAssocs(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startAssocs: " + nodeRef);
        }

        public void endAssocs(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endAssocs: " + nodeRef);
        }

        public void startACL(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startACL: " + nodeRef);
        }

        public void permission(NodeRef nodeRef, AccessPermission permission)
        {
//          System.out.println("TestProgress: permission: " + permission);
        }

        public void endACL(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endACL: " + nodeRef);
        }

        public void startReference(NodeRef nodeRef, QName childName)
        {
//          System.out.println("TestProgress: startReference: " + nodeRef);
        }

        public void endReference(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endReference: " + nodeRef);
        }

    }
    
}
