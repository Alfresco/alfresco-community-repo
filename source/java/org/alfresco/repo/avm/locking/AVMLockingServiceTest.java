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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.avm.locking;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Tests for AVM locking service.
 * @author britt
 */
public class AVMLockingServiceTest extends TestCase
{
    private static FileSystemXmlApplicationContext fContext = null;
    
    private static AVMLockingService fService;
    
    private static AttributeService fAttributeService;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AVMLockingService)fContext.getBean("AVMLockingService");
            fAttributeService = (AttributeService)fContext.getBean("AttributeService");
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        List<String> keys = fAttributeService.getKeys("");
        for (String key : keys)
        {
            fAttributeService.removeAttribute("", key);
        }
    }
    
    public void testAll()
    {
        try
        {
            fService.addWebProject("alfresco");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            List<String> owners = new ArrayList<String>();
            owners.add("Buffy");
            owners.add("Spike");
            AVMLock lock = new AVMLock("alfresco",
                                       "Sunnydale",
                                       "Revello Drive/1630",
                                       AVMLockingService.Type.DISCRETIONARY,
                                       owners);
            fService.lockPath(lock);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertNotNull(fService.getLock("alfresco", "Revello Drive/1630"));
            assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks("alfresco").size());
            List<String> owners2 = new ArrayList<String>();
            owners2.add("Buffy");
            owners2.add("Willow");
            AVMLock lock2 = new AVMLock("alfresco",
                                        "Sunnydale",
                                        "UC Sunnydale/Stevenson Hall",
                                        AVMLockingService.Type.DISCRETIONARY,
                                        owners2);
            fService.lockPath(lock2);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(2, fService.getUsersLocks("Buffy").size());
            assertEquals(2, fService.getWebProjectLocks("alfresco").size());
            fService.removeLock("alfresco", "Revello Drive/1630");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks("alfresco").size());
            fService.removeWebProject("alfresco");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(0, fService.getUsersLocks("Spike").size());
            assertEquals(0, fService.getUsersLocks("Buffy").size());
            assertEquals(0, fService.getUsersLocks("Willow").size());
            assertEquals(0, fService.getUsersLocks("Tara").size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
