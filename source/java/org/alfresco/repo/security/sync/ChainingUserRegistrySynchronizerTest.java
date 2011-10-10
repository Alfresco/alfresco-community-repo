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
package org.alfresco.repo.security.sync;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Tests the {@link ChainingUserRegistrySynchronizer} using a simulated {@link UserRegistry}.
 * 
 * @author dward
 */
public class ChainingUserRegistrySynchronizerTest extends TestCase
{

    /** The context locations, in reverse priority order. */
    private static final String[] CONFIG_LOCATIONS =
    {
        "classpath:alfresco/application-context.xml", "classpath:sync-test-context.xml"
    };

    /** The Spring application context. */
    private static ApplicationContext context = new ClassPathXmlApplicationContext(
            ChainingUserRegistrySynchronizerTest.CONFIG_LOCATIONS);

    /** The synchronizer we are testing. */
    private UserRegistrySynchronizer synchronizer;

    /** The application context manager. */
    private MockApplicationContextManager applicationContextManager;

    /** The person service. */
    private PersonService personService;

    /** The authority service. */
    private AuthorityService authorityService;

    /** The node service. */
    private NodeService nodeService;

    /** The authentication context. */
    private AuthenticationContext authenticationContext;

    /** The retrying transaction helper. */
    private RetryingTransactionHelper retryingTransactionHelper;
    
    /** The value given to the person service. */
    private boolean homeFolderCreationEager;

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        this.synchronizer = (UserRegistrySynchronizer) ChainingUserRegistrySynchronizerTest.context
                .getBean("testUserRegistrySynchronizer");
        this.applicationContextManager = (MockApplicationContextManager) ChainingUserRegistrySynchronizerTest.context
                .getBean("testApplicationContextManager");
        this.personService = (PersonService) ChainingUserRegistrySynchronizerTest.context.getBean("personService");
        this.authorityService = (AuthorityService) ChainingUserRegistrySynchronizerTest.context
                .getBean("authorityService");
        this.nodeService = (NodeService) ChainingUserRegistrySynchronizerTest.context.getBean("nodeService");

        this.authenticationContext = (AuthenticationContext) ChainingUserRegistrySynchronizerTest.context
                .getBean("authenticationContext");
        this.authenticationContext.setSystemUserAsCurrentUser();

