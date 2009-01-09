/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.jcr.item;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.test.BaseJCRTest;


/**
 * One-off item tests - sanity checks for bug fixes
 * 
 * @author janv
 */
public class ItemTest extends BaseJCRTest
{
    protected Session session;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void test_ALFCOM_1655() throws RepositoryException
    {
        SimpleCredentials user = new SimpleCredentials("admin", "admin".toCharArray());
        
        session = repository.login(user, getWorkspace());
        
        try
        {
            Node rootNode = session.getRootNode();
            assertNotNull(rootNode);
            
            Node aNode = rootNode.addNode("A1","my:a");
            assertNotNull(aNode);
            
            Node bNode = aNode.addNode("B1","my:b");
            assertNotNull(bNode);
            
            aNode = rootNode.addNode("A2","cm:folder");
            assertNotNull(aNode);
            
            bNode = aNode.addNode("B2","cm:folder");
            assertNotNull(bNode);
            
            Node cNode = rootNode.addNode("C1","my:c");
            assertNotNull(cNode);
            
            aNode = cNode.addNode("A3","my:a");
            assertNotNull(aNode);
            
            bNode = cNode.addNode("B3","my:b");
            assertNotNull(bNode);
            
            session.save();
        }
        finally
        {
            if (session != null) { session.logout(); }
        }
        
        session = repository.login(user, getWorkspace());
        
        try
        {
            Node rootNode = session.getRootNode();
            assertNotNull(rootNode);
            
            Node aNode = rootNode.getNode("A1");
            assertNotNull(aNode);
            
            Node bNode = aNode.getNode("B1");
            assertNotNull(bNode);
            
            aNode = rootNode.getNode("A2");
            assertNotNull(aNode);
            
            bNode = aNode.getNode("B2");
            assertNotNull(bNode);
            
            Node cNode = rootNode.getNode("C1");
            assertNotNull(cNode);
            
            aNode = cNode.getNode("A3");
            assertNotNull(aNode);
            
            bNode = cNode.getNode("B3");
            assertNotNull(bNode);
        }
        finally
        {
            if (session != null) { session.logout(); }
        }
    }
}
