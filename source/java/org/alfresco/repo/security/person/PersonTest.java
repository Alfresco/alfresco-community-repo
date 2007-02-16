/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.EqualsHelper;

public class PersonTest extends BaseSpringTest
{

    private PersonService personService;

    private NodeService nodeService;

    private NodeRef rootNodeRef;

    public PersonTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void onSetUpInTransaction() throws Exception
    {
        personService = (PersonService) applicationContext.getBean("personService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        for (NodeRef nodeRef : personService.getAllPeople())
        {
            nodeService.deleteNode(nodeRef);
        }

    }

    protected void onTearDownInTransaction() throws Exception
    {
        super.onTearDownInTransaction();
    }

    public void xtestPerformance()
    {
        personService.setCreateMissingPeople(false);

        personService
                .createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        long create = 0;

        long start;
        long end;

        for (int i = 0; i < 10000; i++)
        {
            String id = "TestUser-" + i;
            start = System.nanoTime();
            personService.createPerson(createDefaultProperties(id, id, id, id, id, rootNodeRef));
            end = System.nanoTime();
            create += (end - start);

            if ((i > 0) && (i % 100 == 0))
            {
                System.out.println("Count = " + i);
                System.out.println("Average create : " + (create / i / 1000000.0f));
                start = System.nanoTime();
                personService.personExists(id);
                end = System.nanoTime();
                System.out.println("Exists : " + ((end - start) / 1000000.0f));

                start = System.nanoTime();
                int size = personService.getAllPeople().size();
                end = System.nanoTime();
                System.out.println("Size (" + size + ") : " + ((end - start) / 1000000.0f));
            }
        }
    }

    public void testCreateMissingPeople1()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        personService.setCreateMissingPeople(false);
        try
        {
            personService.getPerson("andy");
            fail("Getting Andy should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testCreateMissingPeople2()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        NodeRef nodeRef = personService.getPerson("andy");
        assertNotNull(nodeRef);
        testProperties(nodeRef, "andy", "andy", "", "", "");

        nodeRef = personService.getPerson("Andy");
        assertNotNull(nodeRef);
        if (personService.getUserIdentifier("Andy").equals("Andy"))
        {
            testProperties(nodeRef, "Andy", "Andy", "", "", "");
        }
        else
        {
            testProperties(nodeRef, "andy", "andy", "", "", "");
        }

        personService.setCreateMissingPeople(false);
        try
        {
            personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                    "alfresco", rootNodeRef));
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testCreateMissingPeople()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        NodeRef nodeRef = personService.getPerson("andy");
        assertNotNull(nodeRef);
        testProperties(nodeRef, "andy", "andy", "", "", "");

        personService.setCreateMissingPeople(true);
        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));
        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");

        testProperties(personService.getPerson("andy"), "andy", "andy", "", "", "");

        assertEquals(2, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("andy")));
        assertTrue(personService.getAllPeople().contains(personService.getPerson("derek")));

    }

    public void testMutableProperties()
    {
        assertEquals(5, personService.getMutableProperties().size());
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_HOMEFOLDER));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_FIRSTNAME));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_LASTNAME));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_EMAIL));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_ORGID));

    }

    public void testPersonCRUD1()
    {
        personService.setCreateMissingPeople(false);
        try
        {
            personService.getPerson("derek");
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testPersonCRUD2()
    {
        personService.setCreateMissingPeople(false);
        personService
                .createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");

        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek_", "Hulley_", "dh@dh_",
                "alfresco_", rootNodeRef));

        testProperties(personService.getPerson("derek"), "derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_");

        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));

        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");

        assertEquals(1, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("derek")));

        personService.deletePerson("derek");
        assertEquals(0, personService.getAllPeople().size());
        try
        {
            personService.getPerson("derek");
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testPersonCRUD()
    {
        personService.setCreateMissingPeople(false);
        personService
                .createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        testProperties(personService.getPerson("Derek"), "Derek", "Derek", "Hulley", "dh@dh", "alfresco");

        personService.setPersonProperties("Derek", createDefaultProperties("derek", "Derek_", "Hulley_", "dh@dh_",
                "alfresco_", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_");

        personService.setPersonProperties("Derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek", "Hulley", "dh@dh", "alfresco");

        assertEquals(1, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("Derek")));
        assertEquals(personService.personExists("derek"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("derek"), "Derek"));
        assertEquals(personService.personExists("dEREK"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("dEREK"), "Derek"));
        assertEquals(personService.personExists("DEREK"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("DEREK"), "Derek"));

        personService.deletePerson("Derek");
        assertEquals(0, personService.getAllPeople().size());

    }

    private void testProperties(NodeRef nodeRef, String userName, String firstName, String lastName, String email,
            String orgId)
    {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        assertEquals(userName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                ContentModel.PROP_USERNAME)));
        assertNotNull(nodeService.getProperty(nodeRef, ContentModel.PROP_HOMEFOLDER));
        assertEquals(firstName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                ContentModel.PROP_FIRSTNAME)));
        assertEquals(lastName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                ContentModel.PROP_LASTNAME)));
        assertEquals(email, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                ContentModel.PROP_EMAIL)));
        assertEquals(orgId, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef,
                ContentModel.PROP_ORGID)));
    }

    private Map<QName, Serializable> createDefaultProperties(String userName, String firstName, String lastName,
            String email, String orgId, NodeRef home)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_HOMEFOLDER, home);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGID, orgId);
        return properties;
    }

    public void testCaseSensitive()
    {

        personService
                .createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        try
        {
            NodeRef nodeRef = personService.getPerson("derek");
            if (personService.getUserIdentifier("derek").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        try
        {
            NodeRef nodeRef = personService.getPerson("deRek");
            if (personService.getUserIdentifier("deRek").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        try
        {

            NodeRef nodeRef = personService.getPerson("DEREK");
            if (personService.getUserIdentifier("DEREK").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        personService.getPerson("Derek");
    }

}
