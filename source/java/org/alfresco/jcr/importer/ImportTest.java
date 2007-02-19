/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
