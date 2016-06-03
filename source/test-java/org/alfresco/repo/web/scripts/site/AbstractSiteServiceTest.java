package org.alfresco.repo.web.scripts.site;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import com.google.common.collect.Lists;

/**
 * Unit test for the Export Web Script API of the Site Object.
 */
public class AbstractSiteServiceTest extends BaseWebScriptTest
{
    protected MutableAuthenticationService authenticationService;
    protected AuthenticationComponent authenticationComponent;
    protected PersonService personService;

    private static final String URL_SITES = "/api/sites";
    private static final String URL_MEMBERSHIPS = "/memberships";

    protected List<String> createdSites = Lists.newArrayList();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.authenticationComponent = (AuthenticationComponent) getServer()
                .getApplicationContext().getBean("authenticationComponent");
        this.authenticationService = (MutableAuthenticationService) getServer()
                .getApplicationContext().getBean("AuthenticationService");
        this.personService = (PersonService) getServer().getApplicationContext().getBean(
                "PersonService");

        // sets the testMode property to true via spring injection. This will
        // prevent emails
        // from being sent from within this test case.
        this.authenticationComponent.setSystemUserAsCurrentUser();
    }

    protected void deleteSites() throws Exception 
    {
        // Tidy-up any sites create during the execution of the test
        for (String shortName : this.createdSites)
        {
            sendRequest(new DeleteRequest(URL_SITES + "/" + shortName), 0);
        }

        // Clear the list
        this.createdSites.clear();
    }
    
    protected void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            createPerson(userName);
        }
    }

    protected void createPerson(String userName)
    {
        PropertyMap ppOne = new PropertyMap(4);
        ppOne.put(ContentModel.PROP_USERNAME, userName);
        ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
        ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
        ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
        ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

        this.personService.createPerson(ppOne);
    }

    protected void deleteUser(String username)
    {
        this.personService.deletePerson(username);
        if (this.authenticationService.authenticationExists(username))
        {
            this.authenticationService.deleteAuthentication(username);
        }
    }

    protected JSONObject createSite(String sitePreset, String shortName, String title,
            String description, SiteVisibility visibility, int expectedStatus) throws Exception
    {
        JSONObject site = new JSONObject();
        site.put("sitePreset", sitePreset);
        site.put("shortName", shortName);
        site.put("title", title);
        site.put("description", description);
        site.put("visibility", visibility.toString());
        Response response = sendRequest(new PostRequest(URL_SITES, site.toString(),
                "application/json"), expectedStatus);
        this.createdSites.add(shortName);
        return new JSONObject(response.getContentAsString());
    }

    protected void addSiteMember(String userName, String site) throws Exception
    {
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        membership.put("person", person);

        sendRequest(new PostRequest(URL_SITES + "/" + site + URL_MEMBERSHIPS,
                membership.toString(), "application/json"), 200);
    }
}
