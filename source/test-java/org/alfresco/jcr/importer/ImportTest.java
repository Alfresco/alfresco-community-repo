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
package org.alfresco.jcr.importer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.test.BaseJCRTest;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

@Category(OwnJVMTestsCategory.class)
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
