/*
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest.test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class CMISCustomTypeTest extends BaseCMISWebScriptTest
{
    private static String TEST_NAMESPACE = "http://www.alfresco.org/model/aiim";
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        // Uncomment to change default behaviour of tests
        setCustomContext("classpath:cmis/cmis-test-context.xml");
        setDefaultRunAs("admin");
//      RemoteServer server = new RemoteServer();
//      server.username = "admin";
//      server.password = "admin";
//      setRemoteServer(server);
//        setArgsAsHeaders(false);
//        setValidateResponse(false);
//        setListener(new CMISTestListener(System.out));
//        setTraceReqRes(true);

        
//        initServer("classpath:wcm/wcm-jbpm-context.xml");
//        
//        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
//        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
//        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
//              
//        this.authenticationComponent.setSystemUserAsCurrentUser();
//        
//        // Create users
//        createUser(USER_ONE);
//        createUser(USER_TWO);
//        createUser(USER_THREE);
//        createUser(USER_FOUR);
//        
//        // Do tests as user one
//        this.authenticationComponent.setCurrentUser(USER_ONE);
//        
        super.setUp();
    }

    public void testX()
        throws Exception
    {
        IRI rootHREF = getRootChildrenCollection(getWorkspace(getRepository()));
        sendRequest(new GetRequest(rootHREF.toString()), 200, getAtomValidator());
    }

    
    public void testCreateSubType()
        throws Exception
    {
        final Entry testFolder = createTestFolder("testCreateSubType");
        final NodeRef testFolderRef = getNodeRef(testFolder);

        // create node
        // TODO: For now create item via Alfresco foundation APIs
        //       When multi-valued props supported, move to pure CMIS Create
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                FileFolderService fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
                NodeService nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
                FileInfo file = fileFolderService.create(testFolderRef, "createSubType", QName.createQName(TEST_NAMESPACE, "content"));
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(QName.createQName(TEST_NAMESPACE, "Title"), "createSubTypeTitle");
                props.put(QName.createQName(TEST_NAMESPACE, "Authors"), (Serializable)Arrays.asList(new String[] { "Dave", "Fred" }));
                nodeService.addProperties(file.getNodeRef(), props);
                fileFolderService.getWriter(file.getNodeRef()).putContent("Some test content");
                return null;
            }
        }, getDefaultRunAs());
    }
    
}
