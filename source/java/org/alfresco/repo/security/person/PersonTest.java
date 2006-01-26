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
        
        for(NodeRef nodeRef: personService.getAllPeople())
        {
            nodeService.deleteNode(nodeRef);
        }

    }

    protected void onTearDownInTransaction() throws Exception
    {
        super.onTearDownInTransaction();
        flushAndClear();
    }

    public void xtestPerformance()
    {
        personService.setCreateMissingPeople(false);
        
        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));
        
        
        
        long create = 0;
        long count = 0;
       
        long start;
        long end;
        
        for(int i = 0; i < 10000; i++)
        {
            String id = "TestUser-"+i;
            start = System.nanoTime();
            personService.createPerson(createDefaultProperties(id, id, id, id,
                    id, rootNodeRef));
            end = System.nanoTime();
            create += (end - start);
            
            if((i > 0) && (i % 100 == 0))
            {
                System.out.println("Count = "+i);
                System.out.println("Average create : "+(create/i/1000000.0f));
                start = System.nanoTime();
                personService.personExists(id);
                end = System.nanoTime();
                System.out.println("Exists : "+((end-start)/1000000.0f));
                
                start = System.nanoTime();
                int size = personService.getAllPeople().size();
                end = System.nanoTime();
                System.out.println("Size ("+size+") : "+((end-start)/1000000.0f));
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
        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));
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
        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh",
                "alfresco", rootNodeRef));
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
       
    }

    private void testProperties(NodeRef nodeRef, String userName, String firstName, String lastName, String email,
            String orgId)
    {
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
        if(personService.getUserNamesAreCaseSensitive())
        {
            personService.createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh",
                    "alfresco", rootNodeRef));
            
            try
            {
                personService.getPerson("derek");
                assertNotNull(null);
            }
            catch (PersonException pe)
            {

            }
            try
            {
                personService.getPerson("deRek");
                assertNotNull(null);
            }
            catch (PersonException pe)
            {

            }
            try
            {
                personService.getPerson("DEREK");
                assertNotNull(null);
            }
            catch (PersonException pe)
            {

            }
            personService.getPerson("Derek");
        }
    }
    
    public void testCaseInsensitive()
    {
        if(!personService.getUserNamesAreCaseSensitive())
        {
            personService.createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh",
                    "alfresco", rootNodeRef));
            
            personService.getPerson("derek");
            personService.getPerson("deRek");
            personService.getPerson("Derek");
            personService.getPerson("DEREK");
        }
    }
}
