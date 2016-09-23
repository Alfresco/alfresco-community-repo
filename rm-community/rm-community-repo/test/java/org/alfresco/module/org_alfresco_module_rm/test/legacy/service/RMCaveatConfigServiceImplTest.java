/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.PropertyMap;

/**
 * Test of RM Caveat (Admin facing scripts)
 *
 * @author Mark Rogers
 */
public class RMCaveatConfigServiceImplTest extends BaseSpringTest implements DOD5015Model
{
	protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

	private NodeRef filePlan;

	private NodeService nodeService;
	private TransactionService transactionService;
	private RMCaveatConfigService caveatConfigService;

	private MutableAuthenticationService authenticationService;
	private PersonService personService;
	private AuthorityService authorityService;


	// example base test data for supplemental markings list
	protected final static String NOFORN     = "NOFORN";     // Not Releasable to Foreign Nationals/Governments/Non-US Citizens
	protected final static String NOCONTRACT = "NOCONTRACT"; // Not Releasable to Contractors or Contractor/Consultants
	protected final static String FOUO       = "FOUO";       // For Official Use Only
	protected final static String FGI        = "FGI";        // Foreign Government Information

	protected final static String RM_LIST = "rmc:smList"; // existing pre-defined list
	protected final static String RM_LIST_ALT = "rmc:anoList";

	@Override
	protected void onSetUpInTransaction() throws Exception
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
		this.nodeService = (NodeService)this.applicationContext.getBean("NodeService"); // use upper 'N'odeService (to test access config interceptor)
		this.authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("AuthenticationService");
		this.personService = (PersonService)this.applicationContext.getBean("PersonService");
		this.authorityService = (AuthorityService)this.applicationContext.getBean("AuthorityService");
		this.caveatConfigService = (RMCaveatConfigServiceImpl)this.applicationContext.getBean("caveatConfigService");
		this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");


