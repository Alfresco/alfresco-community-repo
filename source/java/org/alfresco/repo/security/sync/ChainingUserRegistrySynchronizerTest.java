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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
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
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        this.authenticationContext.clearCurrentSecurityContext();
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
            newPerson("U1"), newPerson("U2")
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

    /**
     * Tests a differential update of the test users and groups. The layout is as follows
     * 
     * <pre>
     * Z1
     * G1 - U1, U6
     * G2 - U1
     * G3 - U2, G4, G5 - U6
     * 
     * Z2
     * G2 - U1, U3, U4, U6
     * G6 - U3, U4, G7
     * </pre>
     * 
     * @throws Exception
     *             the exception
     */
    public void testDifferentialUpdate() throws Exception
    {
        setUpTestUsersAndGroups();
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z1", new NodeDescription[]
        {
            newPerson("U1", "changeofemail@alfresco.com"), newPerson("U6")
        }, new NodeDescription[]
        {
            newGroup("G1", "U1", "U6"), newGroup("G2", "U1"), newGroupWithDisplayName("G5", "Amazing Group", "U6")
        }), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U1", "shouldbeignored@alfresco.com"), newPerson("U5", "u5email@alfresco.com"), newPerson("U6")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4", "U6"), newGroup("G7")
        }));
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
                assertExists("Z1", "G1", "U1", "U6");
                assertExists("Z1", "G2", "U1");
                assertExists("Z1", "G3", "U2", "G4", "G5");
                assertExists("Z1", "G4");
                assertExists("Z1", "G5", "U6");
                assertGroupDisplayNameEquals("G5", "Amazing Group");
                assertExists("Z2", "U3");
                assertExists("Z2", "U4");
                assertExists("Z2", "U5");
                assertEmailEquals("U5", "u5email@alfresco.com");
                assertExists("Z2", "G6", "U3", "U4", "G7");
                assertExists("Z2", "G7");
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
     * G2 -
     * G3 - U2, G5 - U6
     * G6 - u3
     * 
     * Z2
     * G2 - U1, U3, U6
     * G6 - U3, G7
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
            newGroup("G1", "U6"), newGroup("G2"), newGroup("G3", "U2", "G5"), newGroup("G5", "U6"),
            newGroup("G6", "u3")
        }), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U1", "somenewemail@alfresco.com"), newPerson("U3"), newPerson("U6")
        }, new NodeDescription[]
        {
            newGroup("G2", "U1", "U3", "U4", "U6"), newGroup("G6", "U3", "U4", "G7"), newGroupWithDisplayName("G7", "Late Arrival", "U4", "U5")
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
                assertExists("Z1", "G2");
                assertExists("Z1", "G3", "U2", "G5");
                assertNotExists("G4");
                assertExists("Z1", "G5", "U6");
                assertExists("Z1", "G6", "u3");
                assertExists("Z2", "U1");
                assertEmailEquals("U1", "somenewemail@alfresco.com");
                assertNotExists("U4");
                assertNotExists("U5");
                assertExists("Z2", "G7");
                assertGroupDisplayNameEquals("G7", "Late Arrival");
                return null;
            }
        }, false, true);
        tearDownTestUsersAndGroups();
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
        NodeRef personRef = this.personService.getPerson(personName);
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
        return this.authorityService.getName(shortName.startsWith("G") ? AuthorityType.GROUP : AuthorityType.USER,
                shortName);
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
         * @see org.alfresco.repo.security.sync.UserRegistry#processDeletions(java.util.Set)
         */
        public void processDeletions(Set<String> candidateAuthoritiesForDeletion)
        {
            for (NodeDescription person : this.persons)
            {
                candidateAuthoritiesForDeletion.remove(person.getProperties().get(ContentModel.PROP_USERNAME));
            }
            for (NodeDescription group : this.groups)
            {
                candidateAuthoritiesForDeletion.remove(group.getProperties().get(ContentModel.PROP_AUTHORITY_NAME));
            }
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getGroups(java.util.Date)
         */
        public Collection<NodeDescription> getGroups(Date modifiedSince)
        {
            return this.groups;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.security.sync.UserRegistry#getPersons(java.util.Date)
         */
        public Collection<NodeDescription> getPersons(Date modifiedSince)
        {
            return this.persons;
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
                    String[] authorityNames = new String[17];
                    for (int i = 0; i < authorityNames.length; i++)
                    {
                        // Choose an authority at random from the list of known authorities
                        int index = RandomGroupCollection.this.generator.nextInt(RandomGroupCollection.this.authorities
                                .size());
                        authorityNames[i] = ChainingUserRegistrySynchronizerTest.this.authorityService
                                .getShortName((String) RandomGroupCollection.this.authorities.get(index));
                    }
                    NodeDescription group = newGroup("G" + GUID.generate(), authorityNames);
                    // Make this group a candidate for adding to other groups
                    RandomGroupCollection.this.authorities.add((String) group.getProperties().get(ContentModel.PROP_AUTHORITY_NAME));
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
