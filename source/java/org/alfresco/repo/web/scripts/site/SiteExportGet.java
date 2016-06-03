package org.alfresco.repo.web.scripts.site;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.RepositoryAuthenticationDao;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Exports a Site as a zip of ACPs.
 * 
 * @author Nick Burch
 * @since 3.5
 */
public class SiteExportGet extends AbstractWebScript
{
    private static final List<String> USERS_NOT_TO_EXPORT = Arrays.asList(
            new String[] { "admin", "guest" });
    
    private SiteService siteService;
    private ExporterService exporterService;
    private MimetypeService mimetypeService;
    private AuthorityService authorityService;
    private ChildApplicationContextManager authenticationContextManager;
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // Grab the site
        String siteName = 
            req.getServiceMatch().getTemplateVars().get("shortname");
        SiteInfo site = siteService.getSite(siteName);
        if (site == null)
        {
            throw new WebScriptException(
                    Status.STATUS_NOT_FOUND, 
                    "No Site found with that short name");
        }
        
        // Set things up to return them a zip file
        res.setContentType(
                MimetypeMap.MIMETYPE_ZIP);
        
        res.setHeader(
                "Content-Disposition",
                "attachment; fileName=" + siteName + "-export.zip");
        
        ZipOutputStream mainZip = new ZipOutputStream(
                res.getOutputStream());
        CloseIgnoringOutputStream outputForNesting = 
                new CloseIgnoringOutputStream(mainZip); 
        
        // Export the Site's Contents
        // This includes the site config such as dashboards
        mainZip.putNextEntry(new ZipEntry("Contents.acp"));
        doSiteACPExport(site, outputForNesting);
        
        // Export the person details for the users who are members of the site's groups
        // Also includes the list of their site related groups
        //
        // If there are no users in this site (other than the built-ins like admin, guest)
        // then include a special marker entry to that effect.
        final List<NodeRef> peopleNodes = getPersonNodesInSiteGroup(site);
        if (peopleNodes.isEmpty())
        {
            mainZip.putNextEntry(new ZipEntry("No_Persons_In_Site.txt"));
            String text = "Person nodes were not exported because the site does not contain\n"+
                          "any members other than the built-ins e.g. admin, guest.";
            outputForNesting.write(text.getBytes("ASCII"));
        }
        else
        {
            mainZip.putNextEntry(new ZipEntry("People.acp"));
            doPeopleACPExport(peopleNodes, site, outputForNesting);
        }
        
        // Export the Site's groups listings
        mainZip.putNextEntry(new ZipEntry("Groups.txt"));
        doGroupExport(site, outputForNesting);
        
        // Export the User (authentication) details for the site users that have user(authenticator) nodes associated
        // Only does this if the repository based authenticator is enabled
        RepositoryAuthenticationDao authenticationDao = null;
        for(String contextName : authenticationContextManager.getInstanceIds())
        {
            ApplicationContext ctx = authenticationContextManager.getApplicationContext(contextName);
            try 
            {
                authenticationDao = (RepositoryAuthenticationDao)
                    ctx.getBean(RepositoryAuthenticationDao.class);
            } catch(NoSuchBeanDefinitionException e) {}
        }
        List<NodeRef> userNodes = getUserNodesInSiteGroup(site, authenticationDao);
        //authenticationDao is initialized only when using a repository-based authentication subsystem
        if (authenticationDao == null)
        {
            mainZip.putNextEntry(new ZipEntry("Users_Skipped_As_Wrong_Authentication.txt"));
            String text = "Users were not exported because the Authentication\n"+
                          "Subsystem you are using is not repository based";
            outputForNesting.write(text.getBytes("ASCII"));
        }
        else if (userNodes.isEmpty())
        {
            mainZip.putNextEntry(new ZipEntry("No_Users_In_Site.txt"));
            String text = "User nodes were not exported because the site does not contain\n"+
                          "any members other than the built-ins e.g. admin, guest.";
            outputForNesting.write(text.getBytes("ASCII"));
        }
        else
        {
            mainZip.putNextEntry(new ZipEntry("Users.acp"));
            doUserACPExport(userNodes, site, outputForNesting);
        }
        
