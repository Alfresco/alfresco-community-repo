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
package org.alfresco.jcr.importer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.test.BaseJCRTest;
import org.springframework.core.io.ClassPathResource;


public class ImportTest extends BaseJCRTest
{
    protected Session superuserSession;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        SimpleCredentials superuser = new SimpleCredentials("superuser", "".toCharArray());
        superuserSession = repository.login(superuser, getWorkspace());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        superuserSession.logout();
    }
    
    public void testSysImport()
        throws Exception
    {
        ClassPathResource sysview = new ClassPathResource("org/alfresco/jcr/test/sysview.xml");
        superuserSession.importXML("/testroot", sysview.getInputStream(), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    }

    public void testDocImport()
        throws Exception
    {
        ClassPathResource sysview = new ClassPathResource("org/alfresco/jcr/test/docview.xml");
        superuserSession.importXML("/testroot", sysview.getInputStream(), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    }

    public void testThrowCollision()
        throws Exception
    {
        ClassPathResource sysview = new ClassPathResource("org/alfresco/jcr/test/docview.xml");
        superuserSession.importXML("/testroot", sysview.getInputStream(), ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

        try
        {
            superuserSession.importXML("/testroot", sysview.getInputStream(), ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
            fail("Failed to catch UUID collision");
        }
        catch(RepositoryException e)
        {
        }
    }

}