        this.retryingTransactionHelper = (RetryingTransactionHelper) ChainingUserRegistrySynchronizerTest.context
                .getBean("retryingTransactionHelper");
        setHomeFolderCreationEager(false); // the normal default if using LDAP
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        this.authenticationContext.clearCurrentSecurityContext();
        setHomeFolderCreationEager(true); // the normal default if not using LDAP
    }

    /**
     * Sets up the test users and groups in three zones, "Z0", "Z1" and "Z2", by doing a forced synchronize with a Mock
     * user registry. Note that the zones have some overlapping entries. "Z0" is not used in subsequent synchronizations
     * and is used to test that users and groups in zones that aren't in the authentication chain get 're-zoned'
     * appropriately. The layout is as follows
     * 
     * <pre>
     * Z0
     * G1
     * U6
     * 
     * Z1
     * G2 - U1, G3 - U2, G4, G5
     * 
     * Z2
     * G2 - U1, U3, U4
     * G6 - U3, U4, G7 - U5
     * </pre>
     * 
     * @throws Exception
     *             the exception
     */
    private void setUpTestUsersAndGroups() throws Exception
    {
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", new NodeDescription[]
        {
            newPerson("U6")
        }, new NodeDescription[]
        {
            newGroup("G1")
        }), new MockUserRegistry("Z1", new NodeDescription[]
        {
            newPerson("U1"), newPerson("U2"), newPerson("U7")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "G3"), newGroup("G3", "U2", "G4", "G5"), newGroup("G4"), newGroup("G5")
        }), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U1"), newPerson("U3"), newPerson("U4"), newPerson("U5")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4"), newGroup("G6", "U3", "U4", "G7"), newGroup("G7", "U5")
        }));
        this.synchronizer.synchronize(true, true, true);
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                assertExists("Z0", "U6");
                assertExists("Z0", "G1");
                assertExists("Z1", "U1");
                assertExists("Z1", "U2");
                assertExists("Z1", "G2", "U1", "G3");
                assertExists("Z1", "G3", "U2", "G4", "G5");
                assertExists("Z1", "G4");
                assertExists("Z1", "G5");
                assertExists("Z2", "U3");
                assertExists("Z2", "U4");
                assertExists("Z2", "U5");
                assertExists("Z2", "G6", "U3", "U4", "G7");
                assertExists("Z2", "G7", "U5");
                return null;
            }
        }, false, true);
    }

    /**
     * Tear down test users and groups.
     * 
     * @throws Exception
     *             the exception
     */
    public void tearDownTestUsersAndGroups() throws Exception
    {
        // Wipe out everything that was in Z1 and Z2
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", new NodeDescription[] {},
                new NodeDescription[] {}), new MockUserRegistry("Z1", new NodeDescription[] {},
                new NodeDescription[] {}), new MockUserRegistry("Z2", new NodeDescription[] {},
                new NodeDescription[] {}));
        this.synchronizer.synchronize(true, true, true);
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                assertNotExists("U1");
                assertNotExists("U2");
                assertNotExists("U3");
                assertNotExists("U4");
                assertNotExists("U5");
                assertNotExists("U6");
                assertNotExists("U7");
                assertNotExists("G1");
                assertNotExists("G2");
                assertNotExists("G3");
                assertNotExists("G4");
                assertNotExists("G5");
                assertNotExists("G6");
                assertNotExists("G7");
                return null;
            }
        }, false, true);
    }

    public void setHomeFolderCreationEager(boolean homeFolderCreationEager)
    {
        this.homeFolderCreationEager = homeFolderCreationEager;
        ((PersonServiceImpl)personService).setHomeFolderCreationEager(homeFolderCreationEager);
    }

    /**
     * Tests a differential update of the test users and groups. The layout is as follows
     * 
     * <pre>
     * Z1
     * G2 - U1, G1 - U1, U6
     * G3 - U2, G4, G5 - U6
     * 
     * Z2
     * G2 - U1, U3, U4, U6
     * G6 - U3, U4, G7, G8 - U4, U8
     * </pre>
     * 
     * @throws Exception
     *             the exception
     */
    public void testDifferentialUpdate() throws Exception
    {
        setUpTestUsersAndGroups();
        this.applicationContextManager.removeZone("Z0");
        this.applicationContextManager.updateZone("Z1", new NodeDescription[]
        {
            newPerson("U1", "changeofemail@alfresco.com"), newPerson("U6"), newPerson("U7")
        }, new NodeDescription[]
        {
            newGroup("G1", "U1", "U6", "UDangling", "G2"), newGroup("G2", "U1", "GDangling", "G1"), // test cyclic G2
                                                                                                    // <-> G1
            newGroupWithDisplayName("G5", "Amazing Group", "U6", "U7", "G4")
        });
        this.applicationContextManager.updateZone("Z2", new NodeDescription[]
        {
            newPerson("U1", "shouldbeignored@alfresco.com"), newPerson("U5", "u5email@alfresco.com"), newPerson("U6"),
            newPerson("U8")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4", "U6"), newGroup("G7"), newGroup("G8", "U4", "U8")
        });
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {

                ChainingUserRegistrySynchronizerTest.this.synchronizer.synchronize(false, false, false);
                // Stay in the same transaction
                assertExists("Z1", "U1");
                assertEmailEquals("U1", "changeofemail@alfresco.com");
                assertExists("Z1", "U2");
                assertExists("Z1", "U6");
                assertExists("Z1", "U7");
                assertExists("Z1", "G1", "U1", "U6");
                assertExists("Z1", "G2", "U1", "G1");
                assertExists("Z1", "G3", "U2", "G4", "G5");
                assertExists("Z1", "G4");
                assertExists("Z1", "G5", "U6", "U7", "G4");
                assertGroupDisplayNameEquals("G5", "Amazing Group");
                assertExists("Z2", "U3");
                assertExists("Z2", "U4");
                assertExists("Z2", "U5");
                assertEmailEquals("U5", "u5email@alfresco.com");
                assertExists("Z2", "U8");
                assertExists("Z2", "G6", "U3", "U4", "G7");
                assertExists("Z2", "G7");
                assertExists("Z2", "G8", "U4", "U8");
                return null;
            }
        });
        tearDownTestUsersAndGroups();
    }

    /**
     * Tests a forced update of the test users and groups. Also tests that groups and users that previously existed in
     * Z2 get moved when they appear in Z1. Also tests that 'dangling references' to removed users (U4, U5) do not cause
     * any problems. Also tests that case-sensitivity is not a problem when an occluded user is recreated with different
     * case. The layout is as follows
     * 
     * <pre>
     * Z1
     * G1 - U6
     * G3 - U2, G5 - U6, G2 - G1
     * G6 - u3
     * 
     * Z2
     * G2 - U1
     * G6 - U3, G7
	 * G8 - U1, U8
     * </pre>
     * 
     * @throws Exception
     *             the exception
     */
    public void testForcedUpdate() throws Exception
    {
        setUpTestUsersAndGroups();
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z1", new NodeDescription[]
        {
            newPerson("U2"), newPerson("u3"), newPerson("U6")
        }, new NodeDescription[]
        {
            newGroup("G1", "U6", "G5"), newGroup("G2", "G1"), newGroup("G3", "U2", "G5"), newGroup("G5", "U6", "G2"), // cycle g1 -> g5 -> g2 -> g1
            newGroup("G6", "u3")
        }), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U1", "somenewemail@alfresco.com"), newPerson("U3"), newPerson("U6"), newPerson("U8"),
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4", "U6"), newGroup("G6", "U3", "U4", "G7"),
            newGroupWithDisplayName("G7", "Late Arrival", "U4", "U5"), newGroup("G8", "U1", "U8")
        }));
        this.synchronizer.synchronize(true, true, true);
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                assertExists("Z1", "U2");
                assertExists("Z1", "u3");
                assertExists("Z1", "U6");
                assertExists("Z1", "G1", "U6");
                assertExists("Z1", "G2", "G1");
                assertExists("Z1", "G3", "U2", "G5");
                assertNotExists("G4");
                assertExists("Z1", "G5", "U6", "G2");
                assertExists("Z1", "G6", "u3");
                assertExists("Z2", "U1");
                assertEmailEquals("U1", "somenewemail@alfresco.com");
                assertNotExists("U4");
                assertNotExists("U5");
                assertExists("Z2", "U8");
				assertExists("Z2", "G7");
                assertGroupDisplayNameEquals("G7", "Late Arrival");
                assertExists("Z2", "G8", "U1", "U8");
                return null;
            }
        }, false, true);
        tearDownTestUsersAndGroups();
    }

    /**
     * Tests a forced update of the test users and groups where some of the users change their case and some groups
     * appear with different case.
     */
    public void testCaseChange() throws Exception
    {
        setUpTestUsersAndGroups();

        final Map<String, NodeRef> personNodes = new TreeMap<String, NodeRef>();
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Get hold of the original person nodes so we can compare them later
                personNodes.put("u1", ChainingUserRegistrySynchronizerTest.this.personService.getPerson("U1", false));
                personNodes.put("u2", ChainingUserRegistrySynchronizerTest.this.personService.getPerson("U2", false));
                personNodes.put("u6", ChainingUserRegistrySynchronizerTest.this.personService.getPerson("U6", false));
                return null;
            }
        }, false, true);

        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z1", new NodeDescription[]
        {
            newPerson("u1"), newPerson("u2"), newPerson("u6"), newPerson("U7")
        }, new NodeDescription[]
        {
            newGroup("g1", "u6"), newGroup("g2", "u1", "G3"), newGroup("G3", "u2", "g4", "g5"), newGroup("g4"),
            newGroup("g5")
        }), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U1"), newPerson("U3"), newPerson("U4"), newPerson("U5")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4"), newGroup("G6", "U3", "U4", "G7"), newGroup("G7", "U5")
        }));
        this.synchronizer.synchronize(true, true, true);
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                assertExists("Z1", "u1");
                assertExists("Z1", "u2");
                assertExists("Z1", "u6");
                assertExists("Z1", "g1", "u6");
                assertExists("Z1", "g2", "u1", "G3");
                assertExists("Z1", "G3", "u2", "g4", "g5");
                assertExists("Z1", "g4");
                assertExists("Z1", "g5");
                assertExists("Z2", "U3");
                assertExists("Z2", "U4");
                assertExists("Z2", "U5");
                assertExists("Z2", "G2", "U3", "U4");
                assertExists("Z2", "G6", "U3", "U4", "G7");
                assertExists("Z2", "G7", "U5");

                // Make sure the original people have been preserved
                assertEquals(personNodes.get("u1"), ChainingUserRegistrySynchronizerTest.this.personService.getPerson(
                        "U1", false));
                assertEquals(personNodes.get("u2"), ChainingUserRegistrySynchronizerTest.this.personService.getPerson(
                        "U2", false));
                assertEquals(personNodes.get("u6"), ChainingUserRegistrySynchronizerTest.this.personService.getPerson(
                        "U6", false));
                return null;
            }
        }, false, true);

        tearDownTestUsersAndGroups();
    }
    
    public void testDifferentialUpdateWithHomeFolderCreation() throws Exception
    {
        setHomeFolderCreationEager(!homeFolderCreationEager);
        testDifferentialUpdate();
    }

    public void testForcedUpdateWithHomeFolderCreation() throws Exception
    {
        setHomeFolderCreationEager(!homeFolderCreationEager);
        testDifferentialUpdate();
    }

    public void testCaseChangeWithHomeFolderCreation() throws Exception
    {
        setHomeFolderCreationEager(!homeFolderCreationEager);
        testDifferentialUpdate();
    }

    /**
     * Tests synchronization with a zone with a larger volume of authorities.
     * 
     * @throws Exception
     *             the exception
     */
    public void testVolume() throws Exception
    {
        List<NodeDescription> persons = new ArrayList<NodeDescription>(new RandomPersonCollection(100));
        List<NodeDescription> groups = new ArrayList<NodeDescription>(new RandomGroupCollection(50, persons));
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", persons, groups));
        this.synchronizer.synchronize(true, true, true);
        tearDownTestUsersAndGroups();
    }

    /**
     * Tests synchronization of group associations in a zone with a larger volume of authorities.
     * 
     * @throws Exception
     *             the exception
     */
    public void dontTestAssocs() throws Exception
    {
        List<NodeDescription> groups = this.retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<List<NodeDescription>>()
                {

                    public List<NodeDescription> execute() throws Throwable
                    {
                        return new ArrayList<NodeDescription>(new RandomGroupCollection(1000,
                                ChainingUserRegistrySynchronizerTest.this.authorityService.getAllAuthoritiesInZone(
                                        AuthorityService.ZONE_AUTH_EXT_PREFIX + "Z0", null)));
                    }
                }, true, true);
        ChainingUserRegistrySynchronizerTest.this.applicationContextManager.setUserRegistries(new MockUserRegistry(
                "Z0", Collections.<NodeDescription> emptyList(), groups));
        ChainingUserRegistrySynchronizerTest.this.synchronizer.synchronize(true, true, true);
        tearDownTestUsersAndGroups();
    }

    /**
     * Constructs a description of a test group.
     * 
     * @param name
     *            the name
     * @param members
     *            the members
     * @return the node description
     */
    private NodeDescription newGroup(String name, String... members)
    {
        return newGroupWithDisplayName(name, name, members);
    }

    /**
     * Constructs a description of a test group with a display name.
     * 
     * @param name
     *            the name
     * @param displayName
     *            the display name
     * @param members
     *            the members
     * @return the node description
     */
    private NodeDescription newGroupWithDisplayName(String name, String displayName, String... members)
    {
        String longName = longName(name);
        NodeDescription group = new NodeDescription(longName);
        PropertyMap properties = group.getProperties();
        properties.put(ContentModel.PROP_AUTHORITY_NAME, longName);
        properties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, displayName);
        if (members.length > 0)
        {
            Set<String> assocs = group.getChildAssociations();
            for (String member : members)
            {
                assocs.add(longName(member));
            }
        }
        group.setLastModified(new Date());
        return group;
    }

    /**
     * Constructs a description of a test person with default email (userName@alfresco.com)
     * 
     * @param userName
     *            the user name
     * @return the node description
     */
    private NodeDescription newPerson(String userName)
    {
        return newPerson(userName, userName + "@alfresco.com");
    }

    /**
     * Constructs a description of a test person with a given email.
     * 
     * @param userName
     *            the user name
     * @param email
     *            the email
     * @return the node description
     */
    private NodeDescription newPerson(String userName, String email)
    {
        NodeDescription person = new NodeDescription(userName);
        PropertyMap properties = person.getProperties();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, userName + "F");
        properties.put(ContentModel.PROP_LASTNAME, userName + "L");
        properties.put(ContentModel.PROP_EMAIL, email);
        person.setLastModified(new Date());
        return person;
    }

    /**
     * Perform all the necessary assertions to ensure that an authority and its members exist in the correct zone.
     * 
     * @param zone
     *            the zone
     * @param name
     *            the name
     * @param members
     *            the members
     */
    private void assertExists(String zone, String name, String... members)
    {
        String longName = longName(name);
        // Check authority exists
        assertTrue(this.authorityService.authorityExists(longName));

        // Check in correct zone
        assertTrue(this.authorityService.getAuthorityZones(longName).contains(
                AuthorityService.ZONE_AUTH_EXT_PREFIX + zone));
        if (AuthorityType.getAuthorityType(longName).equals(AuthorityType.GROUP))
        {
            // Check groups have expected members
            Set<String> memberSet = new HashSet<String>(members.length * 2);
            for (String member : members)
            {
                memberSet.add(longName(member));
            }
            assertEquals(memberSet, this.authorityService.getContainedAuthorities(null, longName, true));
        }
        else
        {
            // Check users exist as persons
            assertTrue(this.personService.personExists(name));

            // Check case matches
            assertEquals(this.personService.getUserIdentifier(name), name);
            
            // Check the person exist.
            NodeRef person = personService.getPerson(name, false);
            assertNotNull("Person for "+name+" should exist", person);
            
            // Check the home folder exists or not.
            NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
            if (homeFolderCreationEager)
            {
                assertNotNull("Home folder for "+name+" should exist", homeFolder);
            }
            else
            {
                assertNull("Home folder for "+name+" should not exist", homeFolder);
            }
        }
    }

    /**
     * Perform all the necessary assertions to ensure that an authority does not exist.
     * 
     * @param name
     *            the name
     */
    private void assertNotExists(String name)
    {
        String longName = longName(name);
        // Check authority does not exist
        assertFalse(this.authorityService.authorityExists(longName));

        // Check there is no zone
        assertNull(this.authorityService.getAuthorityZones(longName));
        if (!AuthorityType.getAuthorityType(longName).equals(AuthorityType.GROUP))
        {
            // Check person does not exist
            assertFalse(this.personService.personExists(name));
        }
    }

    /**
     * Asserts that a person's email has the expected value.
     * 
     * @param personName
     *            the person name
     * @param email
     *            the email
     */
    private void assertEmailEquals(String personName, String email)
    {
        NodeRef personRef = this.personService.getPerson(personName, false);
        assertEquals(email, this.nodeService.getProperty(personRef, ContentModel.PROP_EMAIL));
    }

    /**
     * Asserts that a group's display name has the expected value.
     * 
     * @param personName
     *            the person name
     * @param email
     *            the email
     */
    private void assertGroupDisplayNameEquals(String name, String displayName)
    {
        assertEquals(displayName, this.authorityService.getAuthorityDisplayName(longName(name)));
    }

    /**
     * Converts the given short name to a full authority name, assuming that those short names beginning with 'G'
     * correspond to groups and all others correspond to users.
     * 
     * @param shortName
     *            the short name
     * @return the full authority name
     */
    private String longName(String shortName)
    {
        return this.authorityService.getName(shortName.toLowerCase().startsWith("g") ? AuthorityType.GROUP
                : AuthorityType.USER, shortName);
    }

    /**
     * A Mock {@link UserRegistry} that returns a fixed set of users and groups.
     */
    public static class MockUserRegistry implements UserRegistry
    {

        /** The zone id. */
        private String zoneId;

        /** The persons. */
        private Collection<NodeDescription> persons;

        /** The groups. */
        private Collection<NodeDescription> groups;

        /**
         * Instantiates a new mock user registry.
         * 
         * @param zoneId
         *            the zone id
         * @param persons
         *            the persons
         * @param groups
         *            the groups
         */
        public MockUserRegistry(String zoneId, Collection<NodeDescription> persons, Collection<NodeDescription> groups)
        {
            this.zoneId = zoneId;
            this.persons = persons;
            this.groups = groups;
        }

        /**
         * Modifies the state to match the arguments. Compares new with old and records new modification dates only for
         * changes.
         * 
         * @param persons
         *            the persons
         * @param groups
         *            the groups
         */
        public void updateState(Collection<NodeDescription> persons, Collection<NodeDescription> groups)
        {
            List<NodeDescription> newPersons = new ArrayList<NodeDescription>(this.persons);
            mergeNodeDescriptions(newPersons, persons, ContentModel.PROP_USERNAME, false);
            this.persons = newPersons;

            List<NodeDescription> newGroups = new ArrayList<NodeDescription>(this.groups);
            mergeNodeDescriptions(newGroups, groups, ContentModel.PROP_AUTHORITY_NAME, true);
            this.groups = newGroups;
        }

        /**
         * Merges together an old and new list of node descriptions. Retains the old node with its old modification date
         * if it is the same in the new list, otherwises uses the node from the new list.
         * 
         * @param oldNodes
         *            the old node list
         * @param newNodes
         *            the new node list
         * @param idProp
         *            the name of the ID property
         * @param caseSensitive
         *            are IDs case sensitive?
         */
        private void mergeNodeDescriptions(List<NodeDescription> oldNodes, Collection<NodeDescription> newNodes,
                QName idProp, boolean caseSensitive)
        {
            Map<String, NodeDescription> nodeMap = new LinkedHashMap<String, NodeDescription>(newNodes.size() * 2);
            for (NodeDescription node : newNodes)
            {
                String id = (String) node.getProperties().get(idProp);
                if (!caseSensitive)
                {
                    id = id.toLowerCase();
                }
                nodeMap.put(id, node);
            }
            for (int i = 0; i < oldNodes.size(); i++)
            {
                NodeDescription oldNode = oldNodes.get(i);
                String id = (String) oldNode.getProperties().get(idProp);
                if (!caseSensitive)
                {
                    id = id.toLowerCase();
                }
                NodeDescription newNode = nodeMap.remove(id);
                if (newNode == null)
                {
                    oldNodes.remove(i);
                    i--;
                }
                else if (!oldNode.getProperties().equals(newNode.getProperties())
                        || !oldNode.getChildAssociations().equals(newNode.getChildAssociations()))
                {
                    oldNodes.set(i, newNode);
                }
            }
            oldNodes.addAll(nodeMap.values());
        }

        /**
         * Instantiates a new mock user registry.
         * 
         * @param zoneId
         *            the zone id
         * @param persons
         *            the persons
         * @param groups
         *            the groups
         */
        public MockUserRegistry(String zoneId, NodeDescription[] persons, NodeDescription[] groups)
        {
            this(zoneId, Arrays.asList(persons), Arrays.asList(groups));
        }

        /**
         * Gets the zone id.
         * 
         * @return the zoneId
         */
        public String getZoneId()
        {
            return this.zoneId;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getGroupNames()
         */
        public Collection<String> getGroupNames()
        {
            List<String> groupNames = new LinkedList<String>();
            for (NodeDescription group : this.groups)
            {
                groupNames.add((String) group.getProperties().get(ContentModel.PROP_AUTHORITY_NAME));
            }
            return groupNames;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getPersonNames()
         */
        public Collection<String> getPersonNames()
        {
            List<String> personNames = new LinkedList<String>();
            for (NodeDescription person : this.persons)
            {
                personNames.add((String) person.getProperties().get(ContentModel.PROP_USERNAME));
            }
            return personNames;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getGroups(java.util.Date)
         */
        public Collection<NodeDescription> getGroups(Date modifiedSince)
        {
            return filterNodeDescriptions(this.groups, modifiedSince);
        }

        /**
         * Filters the given list of node descriptions, retaining only those with a modification date greater than the
         * given date.
         * 
         * @param nodes
         *            the list of nodes
         * @param modifiedSince
         *            the modified date
         * @return the filter list of nodes
         */
        private Collection<NodeDescription> filterNodeDescriptions(Collection<NodeDescription> nodes, Date modifiedSince)
        {
            if (modifiedSince == null)
            {
                return nodes;
            }
            List<NodeDescription> filteredNodes = new LinkedList<NodeDescription>();
            for (NodeDescription node : nodes)
            {
                Date modified = node.getLastModified();
                if (modifiedSince.compareTo(modified) < 0)
                {
                    filteredNodes.add(node);
                }
            }
            return filteredNodes;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getPersons(java.util.Date)
         */
        public Collection<NodeDescription> getPersons(Date modifiedSince)
        {
            return filterNodeDescriptions(this.persons, modifiedSince);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getPersonMappedProperties()
         */
        public Set<QName> getPersonMappedProperties()
        {
            return new HashSet<QName>(Arrays.asList(new QName[]
            {
                ContentModel.PROP_USERNAME, ContentModel.PROP_FIRSTNAME, ContentModel.PROP_LASTNAME,
                ContentModel.PROP_EMAIL, ContentModel.PROP_ORGID, ContentModel.PROP_ORGANIZATION,
                ContentModel.PROP_HOME_FOLDER_PROVIDER
            }));
        }
    }

    /**
     * An {@link ChildApplicationContextManager} for a chain of application contexts containing mock user registries.
     */
    public static class MockApplicationContextManager implements ChildApplicationContextManager
    {

        /** The contexts. */
        private Map<String, ApplicationContext> contexts = Collections.emptyMap();

        /**
         * Sets the user registries.
         * 
         * @param registries
         *            the new user registries
         */
        public void setUserRegistries(MockUserRegistry... registries)
        {
            this.contexts = new LinkedHashMap<String, ApplicationContext>(registries.length * 2);
            for (MockUserRegistry registry : registries)
            {
                StaticApplicationContext context = new StaticApplicationContext();
                context.getDefaultListableBeanFactory().registerSingleton("userRegistry", registry);
                this.contexts.put(registry.getZoneId(), context);
            }
        }

        /**
         * Removes the application context for the given zone ID (simulating a change in the authentication chain).
         * 
         * @param zoneId
         *            the zone id
         */
        public void removeZone(String zoneId)
        {
            this.contexts.remove(zoneId);
        }

        /**
         * Updates the state of the given zone ID, oopying in new modification dates only where changes have been made.
         * 
         * @param zoneId
         *            the zone id
         * @param persons
         *            the new list of persons
         * @param groups
         *            the new list of groups
         */
        public void updateZone(String zoneId, NodeDescription[] persons, NodeDescription[] groups)
        {
            ApplicationContext context = this.contexts.get(zoneId);
            MockUserRegistry registry = (MockUserRegistry) context.getBean("userRegistry");
            registry.updateState(Arrays.asList(persons), Arrays.asList(groups));
        }

        /*
         * (non-Javadoc)
         * @see
         * org.alfresco.repo.management.subsystems.ChildApplicationContextManager#getApplicationContext(java.lang.String
         * )
         */
        public ApplicationContext getApplicationContext(String id)
        {
            return this.contexts.get(id);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.ChildApplicationContextManager#getInstanceIds()
         */
        public Collection<String> getInstanceIds()
        {
            return this.contexts.keySet();
        }
    }

    /**
     * A collection whose iterator returns randomly generated persons.
     */
    public class RandomPersonCollection extends AbstractCollection<NodeDescription>
    {

        /** The collection size. */
        private final int size;

        /**
         * The Constructor.
         * 
         * @param size
         *            the collection size
         */
        public RandomPersonCollection(int size)
        {
            this.size = size;
        }

        /*
         * (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<NodeDescription> iterator()
        {
            return new Iterator<NodeDescription>()
            {

                private int pos;

                public boolean hasNext()
                {
                    return this.pos < RandomPersonCollection.this.size;
                }

                public NodeDescription next()
                {
                    this.pos++;
                    return newPerson("U" + GUID.generate());
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };

        }

        /*
         * (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size()
        {
            return this.size;
        }

    }

    /**
     * A collection whose iterator returns randomly generated groups with random associations to a given list of
     * persons.
     */
    public class RandomGroupCollection extends AbstractCollection<NodeDescription>
    {
        /** Use a fixed seed to give this class deterministic behaviour */
        private Random generator = new Random(1628876500L);

        /** The collection size. */
        private final int size;

        /** The authorities. */
        private final List<String> authorities;

        /**
         * The Constructor.
         * 
         * @param size
         *            the collection size
         * @param authorities
         *            the authorities
         */
        public RandomGroupCollection(int size, Set<String> authorities)
        {
            this.size = size;
            this.authorities = new ArrayList<String>(authorities);
        }

        /**
         * The Constructor.
         * 
         * @param size
         *            the collection size
         * @param authorities
         *            the authorities
         */
        public RandomGroupCollection(int size, Collection<NodeDescription> persons)
        {
            this.size = size;
            this.authorities = new ArrayList<String>(persons.size());
            for (NodeDescription nodeDescription : persons)
            {
                this.authorities.add((String) nodeDescription.getProperties().get(ContentModel.PROP_USERNAME));
            }
        }

        /*
         * (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<NodeDescription> iterator()
        {
            return new Iterator<NodeDescription>()
            {

                private int pos;

                public boolean hasNext()
                {
                    return this.pos < RandomGroupCollection.this.size;
                }

                public NodeDescription next()
                {
                    this.pos++;

                    // Just for fun, make the last group one that includes ALL authorities!
                    String[] authorityNames = new String[this.pos == RandomGroupCollection.this.size ? RandomGroupCollection.this.size : 17];
                    for (int i = 0; i < authorityNames.length; i++)
                    {
                        // Choose an authority at random from the list of known authorities
                        int index = this.pos == RandomGroupCollection.this.size ? i : RandomGroupCollection.this.generator.nextInt(RandomGroupCollection.this.authorities
                                .size());
                        authorityNames[i] = ChainingUserRegistrySynchronizerTest.this.authorityService
                                .getShortName((String) RandomGroupCollection.this.authorities.get(index));
                    }
                    NodeDescription group = newGroup("G" + GUID.generate(), authorityNames);
                    // Make this group a candidate for adding to other groups
                    RandomGroupCollection.this.authorities.add((String) group.getProperties().get(
                            ContentModel.PROP_AUTHORITY_NAME));
                    return group;
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };

        }

        /*
         * (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size()
        {
            return this.size;
        }

    }

}