        // Finish up
        mainZip.close();
    }
    
    protected void doSiteACPExport(SiteInfo site, CloseIgnoringOutputStream writeTo) throws IOException
    {
        // Build the parameters
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(new Location(site.getNodeRef()));
        parameters.setCrawlChildNodes(true);
        parameters.setCrawlSelf(true);
        parameters.setCrawlContent(true);
        
        // And the export handler
        ACPExportPackageHandler handler = new ACPExportPackageHandler(
                writeTo,
                new File(site.getShortName() + ".xml"),
                new File(site.getShortName()),
                mimetypeService);
        
        // Do the export
        exporterService.exportView(handler, parameters, null);
    }
    
    protected void doPeopleACPExport(final List<NodeRef> peopleNodes, SiteInfo site, CloseIgnoringOutputStream writeTo) throws IOException
    {
        if (!peopleNodes.isEmpty())
        {
            // Build the parameters
            ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
            parameters.setExportFrom(new Location(peopleNodes.toArray(new NodeRef[peopleNodes.size()])));
            parameters.setCrawlChildNodes(true);
            parameters.setCrawlSelf(true);
            parameters.setCrawlContent(true);
            
            // And the export handler
            ACPExportPackageHandler handler = new ACPExportPackageHandler(
                    writeTo,
                    new File(site.getShortName() + "-people.xml"),
                    new File(site.getShortName() + "-people"),
                    mimetypeService);
            
            // Do the export
            exporterService.exportView(handler, parameters, null);
        }
    }
    
    /**
     * Gets the NodeRefs for cm:person nodes in the specified site - excluding admin, guest.
     * @since 4.1.5
     */
    private List<NodeRef> getPersonNodesInSiteGroup(SiteInfo site)
    {
        // Find the root group
        String siteGroup = AbstractSiteWebScript.buildSiteGroup(site);
        
        // Now get all people in the child groups
        Set<String> siteUsers = authorityService.getContainedAuthorities(
                AuthorityType.USER, siteGroup, false);
        
        // Turn these all into NodeRefs
        List<NodeRef> peopleNodes = new ArrayList<NodeRef>(siteUsers.size());
        for (String authority : siteUsers) 
        {
            if (!USERS_NOT_TO_EXPORT.contains(authority))
            {
                peopleNodes.add(authorityService.getAuthorityNodeRef(authority));
            }
        }
        return peopleNodes;
    }
    
    /**
     * Returns the user nodes (authentication nodes) if the authenticationDao exists
     * @param site
     * @param authenticationDao
     * @return
     */
    private List<NodeRef> getUserNodesInSiteGroup(SiteInfo site, RepositoryAuthenticationDao authenticationDao) {
          List<NodeRef> userNodes = new ArrayList<NodeRef>();
          if(authenticationDao == null) 
          {
              return userNodes;
          }
          
          // Identify all the users
          String siteGroup = AbstractSiteWebScript.buildSiteGroup(site);
          Set<String> siteUsers = authorityService.getContainedAuthorities(
                  AuthorityType.USER, siteGroup, false);
          
          for (String user : siteUsers)
          {
              if (USERS_NOT_TO_EXPORT.contains(user))
              {
                  // Don't export these core users like admin
              }
              else 
              {
                  NodeRef userNodeRef = authenticationDao.getUserOrNull(user);
                  if(userNodeRef != null)
                  {
                      userNodes.add(userNodeRef);
                  }
              }
          }
          return userNodes;
    }
    
    protected void doGroupExport(SiteInfo site, CloseIgnoringOutputStream writeTo) throws IOException
    {
        // Find the root group
        String siteGroup = AbstractSiteWebScript.buildSiteGroup(site);
        
        // Get all the child groups of the site (but not children of them)
        Set<String> siteGroups = authorityService.getContainedAuthorities(
              AuthorityType.GROUP, siteGroup, true);
        
        // For each group, get all the people
        // (Flattens any intermediate groups)
        // Then, invert it to get the groups per person
        Map<String,List<String>> memberships = new HashMap<String, List<String>>();
        for(String group : siteGroups)
        {
            Set<String> groupUsers = authorityService.getContainedAuthorities(
                  AuthorityType.USER, group, false);
            
            for (String user : groupUsers)
            {
                if (!USERS_NOT_TO_EXPORT.contains(user))
                {
                    if (!memberships.containsKey(user))
                    {
                        memberships.put(user, new ArrayList<String>());
                    }
                    memberships.get(user).add(group);
                }
            }
        }
        
        // Do a simple text based export
        //   user=group1,group2
        PrintWriter out = new PrintWriter(new OutputStreamWriter(writeTo, "UTF-8"));
        for (String user : memberships.keySet())
        {
            out.print(user);
            out.print('=');
            
            boolean first = true;
            for (String group : memberships.get(user))
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    out.print(',');
                }
                out.print(group);
            }
            out.println();
        }
        out.close();
    }
    
    protected void doUserACPExport(List<NodeRef> userNodes, SiteInfo site,
            CloseIgnoringOutputStream writeTo) throws IOException
    {
        // Build the parameters
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(new Location(userNodes.toArray(new NodeRef[userNodes.size()])));
        parameters.setCrawlChildNodes(true);
        parameters.setCrawlSelf(true);
        parameters.setCrawlContent(true);
        
        // And the export handler
        ACPExportPackageHandler handler = new ACPExportPackageHandler(
                writeTo,
                new File(site.getShortName() + "-users.xml"),
                new File(site.getShortName() + "-users"),
                mimetypeService);
        
        // Do the export
        exporterService.exportView(handler, parameters, null);
    }
    
    protected static class CloseIgnoringOutputStream extends FilterOutputStream
    {
        public CloseIgnoringOutputStream(OutputStream out)
        {
            super(out);
        }

        @Override
        public void close() throws IOException
        {
            // Flushes, but doesn't close
            flush();
        }
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setExporterService(ExporterService exporterService)
    {
        this.exporterService = exporterService;
    }
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setAuthenticationContextManager(ChildApplicationContextManager authenticationContextManager)
    {
        this.authenticationContextManager = authenticationContextManager;
    }
}