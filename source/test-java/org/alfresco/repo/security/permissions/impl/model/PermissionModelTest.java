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
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.RequiredPermission.On;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

@Category(OwnJVMTestsCategory.class)
public class PermissionModelTest extends AbstractPermissionTest
{

    public PermissionModelTest()
    {
        super();
    }

    public void testWoof()
    {
        QName typeQname = nodeService.getType(rootNodeRef);
        Set<QName> aspectQNames = nodeService.getAspects(rootNodeRef);
        PermissionReference ref = permissionModelDAO.getPermissionReference(null, "CheckOut");
        Set<PermissionReference> answer = permissionModelDAO.getRequiredPermissions(ref, typeQname, aspectQNames, On.NODE);
        assertEquals(1, answer.size());
    }

    public void testIncludePermissionGroups()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Consumer"));

        assertEquals(8, grantees.size());
    }

    public void testIncludePermissionGroups2()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Contributor"));

        assertEquals(16, grantees.size());
    }

    public void testIncludePermissionGroups3()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Editor"));

        assertEquals(19, grantees.size());
    }

    public void testIncludePermissionGroups4()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Collaborator"));

        assertEquals(26, grantees.size());
    }

    public void testIncludePermissionGroups5()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Coordinator"));

        // NB This has gone from 59 to 63, I believe, because of the for new WCM roles.
        // 63-97 from AVM permission fix up
        assertEquals(103, grantees.size());
    }

    public void testIncludePermissionGroups6()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "RecordAdministrator"));

        assertEquals(19, grantees.size());
    }

    public void testGetGrantingPermissions()
    {
        Set<PermissionReference> granters = permissionModelDAO.getGrantingPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "ReadProperties"));
        // NB This has gone from 10 to 14 because of the new WCM roles, I believe.
        // 14-18 -> 4 site base roles added
        assertEquals(18, granters.size());

        granters = permissionModelDAO.getGrantingPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base", namespacePrefixResolver),
                "_ReadProperties"));
        // NB 11 to 15 as above.
        // 5-19 site based roles added
        assertEquals(19, granters.size());
    }

    public void testGlobalPermissions()
    {
        Set<? extends PermissionEntry> globalPermissions = permissionModelDAO.getGlobalPermissionEntries();
        assertEquals(6, globalPermissions.size());
    }

    public void testRequiredPermissions()
    {
        Set<PermissionReference> required = permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "Read"), QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        assertEquals(3, required.size());

        required = permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base", namespacePrefixResolver),
                "ReadContent"), QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        assertEquals(1, required.size());

        required = permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base", namespacePrefixResolver),
                "_ReadContent"), QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        assertEquals(0, required.size());

        required = permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("cm", "cmobject", namespacePrefixResolver),
                "Coordinator"), QName.createQName("cm", "cmobject", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        assertEquals(18, required.size());

        required = permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base", namespacePrefixResolver),
                "FullControl"), QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        assertEquals(18, required.size());

    }

    public void testMultiThreadedAccess()
    {
        Thread runner = null;

        for (int i = 0; i < 20; i++)
        {
            runner = new Nester("Concurrent-" + i, runner);
        }
        if (runner != null)
        {
            runner.start();

            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    class Nester extends Thread
    {
        Thread waiter;

        Nester(String name, Thread waiter)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
        }

        public void run()
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            if (waiter != null)
            {
                waiter.start();
            }
            try
            {
                System.out.println("Start " + this.getName());
                RetryingTransactionCallback<Void> queryPermissionModel = new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        Random random = new Random();
                        Set<PermissionReference> toTest = permissionModelDAO.getAllPermissions(QName.createQName("sys", "base", namespacePrefixResolver));

                        for (int i = 0; i < 10000; i++)
                        {
                            for (PermissionReference pr : toTest)
                            {
                                if (random.nextFloat() < 0.5f)
                                {
                                    // permissionModelDAO.getGranteePermissions(pr);
                                    // permissionModelDAO.getGrantingPermissions(pr);
                                    permissionModelDAO.getRequiredPermissions(pr, QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(),
                                            On.NODE);
                                }
                            }
                        }
                        return null;
                    }
                };
                retryingTransactionHelper.doInTransaction(queryPermissionModel);
                System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
            }
            if (waiter != null)
            {
                try
                {
                    waiter.join();
                }
                catch (InterruptedException e)
                {
                }
            }
        }

    }
    
    public void testNulls()
    {
        permissionModelDAO.getRequiredPermissions(null, QName.createQName("sys", "base", namespacePrefixResolver), Collections.<QName> emptySet(), On.NODE);
        permissionModelDAO.getRequiredPermissions(SimplePermissionReference.getPermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "Read"),null, Collections.<QName> emptySet(), On.NODE);
        permissionModelDAO.getRequiredPermissions(null, null, Collections.<QName> emptySet(), On.NODE);
        
        permissionModelDAO.getGranteePermissions(null);

        permissionModelDAO.getGlobalPermissionEntries().contains(null);
       
    }
}
