package org.alfresco.rest.api.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

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
import org.junit.Test;

public class TestRemovePermissions extends TestCase
{

    static public final String ADMIN_USER = "admin";
    static public final String ADMIN_PASSWORD = "admin";

    static public final String HOST = "localhost";
    static public final String PORT = "8080";

    static public final String ATOMPUB_URL = "http://" + HOST + ":" + PORT + "/alfresco/s/cmis";
    static public final String ATOMPUB_URL_OC = "http://" + HOST + ":" + PORT + "/alfresco/cmisatom";
    static public final String ATOMPUB_URL_11 = "http://" + HOST + ":" + PORT + "/alfresco/api/-default-/public/cmis/versions/1.1/atom";

    static public final String REPOSITORYSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/RepositoryService?wsdl";
    static public final String NAVIGATIONSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/NavigationService?wsdl";
    static public final String OBJECTSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/ObjectService?wsdl";
    static public final String VERSIONINGSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/VersioningService?wsdl";
    static public final String DISCOVERYSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/DiscoveryService?wsdl";
    static public final String MULTIFILINGSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/MultiFilingService?wsdl";
    static public final String RELATIONSHIPSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/RelationshipService?wsdl";
    static public final String ACLSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/ACLService?wsdl";
    static public final String POLICYSERVICE_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmis/PolicyService?wsdl";

    static public final String REPOSITORYSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/RepositoryService?wsdl";
    static public final String NAVIGATIONSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/NavigationService?wsdl";
    static public final String OBJECTSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/ObjectService?wsdl";
    static public final String VERSIONINGSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/VersioningService?wsdl";
    static public final String DISCOVERYSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/DiscoveryService?wsdl";
    static public final String MULTIFILINGSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/MultiFilingService?wsdl";
    static public final String RELATIONSHIPSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/RelationshipService?wsdl";
    static public final String ACLSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/ACLService?wsdl";
    static public final String POLICYSERVICE_OC_URL = "http://" + HOST + ":" + PORT + "/alfresco/cmisws/PolicyService?wsdl";

    static public final String BROWSE_URL_11 = "http://" + HOST + ":" + PORT + "/alfresco/api/-default-/public/cmis/versions/1.1/browser";

    public static Session getATOMPUB_10_Session()
    {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(SessionParameter.USER, ADMIN_USER);
        parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
        parameters.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL_OC);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());
        return sessionFactory.createSession(parameters);
    }

    public static Session getATOMPUB_11_Session()
    {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(SessionParameter.USER, ADMIN_USER);
        parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
        parameters.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL_11);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());
        return sessionFactory.createSession(parameters);
    }

    public static Session getBROWSER_11_Session()
    {
        Map<String, String> parameter = new HashMap<String, String>();

        parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
        parameter.put(SessionParameter.BROWSER_URL, BROWSE_URL_11);
        parameter.put(SessionParameter.COOKIES, "true");

        parameter.put(SessionParameter.USER, ADMIN_USER);
        parameter.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        parameter.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameter).get(0).getId());
        return sessionFactory.createSession(parameter);
    }

    public static Session getWEBSERVICE_10_Session()
    {
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(SessionParameter.USER, ADMIN_USER);
        parameters.put(SessionParameter.PASSWORD, ADMIN_PASSWORD);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
        parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, ACLSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, DISCOVERYSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, MULTIFILINGSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, NAVIGATIONSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, OBJECTSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, POLICYSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, RELATIONSHIPSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, REPOSITORYSERVICE_OC_URL);
        parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, REPOSITORYSERVICE_OC_URL);
        parameters.put(SessionParameter.REPOSITORY_ID, sessionFactory.getRepositories(parameters).get(0).getId());

        return sessionFactory.createSession(parameters);
    }

    @Override
    protected void setUp() throws Exception
    {

    }

    @Test
    public void testRemoveAllPermissions_WEBSERVICE_10()
    {
        System.out.println("testRemoveAllPermissions_WEBSERVICE_10");
        Session session = getWEBSERVICE_10_Session();
        Folder testFolder = createFolder(session, "testRemoveAllPermissions_WEBSERVICE_10");
        try
        {
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
        finally
        {
            testFolder.delete();
        }
    }

    @Test
    public void testRemoveAllPermissions_ATOMPUB_10()
    {
        System.out.println("testRemoveAllPermissions_ATOMPUB_10");
        Session session = getATOMPUB_10_Session();
        Folder testFolder = createFolder(session, "testRemoveAllPermissions_ATOMPUB_10");
        try
        {
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
        finally
        {
            testFolder.delete();
        }
    }

    @Test
    public void testRemoveAllPermissions_ATOMPUB_11()
    {
        System.out.println("testRemoveAllPermissions_ATOMPUB_11");
        Session session = getATOMPUB_11_Session();
        Folder testFolder = createFolder(session, "testRemoveAllPermissions_ATOMPUB_11");
        try
        {
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
        finally
        {
            testFolder.delete();
        }

    }

    @Test
    public void testRemoveAllPermissions_BROWSER_11()
    {

        System.out.println("testRemoveAllPermissions_BROWSER_11");
        Session session = getBROWSER_11_Session();
        Folder testFolder = createFolder(session, "testRemoveAllPermissions_BROWSER_11");

        try
        {
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
        finally
        {
            testFolder.delete();
        }

    }

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
