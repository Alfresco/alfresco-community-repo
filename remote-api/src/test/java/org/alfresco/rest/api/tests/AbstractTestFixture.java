/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.util.JettyComponent;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.data.SiteRole;

public abstract class AbstractTestFixture implements TestFixture
{
    public static final int DEFAULT_NUM_MEMBERS_PER_SITE = 4;

    public static final String TEST_DOMAIN_PREFIX = "acme";

    protected List<PersonInfo> people = new ArrayList<PersonInfo>(10);
    protected TreeMap<String, TestNetwork> networks = new TreeMap<String, TestNetwork>();
    protected TreeMap<String, SiteInformation> sites = new TreeMap<String, SiteInformation>();

    private String[] configLocations;
    private final String[] classLocations;
    private int port = PORT;
    private String contextPath = CONTEXT_PATH;
    private String servletName = PUBLIC_API_SERVLET_NAME;

    protected JettyComponent jetty;
    protected boolean cleanup;
    protected Random random = new Random();
    protected RepoService repoService;
    protected RetryingTransactionHelper transactionHelper;
    protected int numMembersPerSite;
    protected ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    protected AbstractTestFixture(String[] configLocations, String[] classLocations, int port, String contextPath, String servletName,
            int numMembersPerSite, boolean cleanup)
    {
        this.configLocations = configLocations;
        this.classLocations = classLocations;
        this.port = port;
        this.contextPath = contextPath;
        this.servletName = servletName;
        this.numMembersPerSite = numMembersPerSite;
        this.cleanup = cleanup;
    }

    public JettyComponent getJettyComponent()
    {
        return jetty;
    }

    public int getPort()
    {
        return port;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getServletName()
    {
        return servletName;
    }

    public String[] getConfigLocations()
    {
        return configLocations;
    }

    public String[] getClassLocations()
    {
        return classLocations;
    }

    protected abstract JettyComponent makeJettyComponent();

    protected abstract void populateTestData();

    protected abstract RepoService makeRepoService() throws Exception;

    public void setup() throws Exception
    {
        setup(true);
    }

    public void setup(boolean createTestData) throws Exception
    {
        this.jetty = makeJettyComponent();
        this.jetty.start();
        this.applicationContext = jetty.getApplicationContext();
        this.repoService = makeRepoService();
        this.transactionHelper = (RetryingTransactionHelper) repoService.getApplicationContext().getBean("retryingTransactionHelper");

        if (createTestData)
        {
            populateTestData();
            createTestData();
        }
    }

    public RepoService getRepoService()
    {
        return repoService;
    }

    public TestNetwork getNetwork(String name)
    {
        return networks.get(name);
    }

    public Iterator<TestNetwork> getNetworksIt()
    {
        return networks.values().iterator();
    }

    public TreeMap<String, TestNetwork> getNetworks()
    {
        return networks;
    }

    public Iterator<TestNetwork> networksIterator()
    {
        return networks.values().iterator();
    }

    public TestNetwork getRandomNetwork()
    {
        if (networks.isEmpty())
        {
            populateTestData();
            createTestData();
        }
        int r = random.nextInt(networks.size());
        List<TestNetwork> a = new ArrayList<TestNetwork>();
        a.addAll(networks.values());
        return a.get(r);
    }

    protected void addNetwork(TestNetwork network)
    {
        networks.put(network.getId(), network);
    }

    public void addPerson(PersonInfo personInfo)
    {
        people.add(personInfo);
    }

    public void addSite(SiteInformation siteInfo)
    {
        sites.put(siteInfo.getShortName(), siteInfo);
    }

    public TreeMap<String, SiteInformation> getSites()
    {
        return sites;
    }

    public void createTestData()
    {
        for (final TestNetwork testAccount : getNetworks().values())
        {
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                @SuppressWarnings("synthetic-access")
                public Void execute() throws Throwable
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                    testAccount.create();

                    for (PersonInfo testPerson : people)
                    {
                        testAccount.createUser(testPerson);
                    }

                    return null;
                }
            }, false, true);

            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                @SuppressWarnings("synthetic-access")
                public Void execute() throws Throwable
                {
                    // clear fully authenticated user ("admin") - affects site creation (which uses this to setup perms)
                    AuthenticationUtil.clearCurrentSecurityContext();

                    if (testAccount.getPersonIds().size() > 0)
                    {
                        // use a fixed sample size of account members (so we have some left over for tests)
                        List<String> people = testAccount.peopleSample(testAccount.getPersonIds().size());

                        String tenantDomain = testAccount.getId();

                        int i = 0;
                        for (final SiteInformation site : getSites().values())
                        {
                            final Iterator<String> peopleIterator = RepoService.getWrappingIterator(i++, people);

                            final String siteCreator = peopleIterator.next();
                            assertNotNull(siteCreator);

                            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>() {
                                @Override
                                public Void doWork() throws Exception
                                {
                                    final TestSite testSite = testAccount.createSite(site);

                                    // invite some members to the site, leave at least one non-site member
                                    for (int j = 0; j < numMembersPerSite; j++)
                                    {
                                        String siteMember = peopleIterator.next();
                                        assertNotNull(siteMember);
                                        testSite.inviteToSite(siteMember, SiteRole.SiteContributor);
                                    }

                                    return null;
                                }
                            }, siteCreator, tenantDomain);
                        }
                    }

                    return null;
                }
            }, false, true);
        }
    }

    public void shutdown()
    {
        if (cleanup)
        {
            repoService.shutdown();
        }

        this.jetty.shutdown();
    }
}
