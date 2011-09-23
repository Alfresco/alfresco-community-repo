/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
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
 * As of 4.0, the export no longer includes an AVM Dump, as
 *  the site config is now in the main site ACP.
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
        
        // Export the users who are members of the site's groups
        // Also includes the list of their site related groups
        mainZip.putNextEntry(new ZipEntry("People.acp"));
        doPeopleACPExport(site, outputForNesting);
        
        // Export the Site's groups listings
        mainZip.putNextEntry(new ZipEntry("Groups.txt"));
        doGroupExport(site, outputForNesting);
        
        // Export the User (authentication) details of those people
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
        if (authenticationDao == null)
        {
            mainZip.putNextEntry(new ZipEntry("Users_Skipped_As_Wrong_Authentication.txt"));
            String text = "Users were not exported because the Authentication\n"+
                          "Subsystem you are using is not repository based";
            outputForNesting.write(text.getBytes("ASCII"));
        }
        else
        {
            mainZip.putNextEntry(new ZipEntry("Users.acp"));
            doUserACPExport(site, outputForNesting, authenticationDao);
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
    
    protected void doPeopleACPExport(SiteInfo site, CloseIgnoringOutputStream writeTo) throws IOException
    {
        // Find the root group
        String siteGroup = buildSiteGroup(site);
        
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
    
    protected void doGroupExport(SiteInfo site, CloseIgnoringOutputStream writeTo) throws IOException
    {
        // Find the root group
        String siteGroup = buildSiteGroup(site);
        
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
    
    protected void doUserACPExport(SiteInfo site, CloseIgnoringOutputStream writeTo,
            RepositoryAuthenticationDao authenticationDao) throws IOException
    {
        List<NodeRef> exportNodes = new ArrayList<NodeRef>();
        
        // Identify all the users
        String siteGroup = buildSiteGroup(site);
        Set<String> siteUsers = authorityService.getContainedAuthorities(
                AuthorityType.USER, siteGroup, false);
        
        // Now export them, and only them
        for (String user : siteUsers)
        {
            if (USERS_NOT_TO_EXPORT.contains(user))
            {
                // Don't export these core users like admin
            }
            else 
            {
                //NodeRef personNodeRef = authorityService.getAuthorityNodeRef(user);
                NodeRef userNodeRef = authenticationDao.getUserOrNull(user);
                exportNodes.add(userNodeRef);
            }
        }

        // Build the parameters
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(new Location(exportNodes.toArray(new NodeRef[exportNodes.size()])));
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
    
    protected String buildSiteGroup(SiteInfo site)
    {
        return "GROUP_site_" + site.getShortName();
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