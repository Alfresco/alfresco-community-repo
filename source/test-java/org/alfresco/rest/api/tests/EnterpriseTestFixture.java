package org.alfresco.rest.api.tests;

import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.service.cmr.site.SiteVisibility;

public abstract class EnterpriseTestFixture extends AbstractTestFixture
{
    public EnterpriseTestFixture(String[] configLocations, String[] classLocations, int port, String contextPath,
    		String servletName, int numMembersPerSite, boolean cleanup)
    {
		super(configLocations, classLocations, port, contextPath, servletName, numMembersPerSite, cleanup);
	}

	@Override
	protected void populateTestData()
	{
		for(int i = 1; i <= 2; i++)
        {
			TestNetwork network = repoService.createNetworkWithAlias(TEST_DOMAIN_PREFIX + "00" + i, true);
			addNetwork(network);
        }

        // 5 public sites
        for(int i = 0; i < 5;  i++)
        {
        	SiteInformation siteInfo = new SiteInformation("testSite" + i, "Public Test Site" + i, "Public Test Site" + i, SiteVisibility.PUBLIC);
        	addSite(siteInfo);
        }

        // 5 private sites
        for(int i = 5; i < 10;  i++)
        {
        	SiteInformation siteInfo = new SiteInformation("testSite" + i, "Private Test Site" + i, "Private Test Site" + i, SiteVisibility.PRIVATE);
        	addSite(siteInfo);
        }

    	addPerson(new PersonInfo("David", "Smith", "david.smith", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("Bob", "Jones", "bob.jones", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("Bill", "Grainger", "bill.grainger", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("Jill", "Fry", "jill.fry", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("Elvis", "Presley", "elvis.presley", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("John", "Lennon", "john.lennon", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("George", "Harrison", "george.harrison", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("David", "Bowie", "david.bowie", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
    	addPerson(new PersonInfo("Ford", "Prefect", "ford.prefect", "password", null, "skype", "location", "telephone", "mob", "instant", "google"));
	}
}
