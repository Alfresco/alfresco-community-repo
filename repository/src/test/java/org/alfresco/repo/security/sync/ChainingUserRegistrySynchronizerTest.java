/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.sync;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.ldap.LDAPInitialDirContextFactoryImpl;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.security.sync.ldap.AbstractDirectoryServiceUserAccountStatusInterpreter;
import org.alfresco.repo.security.sync.ldap.LDAPUserAccountStatusInterpreter;
import org.alfresco.repo.security.sync.ldap.LDAPUserRegistry;
import org.alfresco.repo.security.sync.ldap.LDAPUserRegistry.PersonCollection;
import org.alfresco.repo.security.sync.ldap_ad.LDAPADUserAccountStatusInterpreter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.io.Serializable;
import java.util.*;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Tests the {@link ChainingUserRegistrySynchronizer} using a simulated {@link UserRegistry}.
 * 
 * @author dward
 */
@Category(LuceneTests.class)
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
    
    /** The namespace service. */
    private NamespaceService namespaceService;

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

    private static final Log logger = LogFactory.getLog(ChainingUserRegistrySynchronizerTest.class);

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
        
        
        // this.authenticationContext.setSystemUserAsCurrentUser();
        //AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        this.authenticationContext.setSystemUserAsCurrentUser();

        this.retryingTransactionHelper = (RetryingTransactionHelper) ChainingUserRegistrySynchronizerTest.context
                .getBean("retryingTransactionHelper");
        setHomeFolderCreationEager(false); // the normal default if using LDAP
        
        this.namespaceService = (NamespaceService) ChainingUserRegistrySynchronizerTest.context
                .getBean("namespaceService");
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
        this.synchronizer.synchronize(true, true);
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
        // Re-zone everything that may have gone astray
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", new NodeDescription[]
        {
            newPerson("U1"), newPerson("U2"), newPerson("U3"), newPerson("U4"), newPerson("U5"), newPerson("U6"),
            newPerson("U7")
        }, new NodeDescription[]
        {
            newGroup("G1"), newGroup("G2"), newGroup("G3"), newGroup("G4"), newGroup("G5"), newGroup("G6"),
            newGroup("G7")
        }), new MockUserRegistry("Z1", new NodeDescription[] {}, new NodeDescription[] {}), new MockUserRegistry("Z2",
                new NodeDescription[] {}, new NodeDescription[] {}));
        this.synchronizer.synchronize(true, true);
        // Wipe out everything that was in Z0 - Z2
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", new NodeDescription[] {},
                new NodeDescription[] {}), new MockUserRegistry("Z1", new NodeDescription[] {},
                new NodeDescription[] {}), new MockUserRegistry("Z2", new NodeDescription[] {},
                new NodeDescription[] {}));
        this.synchronizer.synchronize(true, true);
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
                // re: THOR-293 - note: use runAs else security context is cleared (=> current system user becomes null and personExists fails)
                AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Split transactions to avoid MNT-9768
                        ChainingUserRegistrySynchronizerTest.this.synchronizer.synchronize(false, false);
                        return null;
                    }
                });
                
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
        this.synchronizer.synchronize(true, true);
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

    private class MockLDAPUserRegistry extends LDAPUserRegistry implements IMockUserRegistry
    {
        MockUserRegistry mockUserRegistry;

        public MockLDAPUserRegistry(MockUserRegistry mockUserRegistry)
        {
            this.mockUserRegistry = mockUserRegistry;
        }

        @Override
        public void setActive(boolean active)
        {
            mockUserRegistry.setActive(active);
        }

        @Override
        public boolean isActive()
        {
            return mockUserRegistry.isActive();
        }

        @Override
        public Set<QName> getPersonMappedProperties()
        {
            return mockUserRegistry.getPersonMappedProperties();
        }

        @Override
        public Collection<NodeDescription> getPersons(Date modifiedSince)
        {
            Collection<NodeDescription> persons = mockUserRegistry.getPersons(modifiedSince);
            return !persons.isEmpty() ? persons : super.getPersons(modifiedSince);
        }

        @Override
        public Collection<String> getPersonNames()
        {
            return mockUserRegistry.getPersonNames();
        }

        @Override
        public Collection<String> getGroupNames()
        {
            return mockUserRegistry.getGroupNames();
        }

        @Override
        public Collection<NodeDescription> getGroups(Date modifiedSince)
        {
            return mockUserRegistry.getGroups(modifiedSince);
        }

        @Override
        public String getZoneId()
        {
            return mockUserRegistry.getZoneId();
        }

        @Override
        public void updateState(Collection<NodeDescription> persons, Collection<NodeDescription> groups)
        {
            mockUserRegistry.updateState(persons, groups);
        }
    }

    private void testLDAPDisableUserAccount(AbstractDirectoryServiceUserAccountStatusInterpreter userAccountStatusInterpreter,
            String enabledAccountPropertyValue, String disabledAccountPropertyValue) throws Exception
    {
        MockUserRegistry mockUserRegistry = new MockUserRegistry("ldap1", new NodeDescription[] {
                newPersonWithUserAccountStatusProperty("EnabledUser", enabledAccountPropertyValue),
                newPersonWithUserAccountStatusProperty("DisabledUser", disabledAccountPropertyValue) }, new NodeDescription[] {});

        MockLDAPUserRegistry mockLDAPUserRegistry = new MockLDAPUserRegistry(mockUserRegistry);
        mockLDAPUserRegistry.setUserAccountStatusInterpreter(userAccountStatusInterpreter);

        this.applicationContextManager.setUserRegistries(mockLDAPUserRegistry);

        ChainingUserRegistrySynchronizer chainingSynchronizer = (ChainingUserRegistrySynchronizer) this.synchronizer;
        chainingSynchronizer.setExternalUserControl("true");
        chainingSynchronizer.setExternalUserControlSubsystemName("ldap1");

        this.synchronizer.synchronize(false, false);

        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef enabledUserRef = ChainingUserRegistrySynchronizerTest.this.personService.getPerson("EnabledUser", false);
                assertFalse(ChainingUserRegistrySynchronizerTest.this.nodeService.hasAspect(enabledUserRef, ContentModel.ASPECT_PERSON_DISABLED));

                NodeRef disabledUserRef = ChainingUserRegistrySynchronizerTest.this.personService.getPerson("DisabledUser", false);
                assertTrue(ChainingUserRegistrySynchronizerTest.this.nodeService.hasAspect(disabledUserRef, ContentModel.ASPECT_PERSON_DISABLED));

                return null;
            }
        }, false, true);
    }

    public void testLDAPDisableUserAccountWithActiveDirectoryProperty() throws Exception
    {
        LDAPADUserAccountStatusInterpreter ldapadUserAccountStatusInterpreter = new LDAPADUserAccountStatusInterpreter();

        // Active Directory enabled account: userAccountControl=512; 
        // disabled account: userAccountControl=514.
        testLDAPDisableUserAccount(ldapadUserAccountStatusInterpreter, "512", "514");
    }

    public void testLDAPDisableUserAccountWithNetscapDSProperty() throws Exception
    {
        LDAPUserAccountStatusInterpreter ldapUserAccountStatusInterpreter = new LDAPUserAccountStatusInterpreter();
        ldapUserAccountStatusInterpreter.setAcceptNullArgument(true);

        // Netscape Directory Server derivatives (Oracle, Red Hat, 389 DS)
        // disabled account property: nsAccountLock=true.
        ldapUserAccountStatusInterpreter.setDisabledAccountPropertyValue("true");

        testLDAPDisableUserAccount(ldapUserAccountStatusInterpreter, null, "true");
    }

    public void testLDAPDisableUserAccountWithOpenLDAPProperty() throws Exception
    {
        LDAPUserAccountStatusInterpreter ldapUserAccountStatusInterpreter = new LDAPUserAccountStatusInterpreter();
        ldapUserAccountStatusInterpreter.setAcceptNullArgument(true);

        // OpenLDAP disabled account: pwdAccountLockedTime=000001010000Z (part of PPolicy module)
        ldapUserAccountStatusInterpreter.setDisabledAccountPropertyValue("000001010000Z");

        testLDAPDisableUserAccount(ldapUserAccountStatusInterpreter, null, "000001010000Z");
    }

    /**
     * Tests a forced update of the test users and groups with deletions disabled. No users or groups should be deleted,
     * whether or not they move registry. Groups that would have been deleted should have no members and should only be
     * in the default zone.
     * 
     * @throws Exception
     *             the exception
     */
    public void testForcedUpdateWithoutDeletions() throws Exception
    {
        UserRegistrySynchronizer synchronizer = (UserRegistrySynchronizer) ChainingUserRegistrySynchronizerTest.context
                .getBean("testUserRegistrySynchronizerPreventDeletions");
        setUpTestUsersAndGroups();
        this.applicationContextManager.setUserRegistries(new MockUserRegistry("Z0", new NodeDescription[]
        {
            newPerson("U2"), newPerson("U3"), newPerson("U4"),
        }, new NodeDescription[]
        {
            newGroup("G1"), newGroup("G2"),
        }), new MockUserRegistry("Z1", new NodeDescription[]
        {
            newPerson("U5"), newPerson("u6"),
        }, new NodeDescription[] {}), new MockUserRegistry("Z2", new NodeDescription[]
        {
            newPerson("U6"),
        }, new NodeDescription[] {}));
        synchronizer.synchronize(true, true);
        this.retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                // MNT-13867 fix. User U6 already exists in zone "Z0". According ChainingUserRegistrySynchronizercurrent
                // implementation when syncDelete==false person that exists in a different zone with higher
                // precedence  will be ignored
                assertExists("Z0", "U6");
                assertExists("Z1", "U1");
                assertExists("Z1", "U7");
                assertExists("Z1", "G5");
                assertExists("Z2", "G6", "U3", "U4", "G7");
                assertExists("Z1", "U5");
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
        this.synchronizer.synchronize(true, true);
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
        this.synchronizer.synchronize(true, true);
        tearDownTestUsersAndGroups();
    }
    
    /**
     * 
     */
    public void testTestSynchronize() throws Exception
    {
        String TEST_REGISTRY_NAME = "Z0";
        String TEST_REGISTRY_NAME_BAD = "XX";
        List<NodeDescription> persons = new ArrayList<NodeDescription>(new RandomPersonCollection(100));
        List<NodeDescription> groups = new ArrayList<NodeDescription>(new RandomGroupCollection(50, persons));
        MockUserRegistry testRegistry = new MockUserRegistry(TEST_REGISTRY_NAME, persons, groups);
        this.applicationContextManager.setUserRegistries(testRegistry);
        Collection<String> instances = this.applicationContextManager.getInstanceIds();
        try
        {
            // pre-conditions
            assertTrue("pre-condition test registry isActive", testRegistry.isActive());
            assertTrue("pre-condition test registry in application context", instances.contains(TEST_REGISTRY_NAME));
            assertFalse("pre-condition test registry isActive", instances.contains(TEST_REGISTRY_NAME_BAD));
        
            if(synchronizer instanceof ChainingUserRegistrySynchronizer)
            {
                /**
                 *  positive test using mocked user registry
                 */
                ChainingUserRegistrySynchronizer chainingSynchronizer = (ChainingUserRegistrySynchronizer)synchronizer;
                SynchronizeDiagnostic diagnostic = chainingSynchronizer.testSynchronize(TEST_REGISTRY_NAME);
                assertTrue("diagnostic is active", diagnostic.isActive());
                assertNotNull("diagnostic users are null", diagnostic.getUsers());
                assertNotNull("diagnostic groups are null", diagnostic.getGroups());
                
                /**
                 *  test with active flag set to false
                 */
                testRegistry.setActive(false);
                diagnostic = chainingSynchronizer.testSynchronize(TEST_REGISTRY_NAME);
                assertFalse("diagnostic is still active", diagnostic.isActive());
                assertNotNull("diagnostic users are null", diagnostic.getUsers());
                assertNotNull("diagnostic groups are null", diagnostic.getGroups());
                
                /**
                 * negative test - invalid user registry
                 */
                try
                {
                    chainingSynchronizer.testSynchronize(TEST_REGISTRY_NAME_BAD);
                    fail("bad user registry not detected");
                }
                catch (AuthenticationException ae)
                {
                    // expect to go here on invalid authenticator
                }
            }
            else
            {
                fail("test not run - synchroniser is not a ChainingUserRegistrySynchronizer");
            }
        } 
        finally
        {
            tearDownTestUsersAndGroups();
        }
    }
    
    public void testSyncStatus() throws Exception
    {
        Date testStart = new Date();
        
        try
        {       
            List<NodeDescription> persons = new ArrayList<NodeDescription>(new RandomPersonCollection(3));
            List<NodeDescription> groups = new ArrayList<NodeDescription>(new RandomGroupCollection(4, persons));
            MockUserRegistry testRegistry = new MockUserRegistry("Z0", persons, groups);
            this.applicationContextManager.setUserRegistries(testRegistry);
            this.synchronizer.synchronize(true, true);
            
            if(this.synchronizer instanceof ChainingUserRegistrySynchronizerStatus)
            {
                ChainingUserRegistrySynchronizerStatus status = (ChainingUserRegistrySynchronizerStatus)this.synchronizer;
                // Header Status
                assertTrue("end time not updated", status.getSyncEndTime().after(testStart));
                assertTrue("start time not updated", status.getSyncStartTime().after(testStart));
                assertEquals("sync status is not complete", "COMPLETE", status.getSynchronizationStatus());         
                assertNotNull("last run on server is null", status.getLastRunOnServer());
                assertNull(status.getLastErrorMessage());

                // Authenticator status
                assertEquals("sync status is not complete", "COMPLETE", status.getSynchronizationStatus("Z0")); 
                //assertNull(status.getSynchronizationLastError("Z0"));
            }
            else
            {
                fail("test not run");
            }
            
            /**
             * Negative test - make an user registry throw an exception
             */
            testRegistry.setThrowError(true);
            testStart = new Date();
            try
            {
                this.synchronizer.synchronize(true, true);
                fail("error not thrown");
            }
            catch (AlfrescoRuntimeException e)
            {
                // expect to go here
                ChainingUserRegistrySynchronizerStatus status = (ChainingUserRegistrySynchronizerStatus)this.synchronizer;
                // Header Status
                assertTrue("end time not updated", status.getSyncEndTime().after(testStart));
                assertTrue("start time not updated", status.getSyncStartTime().after(testStart));
                assertEquals("sync status is not complete", "COMPLETE_ERROR", status.getSynchronizationStatus());         
                assertNotNull("last run on server is null", status.getLastRunOnServer());
                assertNotNull(status.getLastErrorMessage());

                // Authenticator status
                assertEquals("sync status is not complete", "COMPLETE_ERROR", status.getSynchronizationStatus("Z0")); 
                assertNotNull(status.getSynchronizationLastError("Z0"));
            }
        }
        finally
        {
            tearDownTestUsersAndGroups();
        }
    }
    
    /**
     * <p>Test that upon a first sync, the missing properties at the AD level are set as 'null' on the Alfresco Person object.</p> 
     * <p>MNT-14026: LDAP sync fails to update attribute's value deletion.</p>
     */
    public void testSyncInexistentProperty() throws Exception
    {
        try
        {
            // Execute an LDAP sync where the AD server returns the attributes of a person without a certain property, in this case the 'mail'.
            executeMockedLDAPSyncWithoutActiveDirectoryEmailProp();

            Map<QName, Serializable> userProperties = this.nodeService.getProperties(this.personService.getPerson("U1"));
            assertTrue("User must have the email property even though it's null", userProperties.containsKey(ContentModel.PROP_EMAIL));
            assertTrue("User's email must be null on first sync.", userProperties.get(ContentModel.PROP_EMAIL) == null);
        }
        finally
        {
            tearDownTestUsersAndGroups();
        }
    }
    
    /**
     * <p>Test that an attribute is also removed on the Alfresco side, when it's removed at the AD level.</p> 
     * <p>MNT-14026: LDAP sync fails to update attribute's value deletion.</p>
     */
    public void testSyncDeletedProperty() throws Exception
    {
        try
        {
            logger.info("testSyncDeletedProperty executing..");
            // Execute an LDAP sync where the AD server returns the attributes of a person, including the 'mail' property.
            executeMockedLDAPSyncWithActiveDirectoryEmailProp();

            Map<QName, Serializable> userProperties = this.nodeService.getProperties(this.personService.getPerson("U1"));
            assertTrue("User's email must be not null.", userProperties.get(ContentModel.PROP_EMAIL).equals("U1@alfresco.com"));

            // Execute an LDAP sync where the AD server returns the attributes
            // of a person without the 'mail' property, because it was deleted.
            executeMockedLDAPSyncWithoutActiveDirectoryEmailProp();

            userProperties = this.nodeService.getProperties(this.personService.getPerson("U1"));
            assertTrue("User must have the email property even though it's null", userProperties.containsKey(ContentModel.PROP_EMAIL));
            assertTrue("User's email must be null on a 2rd sync, since the email property was removed at the AD level.", userProperties.get(ContentModel.PROP_EMAIL) == null);
        }
        finally
        {
            logger.info("testSyncDeletedProperty executing finally");

            tearDownTestUsersAndGroups();
            logger.info("testSyncDeletedProperty finished finally");
        }
    }
    
    private void executeMockedLDAPSyncWithActiveDirectoryEmailProp() throws Exception
    {
        executeMockedLDAPSync(true);
    }
    
    private void executeMockedLDAPSyncWithoutActiveDirectoryEmailProp() throws Exception
    {
        executeMockedLDAPSync(false);
    }

    private void executeMockedLDAPSync(boolean withEmail) throws NamingException, Exception
    {
        MockUserRegistry mockUserRegistry = new MockUserRegistry("Z0", new NodeDescription[] {}, new NodeDescription[] {});

        MockLDAPUserRegistry mockLDAPUserRegistry = new MockLDAPUserRegistry(mockUserRegistry);

        LDAPInitialDirContextFactoryImpl mockedLdapInitialDirContextFactory = getMockedLDAPSearchResult(withEmail);

        mockLDAPUserRegistry.setLDAPInitialDirContextFactory(mockedLdapInitialDirContextFactory);
        mockLDAPUserRegistry.setEnableProgressEstimation(false);
        mockLDAPUserRegistry.setUserIdAttributeName("sAMAccountName");
        Map<String, String> personAttributeMapping = getMockedLdapAttributeMapping();
        mockLDAPUserRegistry.setPersonAttributeMapping(personAttributeMapping);
        mockLDAPUserRegistry.setNamespaceService(this.namespaceService);

        mockLDAPUserRegistry.afterPropertiesSet();

        this.applicationContextManager.setUserRegistries(mockLDAPUserRegistry);

        ChainingUserRegistrySynchronizer chainingSynchronizer = (ChainingUserRegistrySynchronizer) this.synchronizer;

        chainingSynchronizer.synchronize(false, false);
    }

    private LDAPInitialDirContextFactoryImpl getMockedLDAPSearchResult(boolean withEmail) throws NamingException
    {
        @SuppressWarnings("unchecked")
        NamingEnumeration<SearchResult> mockedNamingEnumeration = mock(NamingEnumeration.class);
        when(mockedNamingEnumeration.hasMore()).thenReturn(true).thenReturn(false);

        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("sAMAccountName", "U1"));
        attributes.put(new BasicAttribute("givenName", "U1"));
        if (withEmail)
        {
            attributes.put(new BasicAttribute("mail", "U1@alfresco.com"));
        }
        SearchResult mockedSearchResult = new SearchResult("CN:U1", null, attributes);
        mockedSearchResult.setNameInNamespace("CN:U1");

        when(mockedNamingEnumeration.next()).thenReturn(mockedSearchResult);

        InitialDirContext mockedInitialDirContext = mock(InitialDirContext.class);
        when(mockedInitialDirContext.search((String)any(), anyString(), any(SearchControls.class))).thenReturn(mockedNamingEnumeration);

        LDAPInitialDirContextFactoryImpl mockedLdapInitialDirContextFactory = mock(LDAPInitialDirContextFactoryImpl.class);
        when(mockedLdapInitialDirContextFactory.getDefaultIntialDirContext(0)).thenReturn(mockedInitialDirContext);
        return mockedLdapInitialDirContextFactory;
    }

    private Map<String, String> getMockedLdapAttributeMapping()
    {
        Map<String, String> personAttributeMapping = new HashMap<>();
        personAttributeMapping.put("cm:userName", "sAMAccountName");
        personAttributeMapping.put("cm:firstName", "givenName");
        personAttributeMapping.put("cm:email", "mail");
        return personAttributeMapping;
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
        ChainingUserRegistrySynchronizerTest.this.synchronizer.synchronize(true, true);
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

    private NodeDescription newPersonWithUserAccountStatusProperty(String userName, String userAccountPropertyValue)
    {
        NodeDescription person = newPerson(userName, userName + "@somedomain.com");

        person.getProperties().put(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "userAccountStatusProperty"), userAccountPropertyValue);
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
        if (zone == null)
        {
            Set<String> zones = new TreeSet<String>();
            zones.add(AuthorityService.ZONE_APP_DEFAULT);
            zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
            assertEquals(zones, this.authorityService.getAuthorityZones(longName));
        }
        else
        {
            assertTrue(this.authorityService.getAuthorityZones(longName).contains(
                    AuthorityService.ZONE_AUTH_EXT_PREFIX + zone));
        }

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
     * @param name
     *            the person name
     * @param displayName
     *            the display name
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
     * A Test {@link ApplicationListener} that checks SyncEndTime before the SynchronizeEndEvent and SynchronizeDirectoryEndEvent events.
     */
    public static class TestSynchronizeEventListener implements ApplicationListener<ApplicationEvent>
    {
        private ChainingUserRegistrySynchronizer synchronizer;
        
        public void setSynchronizer(ChainingUserRegistrySynchronizer synchronizer)
        {
            this.synchronizer = synchronizer;
        }

        @Override
        public void onApplicationEvent(ApplicationEvent event)
        {
            if (SynchronizeEndEvent.class.isAssignableFrom(event.getClass())
                    || SynchronizeDirectoryEndEvent.class.isAssignableFrom(event.getClass()))
            {
                assertEquals(null, this.synchronizer.getSyncEndTime());
            }
        }
        
    }

    public static interface IMockUserRegistry extends UserRegistry, ActivateableBean
    {
        /**
         * Gets the zone id.
         * 
         * @return the zoneId
         */
        String getZoneId();

        /**
         * Modifies the state to match the arguments. Compares new with old and
         * records new modification dates only for changes.
         * 
         * @param persons
         *            the persons
         * @param groups
         *            the groups
         */
        void updateState(Collection<NodeDescription> persons, Collection<NodeDescription> groups);
    }

    /**
     * A Mock {@link UserRegistry} that returns a fixed set of users and groups.
     */
    public static class MockUserRegistry implements IMockUserRegistry
    {
        private boolean isActive = true;
        
        private boolean throwError = false;

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
        


        @Override
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
         * 
         * @param throwError boolean
         */
        public void setThrowError(boolean throwError)
        {
            this.throwError = throwError;
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

        @Override
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
            if(throwError)
            {
                throw new AlfrescoRuntimeException("test error");
            }
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

        @Override
        public boolean isActive()
        {
            return isActive;
        }
        
        public void setActive(boolean isActive)
        {
            this.isActive = isActive;
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
        public void setUserRegistries(IMockUserRegistry... registries)
        {
            this.contexts = new LinkedHashMap<String, ApplicationContext>(registries.length * 2);
            for (IMockUserRegistry registry : registries)
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
            IMockUserRegistry registry = (IMockUserRegistry) context.getBean("userRegistry");
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
         * @param persons
         *            the persons
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
