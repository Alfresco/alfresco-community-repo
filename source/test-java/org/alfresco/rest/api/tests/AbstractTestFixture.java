package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.util.JettyComponent;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.springframework.context.ApplicationContext;

public abstract class AbstractTestFixture implements TestFixture
{
	public static final int DEFAULT_NUM_MEMBERS_PER_SITE = 4;
	
	public static final String TEST_DOMAIN_PREFIX = "acme";

    protected List<PersonInfo> people = new ArrayList<PersonInfo>(10);
	protected TreeMap<String, TestNetwork> networks = new TreeMap<String, TestNetwork>();
    protected TreeMap<String, SiteInformation> sites = new TreeMap<String, SiteInformation>();

    protected String[] configLocations;
    protected final String[] classLocations;
    protected int port = 8081;
    protected String contextPath = "/alfresco";
    protected String servletName = "api";
    protected String hostname = "localhost";
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
	
	protected abstract JettyComponent makeJettyComponent();
	protected abstract void populateTestData();
	protected abstract RepoService makeRepoService() throws Exception;

	public void setup() throws Exception
	{
		this.jetty = makeJettyComponent();
		this.jetty.start();
		this.applicationContext = jetty.getApplicationContext();
    	this.repoService = makeRepoService();
		this.transactionHelper = (RetryingTransactionHelper)repoService.getApplicationContext().getBean("retryingTransactionHelper");
		
		populateTestData();

    	createTestData();
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
		for(final TestNetwork testAccount : getNetworks().values())
		{
	        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
            	@SuppressWarnings("synthetic-access")
            	public Void execute() throws Throwable
            	{
        	        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        	        testAccount.create();

        			for(PersonInfo testPerson : people)
        			{
        				testAccount.createUser(testPerson);
        			}

        			return null;
            	}
            }, false, true);

	        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
            	@SuppressWarnings("synthetic-access")
            	public Void execute() throws Throwable
            	{
					// clear fully authenticated user ("admin") - affects site creation (which uses this to setup perms)
					AuthenticationUtil.clearCurrentSecurityContext();
					
					if(testAccount.getPersonIds().size() > 0)
					{
						// use a fixed sample size of account members (so we have some left over for tests)
						List<String> people = testAccount.peopleSample(testAccount.getPersonIds().size());
			
						String tenantDomain = testAccount.getId();
	
						int i = 0;
						for(final SiteInformation site : getSites().values())
						{
	    					final Iterator<String> peopleIterator = RepoService.getWrappingIterator(i++, people);
	
							final String siteCreator = peopleIterator.next();
			        		assertNotNull(siteCreator);
	
			    			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
							{
								@Override
								public Void doWork() throws Exception
								{
									final TestSite testSite = testAccount.createSite(site);
	
									// invite some members to the site, leave at least one non-site member
									for(int j = 0; j < numMembersPerSite; j++)
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
    	if(cleanup)
    	{
    		repoService.shutdown();
    	}

    	this.jetty.shutdown();
    }
}