		// Set the current security context as admin
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		// Get the test data
		setUpTestData();
	}

	private void setUpTestData()
	{
	}

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        try
        {
            UserTransaction txn = transactionService.getUserTransaction(false);
            txn.begin();
            this.nodeService.deleteNode(filePlan);
            txn.commit();
        }
        catch (Exception e)
        {
            // Nothing
            //System.out.println("DID NOT DELETE FILE PLAN!");
        }
    }

    @Override
    protected void onTearDownAfterTransaction() throws Exception
    {
        // TODO Auto-generated method stub
        super.onTearDownAfterTransaction();
    }

    public void testSetup()
    {
        // NOOP
    }


    /**
     * Test of Caveat Config
     *
     * @throws Exception
     */
    public void testAddRMConstraintList() throws Exception
    {
        setComplete();
        endTransaction();

        cleanCaveatConfigData();

        startNewTransaction();

        /**
         * Now remove the entire list (rma:smList);
         */
        logger.debug("test remove entire list rmc:smList");
        caveatConfigService.deleteRMConstraint(RM_LIST);

        /**
         * Now add the list again
         */
        logger.debug("test add back rmc:smList");
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Negative test - add a list that already exists
         */
        logger.debug("try to create duplicate list rmc:smList");
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Negative test - remove a list that does not exist
         */
        logger.debug("test remove entire list rmc:smList");
        caveatConfigService.deleteRMConstraint(RM_LIST);
        try
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
            fail("unknown constraint should have thrown an exception");
        }
        catch (Exception e)
        {
            // expect to go here
        }


        /**
         * Negative test - add a constraint to property that does not exist
         */
        logger.debug("test property does not exist");
        try
        {
            caveatConfigService.addRMConstraint("rma:mer", "", new String[0]);
            fail("unknown property should have thrown an exception");
        }
        catch (Exception e)
        {
            // expect to go here
        }
        endTransaction();
        cleanCaveatConfigData();

    }

    /**
     * Test of addRMConstraintListValue
     *
     * @throws Exception
     */
    public void testAddRMConstraintListValue() throws Exception
    {
        setComplete();
        endTransaction();

        cleanCaveatConfigData();
        setupCaveatConfigData();

        startNewTransaction();
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Add a user to the list
         */
        List<String> values = new ArrayList<String>();
        values.add(NOFORN);
        values.add(NOCONTRACT);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);

        /**
         * Add another value to that list
         */
        caveatConfigService.addRMConstraintListValue(RM_LIST, "jrogers", FGI);

        /**
         * Negative test - attempt to add a duplicate value
         */
        caveatConfigService.addRMConstraintListValue(RM_LIST, "jrogers", FGI);

        /**
         * Negative test - attempt to add to a list that does not exist
         */
        try
        {
            caveatConfigService.addRMConstraintListValue(RM_LIST_ALT, "mhouse", FGI);
            fail("exception not thrown");
        }
        catch (Exception re)
        {
            // should go here

        }

        /**
         * Negative test - attempt to add to a list that does exist and user that does not exist
         */
        try
        {
            caveatConfigService.addRMConstraintListValue(RM_LIST, "mhouse", FGI);
            fail("exception not thrown");
        }
        catch (Exception e)
        {
            // should go here
        }

    }


    /**
     * Test of UpdateRMConstraintListAuthority
     *
     * @throws Exception
     */
    public void testUpdateRMConstraintListAuthority() throws Exception
    {
        setComplete();
        endTransaction();

        cleanCaveatConfigData();
        setupCaveatConfigData();

        startNewTransaction();

        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Add a user to the list
         */
        List<String> values = new ArrayList<String>();
        values.add(NOFORN);
        values.add(NOCONTRACT);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);

        /**
         * Add to a authority that already exists
         * Should replace existing authority
         */
        List<String> updatedValues = new ArrayList<String>();
        values.add(FGI);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", updatedValues);

        /**
         * Add a group to the list
         */
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "Engineering", values);

        /**
         * Add to a list that does not exist
         * Should create a new list
         */
        caveatConfigService.deleteRMConstraint(RM_LIST);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);


        /**
         * Add to a authority that already exists
         * Should replace existing authority
         */

        endTransaction();
        cleanCaveatConfigData();

    }

    /**
     * Test of RemoveRMConstraintListAuthority
     *
     * @throws Exception
     */
    public void testRemoveRMConstraintListAuthority() throws Exception
    {
        setComplete();
        endTransaction();

        cleanCaveatConfigData();
        setupCaveatConfigData();

        startNewTransaction();
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        List<String> values = new ArrayList<String>();
        values.add(FGI);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);

        /**
         * Remove a user from a list
         */
        caveatConfigService.removeRMConstraintListAuthority(RM_LIST, "jrogers");

        /**
         * Negative test - remove a user that does not exist
         */
        caveatConfigService.removeRMConstraintListAuthority(RM_LIST, "jrogers");

        /**
         * Negative test - remove a user from a list that does not exist.
         * Should create a new list
         */

        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);

        endTransaction();
        cleanCaveatConfigData();

    }




    /**
     * Test of Caveat Config
     *
     * @throws Exception
     */
    public void testRMCaveatConfig() throws Exception
    {
        setComplete();
        endTransaction();

        cleanCaveatConfigData();

        startNewTransaction();

        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        List<String> values = new ArrayList<String>();
        values.add(NOFORN);
        values.add(FOUO);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "dfranco", values);

        values.add(FGI);
        values.add(NOCONTRACT);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "dmartinz", values);

        // Test list of allowed values for caveats

        List<String> allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
        {
            public List<String> doWork()
            {
                // get allowed values for given caveat (for current user)
                return caveatConfigService.getRMAllowedValues(RM_LIST);
            }
        }, "dfranco");

        assertEquals(2, allowedValues.size());
        assertTrue(allowedValues.contains(NOFORN));
        assertTrue(allowedValues.contains(FOUO));


        allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
        {
            public List<String> doWork()
            {
                // get allowed values for given caveat (for current user)
                return caveatConfigService.getRMAllowedValues(RM_LIST);
            }
        }, "dmartinz");

        assertEquals(4, allowedValues.size());
        assertTrue(allowedValues.contains(NOFORN));
        assertTrue(allowedValues.contains(NOCONTRACT));
        assertTrue(allowedValues.contains(FOUO));
        assertTrue(allowedValues.contains(FGI));

        /**
        //
         * Now remove the entire list (rma:smList);
         */
        logger.debug("test remove entire list rmc:smList");
        caveatConfigService.deleteRMConstraint(RM_LIST);


        /**
         * Now add the list again
         */
        logger.debug("test add back rmc:smList");
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Negative test - add a list that already exists
         */
        logger.debug("try to create duplicate list rmc:smList");
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Negative test - remove a list that does not exist
         */
        logger.debug("test remove entire list rmc:smList");
        caveatConfigService.deleteRMConstraint(RM_LIST);
        try
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
            fail("unknown constraint should have thrown an exception");
        }
        catch (Exception e)
        {
            // expect to go here
        }


        /**
         * Negative test - add a constraint to property that does not exist
         */
        logger.debug("test property does not exist");
        try
        {
            caveatConfigService.addRMConstraint("rma:mer", "", new String[0]);
            fail("unknown property should have thrown an exception");
        }
        catch (Exception e)
        {
            // expect to go here
        }
        endTransaction();
        cleanCaveatConfigData();
    }

    private void cleanCaveatConfigData()
    {
        startNewTransaction();

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        deleteUser("jrangel");
        deleteUser("dmartinz");
        deleteUser("jrogers");
        deleteUser("hmcneil");
        deleteUser("dfranco");
        deleteUser("gsmith");
        deleteUser("eharris");
        deleteUser("bbayless");
        deleteUser("mhouse");
        deleteUser("aly");
        deleteUser("dsandy");
        deleteUser("driggs");
        deleteUser("test1");

        deleteGroup("Engineering");
        deleteGroup("Finance");
        deleteGroup("test1");

        caveatConfigService.updateOrCreateCaveatConfig("{}"); // empty config !

        setComplete();
        endTransaction();
    }

    private void setupCaveatConfigData()
    {
        startNewTransaction();

        // Switch to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Create test users/groups (if they do not already exist)

        createUser("jrangel");
        createUser("dmartinz");
        createUser("jrogers");
        createUser("hmcneil");
        createUser("dfranco");
        createUser("gsmith");
        createUser("eharris");
        createUser("bbayless");
        createUser("mhouse");
        createUser("aly");
        createUser("dsandy");
        createUser("driggs");
        createUser("test1");

        createGroup("Engineering");
        createGroup("Finance");
        createGroup("test1");

        addToGroup("jrogers", "Engineering");
        addToGroup("dfranco", "Finance");

        // not in grouo to start with - added later
        //addToGroup("gsmith", "Engineering");


        //URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("testCaveatConfig2.json"); // from test-resources
        //assertNotNull(url);
        //File file = new File(url.getFile());
        //assertTrue(file.exists());

        //caveatConfigService.updateOrCreateCaveatConfig(file);

        setComplete();
        endTransaction();
    }

    protected void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }

        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    protected void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }

    protected void createGroup(String groupShortName)
    {
        createGroup(null, groupShortName);
    }

    protected void createGroup(String parentGroupShortName, String groupShortName)
    {
        if (parentGroupShortName != null)
        {
            String parentGroupFullName = authorityService.getName(AuthorityType.GROUP, parentGroupShortName);
            if (!authorityService.authorityExists(parentGroupFullName))
            {
                authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
                authorityService.addAuthority(parentGroupFullName, groupShortName);
            }
        }
        else
        {
            authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
        }
    }

    protected void deleteGroup(String groupShortName)
    {
        String groupFullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        if (authorityService.authorityExists(groupFullName))
        {
            authorityService.deleteAuthority(groupFullName);
        }
    }

    protected void addToGroup(String authorityName, String groupShortName)
    {
        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, groupShortName), authorityName);
    }

    protected void removeFromGroup(String authorityName, String groupShortName)
    {
        authorityService.removeAuthority(authorityService.getName(AuthorityType.GROUP, groupShortName), authorityName);
    }

}
