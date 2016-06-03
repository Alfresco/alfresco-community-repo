package org.alfresco.repo.web.scripts.site;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.junit.Ignore;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import com.google.common.collect.Lists;

/**
 * Unit test for the Export Web Script API of the Site Object.
 */
public class SiteExportServiceTest extends AbstractSiteServiceTest
{
    private static final String USER_FROM_LDAP = "SiteUserLdap";
    private static final String USER_ONE = "SiteUser";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Create users
        createUser(USER_ONE);
        createPerson(USER_FROM_LDAP);

        // Do tests as admin
        this.authenticationComponent.setCurrentUser("admin");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Clear the users
        deleteUser(USER_ONE);
        deleteUser(USER_FROM_LDAP);
                
        //Delete the sites
        deleteSites();
    }
    
    public void testExportSiteWithMutipleUsers() throws Exception
    {
        // Create a site
        String shortName = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        // add a user and a person as members
        addSiteMember(USER_FROM_LDAP, shortName);
        addSiteMember(USER_ONE, shortName);

        // Export site
        Response response = sendRequest(new GetRequest(getExportUrl(shortName)), 200);

        // check exported files
        List<String> entries = getEntries(new ZipInputStream(new ByteArrayInputStream(
                response.getContentAsByteArray())));
        assertFalse(entries.contains("No_Users_In_Site.txt"));
        assertFalse(entries.contains("No_Persons_In_Site.txt"));
        assertTrue(entries.contains("People.acp"));
        assertTrue(entries.contains(shortName + "-people.xml"));
        assertTrue(entries.contains("Users.acp"));
        assertTrue(entries.contains(shortName + "-users.xml"));
    }

    public void testExportSiteWithOneLDAPUser() throws Exception
    {
        // Create a site
        String shortName = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        // add a user synced from LDAP(authenticator node not present)
        addSiteMember(USER_FROM_LDAP, shortName);

        // Export site
        Response response = sendRequest(new GetRequest(getExportUrl(shortName)), 200);

        // check No_Users_In_Site.txt present
        // because there is no user associated with the single member of the
        // site
        List<String> entries = getEntries(new ZipInputStream(new ByteArrayInputStream(
                response.getContentAsByteArray())));
        assertFalse(entries.contains("Users.acp"));
        assertTrue(entries.contains("No_Users_In_Site.txt"));
        assertTrue(entries.contains("People.acp"));
        assertTrue(entries.contains(shortName + "-people.xml"));
    }

    public void testExportSiteWithNoUsers() throws Exception
    {
        // Create a site
        String shortName = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        // Export site
        Response response = sendRequest(new GetRequest(getExportUrl(shortName)), 200);

        // check No_Users_In_Site.txt and No_Persons_In_Site.txt present
        List<String> entries = getEntries(new ZipInputStream(new ByteArrayInputStream(
                response.getContentAsByteArray())));
        assertTrue(entries.contains("No_Users_In_Site.txt"));
        assertTrue(entries.contains("No_Persons_In_Site.txt"));
        assertFalse(entries.contains("Users.acp"));
        assertFalse(entries.contains("People.acp"));
    }

    private List<String> getEntries(ZipInputStream zipStream) throws Exception
    {
        ZipEntry entry = null;
        List<String> entries = Lists.newArrayList();
        while ((entry = zipStream.getNextEntry()) != null)
        {
            if (entry.getName().endsWith("acp"))
            {
                entries.addAll(getAcpEntries(zipStream));
            }
            entries.add(entry.getName());
            zipStream.closeEntry();
        }
        zipStream.close();
        return entries;
    }

    private List<String> getAcpEntries(InputStream inputStream) throws Exception
    {
        List<String> entries = Lists.newArrayList();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry = null;
        try
        {
            while ((entry = zipInputStream.getNextEntry()) != null)
            {
                entries.add(entry.getName());
            }
        }
        catch (ZipException e)
        {
            // ignore
        }
        return entries;
    }

    private String getExportUrl(String shortName)
    {
        return "/api/sites/" + shortName + "/export";
    }

}
