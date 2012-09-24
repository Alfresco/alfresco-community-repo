/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authority.script.ScriptAuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Lists the members of a site, optionally filtering by name, role
 *  or authority type.
 * 
 * Based on the old memberships.get.js webscript controller
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class SiteMembershipsGet extends AbstractSiteWebScript
{
    private PersonService personService;
    private ScriptAuthorityService scriptAuthorityService;
   
    public void setPersonService(PersonService personService) 
    {
      this.personService = personService;
    }

    public void setScriptAuthorityService(ScriptAuthorityService scriptAuthorityService) 
    {
       this.scriptAuthorityService = scriptAuthorityService;
    }

    @Override
    protected Map<String, Object> executeImpl(SiteInfo site,
          WebScriptRequest req, Status status, Cache cache) 
    {
       // Grab out filters
       String nameFilter = req.getParameter("nf");
       if (nameFilter!=null)
       {
           nameFilter = nameFilter.endsWith("*") ? nameFilter : nameFilter+"*";
       }
       String roleFilter = req.getParameter("rf");
       String authorityType = req.getParameter("authorityType");
       String sizeS = req.getParameter("size");
       boolean collapseGroups = false;
       
       // Sanity check the types
       if(authorityType != null)
       {
          if("USER".equals(authorityType))
          {
             collapseGroups = true;
          }
          else if("GROUP".equals(authorityType))
          {
             // No extra settings needed
          }
          else
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                   "The Authority must be one of USER or GROUP");
          }
       }

       // Figure out what to limit to, if anythign
       int limit = 0;
       if(sizeS != null)
       {
          try
          {
             limit = Integer.parseInt(sizeS);
          }
          catch(NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                   "Invalid size specified");
          }
       }

        // Fetch the membership details of the site
        List<SiteMemberInfo> members = this.siteService.listMembersInfo(site.getShortName(),
                    nameFilter, roleFilter, limit, collapseGroups);

        // Process it ready for FreeMarker
        // Note that as usernames may be all numbers, we need to
        // prefix them with an underscore otherwise FTL can get confused!
        Map<String, Object> authorities = new HashMap<String, Object>(members.size());      
        Map<String, Object> memberInfo = new LinkedHashMap<String, Object>(members.size());
        for (SiteMemberInfo authorityObj : members)
        {           
            String ftlSafeName = "_" + authorityObj.getMemberName();

            if (authorityObj.getMemberName().startsWith("GROUP_"))
            {
                if (authorityType == null || authorityType.equals("GROUP"))
                {
                    // Record the details
                    authorities.put(ftlSafeName, scriptAuthorityService
                                .getGroupForFullAuthorityName(authorityObj.getMemberName()));                    
                    memberInfo.put(ftlSafeName, authorityObj);
                }
            }
            else
            {
                if (authorityType == null || authorityType.equals("USER"))
                {
                    // Record the details
                    authorities.put(ftlSafeName,
                                personService.getPerson(authorityObj.getMemberName()));                  
                    memberInfo.put(ftlSafeName, authorityObj);                   
                }
            }
        }

        // Pass the details to freemarker
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("site", site);
        model.put("authorities", authorities);
        model.put("memberInfo", memberInfo);
        return model;
    }
}