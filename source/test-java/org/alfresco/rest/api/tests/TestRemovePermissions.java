package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TestRemovePermissions extends EnterpriseTestApi
{
    private static Log logger = LogFactory.getLog(TestRemovePermissions.class);

    static public final String ADMIN_USER = "admin";
    static public final String ADMIN_PASSWORD = "admin";
    static public final String DEFAULT_HOSTNAME = "localhost";

    static public final String ATOMPUB_URL_OC = "http://{0}:{1}/alfresco/cmisatom";
    static public final String ATOMPUB_URL_11 = "http://{0}:{1}/alfresco/api/-default-/public/cmis/versions/1.1/atom";

    static public final String REPOSITORYSERVICE_URL = "http://{0}:{1}/alfresco/cmis/RepositoryService?wsdl";
    static public final String NAVIGATIONSERVICE_URL = "http://{0}:{1}/alfresco/cmis/NavigationService?wsdl";
    static public final String OBJECTSERVICE_URL = "http://{0}:{1}/alfresco/cmis/ObjectService?wsdl";
    static public final String VERSIONINGSERVICE_URL = "http://{0}:{1}/alfresco/cmis/VersioningService?wsdl";
    static public final String DISCOVERYSERVICE_URL = "http://{0}:{1}/alfresco/cmis/DiscoveryService?wsdl";
    static public final String MULTIFILINGSERVICE_URL = "http://{0}:{1}/alfresco/cmis/MultiFilingService?wsdl";
    static public final String RELATIONSHIPSERVICE_URL = "http://{0}:{1}/alfresco/cmis/RelationshipService?wsdl";
    static public final String ACLSERVICE_URL = "http://{0}:{1}/alfresco/cmis/ACLService?wsdl";
    static public final String POLICYSERVICE_URL = "http://{0}:{1}/alfresco/cmis/PolicyService?wsdl";

    static public final String REPOSITORYSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/cmis?wsdl";
    static public final String NAVIGATIONSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/NavigationService?wsdl";
    static public final String OBJECTSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/ObjectService?wsdl";
    static public final String VERSIONINGSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/VersioningService?wsdl";
    static public final String DISCOVERYSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/DiscoveryService?wsdl";
    static public final String MULTIFILINGSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/MultiFilingService?wsdl";
    static public final String RELATIONSHIPSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/RelationshipService?wsdl";
    static public final String ACLSERVICE_OC_URL = "http://{0}:{1}//alfresco/cmisws/ACLService?wsdl";
    static public final String POLICYSERVICE_OC_URL = "http://{0}:{1}/alfresco/cmisws/PolicyService?wsdl";

    static public final String BROWSE_URL_11 = "http://{0}:{1}/alfresco/api/-default-/public/cmis/versions/1.1/browser";

    protected Session getATOMPUB_10_Session()
    {
        try
        {
            Map<String, String> parameters = new HashMap<String, String>();
            int port = getTestFixture().getJettyComponent().getPort();

            parameters.put(SessionParameter.USER, ADMIN_USER);
            parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
            parameters.put(SessionParameter.ATOMPUB_URL, MessageFormat.format(ATOMPUB_URL_OC, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());
            return sessionFactory.createSession(parameters);
        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return null;
    }

    protected Session getATOMPUB_11_Session()
    {
        try
        {
            Map<String, String> parameters = new HashMap<String, String>();
            int port = getTestFixture().getJettyComponent().getPort();

            parameters.put(SessionParameter.USER, ADMIN_USER);
            parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
            parameters.put(SessionParameter.ATOMPUB_URL, MessageFormat.format(ATOMPUB_URL_11, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());
            return sessionFactory.createSession(parameters);
        }
        catch (Exception ex)
        {
            logger.error(ex);

        }
        return null;
    }

    protected Session getBROWSER_11_Session()
    {
        try
        {
            Map<String, String> parameter = new HashMap<String, String>();
            int port = getTestFixture().getJettyComponent().getPort();

            parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
            parameter.put(SessionParameter.BROWSER_URL, MessageFormat.format(BROWSE_URL_11, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameter.put(SessionParameter.COOKIES, "true");

            parameter.put(SessionParameter.USER, ADMIN_USER);
            parameter.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);

            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            parameter.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameter).get(0).getId());
            return sessionFactory.createSession(parameter);
        }
        catch (Exception ex)
        {
            logger.error(ex);

        }
        return null;
    }

    protected Session getWEBSERVICE_10_Session()
    {
        try
        {
            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
            int port = 8080;

            Map<String, String> parameters = new HashMap<String, String>();

            parameters.put(SessionParameter.USER, ADMIN_USER);
            parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());

            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE,
                    MessageFormat.format(ACLSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
                    MessageFormat.format(DISCOVERYSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,
                    MessageFormat.format(MULTIFILINGSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,
                    MessageFormat.format(NAVIGATIONSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE,
                    MessageFormat.format(OBJECTSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE,
                    MessageFormat.format(POLICYSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
                    MessageFormat.format(RELATIONSHIPSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE,
                    MessageFormat.format(REPOSITORYSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE,
                    MessageFormat.format(REPOSITORYSERVICE_OC_URL, DEFAULT_HOSTNAME, String.valueOf(port)));
            parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());

            return sessionFactory.createSession(parameters);
        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return null;
    }

    /**
     * cmisws?wsdl is not available using jetty in automated test suite should
     * be runned using an external alfresco server
     * 
     */
    // @Test
    public void testRemoveAllPermissions_WEBSERVICE_10()
    {
        Folder testFolder = null;
        try
        {
            Session session = getWEBSERVICE_10_Session();
            if (session == null)
            {
                fail("WEBSERVICE 1.0 session cannot be null");
            }
            testFolder = createFolder(session, "testRemoveAllPermissions_WEBSERVICE_10");
            List<Ace> acl = create2TestACLs(session);

            // adding new ACE
            testFolder.addAcl(acl, AclPropagation.PROPAGATE);

            Acl allacl = session.getAcl(session.createObjectId(testFolder.getId()), false);
            int oldSize = allacl.getAces().size();

            // Removing ALL ACEs
            Acl newAcl = testFolder.removeAcl(allacl.getAces(), AclPropagation.PROPAGATE);

            int newsize = newAcl.getAces().size();

            System.out.println("Old ace size -->" + oldSize);
            System.out.println("New ace size --> " + newsize);

            assertTrue(newsize == oldSize - acl.size());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
        finally
        {
            if (testFolder != null)
            {
                testFolder.delete();
            }
        }
    }

    @Test
    public void testRemoveAllPermissions_ATOMPUB_10()
    {
        Folder testFolder = null;
        try
        {
            Session session = getATOMPUB_10_Session();
            if (session == null)
            {
                fail("ATOMPUB 1.0 session cannot be null");
            }
            testFolder = createFolder(session, "testRemoveAllPermissions_ATOMPUB_10");
            List<Ace> acl = create2TestACLs(session);

            // adding new ACE
            testFolder.addAcl(acl, AclPropagation.PROPAGATE);

            Acl allacl = session.getAcl(session.createObjectId(testFolder.getId()), false);
            int oldSize = allacl.getAces().size();

            // Removing ALL ACEs
            Acl newAcl = testFolder.removeAcl(allacl.getAces(), AclPropagation.PROPAGATE);

            int newsize = newAcl.getAces().size();

            System.out.println("Old ace size -->" + oldSize);
            System.out.println("New ace size --> " + newsize);

            assertTrue(newsize == oldSize - acl.size());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
        finally
        {
            if (testFolder != null)
            {
                testFolder.delete();
            }
        }
    }

    @Test
    public void testRemoveAllPermissions_ATOMPUB_11()
    {
        Folder testFolder = null;
        try
        {
            Session session = getATOMPUB_11_Session();
            if (session == null)
            {
                fail("ATOMPUB 1.1 session cannot be null");
            }
            testFolder = createFolder(session, "testRemoveAllPermissions_ATOMPUB_11");
            List<Ace> acl = create2TestACLs(session);

            // adding new ACE
            testFolder.addAcl(acl, AclPropagation.PROPAGATE);

            Acl allacl = session.getAcl(session.createObjectId(testFolder.getId()), false);
            int oldSize = allacl.getAces().size();

            // Removing ALL ACEs
            Acl newAcl = testFolder.removeAcl(allacl.getAces(), AclPropagation.PROPAGATE);

            int newsize = newAcl.getAces().size();

            System.out.println("Old ace size -->" + oldSize);
            System.out.println("New ace size --> " + newsize);

            assertTrue(newsize == oldSize - acl.size());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
        finally
        {
            if (testFolder != null)
            {
                testFolder.delete();
            }
        }

    }

    @Test
    public void testRemoveAllPermissions_BROWSER_11()
    {
        Folder testFolder = null;
        try
        {
            Session session = getBROWSER_11_Session();
            if (session == null)
            {
                fail("ATOMPUB 1.1 session cannot be null");
            }
            testFolder = createFolder(session, "testRemoveAllPermissions_BROWSER_11");
            List<Ace> acl = create2TestACLs(session);

            // adding new ACE
            testFolder.addAcl(acl, AclPropagation.PROPAGATE);

            Acl allacl = session.getAcl(session.createObjectId(testFolder.getId()), false);
            int oldSize = allacl.getAces().size();

            // Removing ALL ACEs

            Acl newAcl = testFolder.removeAcl(allacl.getAces(), AclPropagation.PROPAGATE);
            int newsize = newAcl.getAces().size();

            System.out.println("Old ace size -->" + oldSize);
            System.out.println("New ace size --> " + newsize);

            assertTrue(newsize == oldSize - acl.size());
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
        finally
        {
            if (testFolder != null)
            {
                testFolder.delete();
            }
        }

    }

    /**
     * 
     * @param session Session
     * @param name String
     * @return Folder
     */
    private Folder createFolder(Session session, String name)
    {
        Folder testFolder;
        Folder folder = session.getRootFolder();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, name);

        testFolder = folder.createFolder(properties);

        return testFolder;
    }

    /**
     * 
     * @param session Session
     * @return List<Ace>
     */
    private List<Ace> create2TestACLs(Session session)
    {
        List<Ace> newACE = new ArrayList<Ace>();
        LinkedList<String> permissions1 = new LinkedList<String>();
        permissions1.add("{http://www.alfresco.org/model/system/1.0}base.ReadPermissions");

        LinkedList<String> permissions2 = new LinkedList<String>();
        permissions2.add("{http://www.alfresco.org/model/system/1.0}base.Unlock");

        Ace ace1 = session.getObjectFactory().createAce("testUser1", permissions1);
        Ace ace2 = session.getObjectFactory().createAce("testUser2", permissions2);
        newACE.add(ace1);
        newACE.add(ace2);
        return newACE;

    }

}
