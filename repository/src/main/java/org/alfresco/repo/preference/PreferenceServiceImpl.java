/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.preference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.preference.traitextender.PreferenceServiceExtension;
import org.alfresco.repo.preference.traitextender.PreferenceServiceTrait;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.traitextender.AJExtender;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Preference Service Implementation
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceImpl implements PreferenceService, Extensible
{
    private static final Log log = LogFactory.getLog(PreferenceServiceImpl.class);
    
    private static final String SHARE_SITES_PREFERENCE_KEY = "org.alfresco.share.sites.favourites.";
    private static final int SHARE_SITES_PREFERENCE_KEY_LEN = SHARE_SITES_PREFERENCE_KEY.length();
    private static final String EXT_SITES_PREFERENCE_KEY = "org.alfresco.ext.sites.favourites.";
    
    /** Node service */    
    private NodeService nodeService;
    private ContentService contentService;
    private PersonService personService;
    private PermissionService permissionService;
    
    /** Authentication Service */
    private AuthenticationContext authenticationContext;
    private AuthorityService authorityService;

    private final ExtendedTrait<PreferenceServiceTrait> preferenceServiceTrait;
    
    public PreferenceServiceImpl()
    {
        preferenceServiceTrait=new ExtendedTrait<PreferenceServiceTrait>(createPreferenceServiceTrait());
    }
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the person service
     * 
     * @param personService     the person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    
    /**
     * @see org.alfresco.service.cmr.preference.PreferenceService#getPreferences(java.lang.String)
     */
    public Map<String, Serializable> getPreferences(String userName)
    {
        return getPreferences(userName, null);
    }
    
    private JSONObject getPreferencesObject(String userName) throws JSONException
    {
        JSONObject jsonPrefs = null;

        // Get the user node reference
        NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Cannot get preferences for " + userName
                + " because he/she does not exist."); 
        }

        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        boolean isSystem = AuthenticationUtil.isRunAsUserTheSystemUser() || authenticationContext.isSystemUserName(currentUserName);
        if (isSystem || userName.equals(currentUserName)
                    || personService.getUserIdentifier(userName).equals(personService.getUserIdentifier(currentUserName))
                    || authorityService.isAdminAuthority(currentUserName))
        {
            // Check for preferences aspect
            if (this.nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == true)
            {
                // Get the preferences for this user
                ContentReader reader = this.contentService.getReader(personNodeRef,
                        ContentModel.PROP_PREFERENCE_VALUES);
                if (reader != null)
                {
                    jsonPrefs = new JSONObject(reader.getContentString());
                }
            }
        }
        else
        {
            // The current user does not have sufficient permissions to get
            // the preferences for this user
            throw new AccessDeniedException("The current user " + currentUserName
                    + " does not have sufficient permissions to get the preferences of the user " + userName);
        }
        
        return jsonPrefs;
    }

    public Serializable getPreference(String userName, String preferenceName)
    {
        String preferenceValue = null;
        try
        {
            JSONObject jsonPrefs = getPreferencesObject(userName);
            if(jsonPrefs != null)
            {
                if(jsonPrefs.has(preferenceName))
                {
                    preferenceValue = jsonPrefs.getString(preferenceName);
                }
            }
        }
        catch (JSONException exception)
        {
            throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because there was an error pasing the JSON data.", exception);
        }

        return preferenceValue;
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, Serializable> getPreferences(String userName, String preferenceFilter)
    {
        if (log.isTraceEnabled()) 
        { 
            log.trace("getPreferences(" + userName + ", " + preferenceFilter + ")"); 
        }
        
        Map<String, Serializable> preferences = new TreeMap<String, Serializable>();
        
        try
        {
            JSONObject jsonPrefs = getPreferencesObject(userName);
            if(jsonPrefs != null)
            {
                // Build hash from preferences stored in the repository
                Iterator<String> keys = jsonPrefs.keys();
                while (keys.hasNext())
                {
                    String key = (String)keys.next();
                    Serializable value = (Serializable)jsonPrefs.get(key);

                    if(key.startsWith(SHARE_SITES_PREFERENCE_KEY))
                    {
                        // CLOUD-1518: convert site preferences on the fly
                        // convert keys as follows:
                        //   <SHARE_SITES_PREFERENCE_KEY>.<siteId>.favourited -> <SHARE_SITES_PREFERENCE_KEY>.<siteId>
                        //   <SHARE_SITES_PREFERENCE_KEY>.<siteId>.createdAt -> <EXT_SITES_PREFERENCE_KEY>.<siteId>.createdAt
                        if(key.endsWith(".favourited"))
                        {
                            int idx = key.indexOf(".favourited");
                            String siteId = key.substring(SHARE_SITES_PREFERENCE_KEY_LEN, idx);
                            StringBuilder sb = new StringBuilder(SHARE_SITES_PREFERENCE_KEY);
                            sb.append(siteId);
                            key = sb.toString();
                        }
    
                        else if(key.endsWith(".createdAt"))
                        {
                            int idx = key.indexOf(".createdAt");
                            String siteId = key.substring(SHARE_SITES_PREFERENCE_KEY_LEN, idx);
                            StringBuilder sb = new StringBuilder(EXT_SITES_PREFERENCE_KEY);
                            sb.append(siteId);
                            sb.append(".createdAt");
                            key = sb.toString();
                        }
                        else if(preferences.containsKey(key))
                        {
                            // Ensure that the values of the following form (the only other important form in this case) does not
                            // override those on the lhs from above:
                            //   <SHARE_SITES_PREFERENCE_KEY>.<siteId>
                            continue;
                        }
                    }

                    if (preferenceFilter == null ||
                        preferenceFilter.length() == 0 ||
                        matchPreferenceNames(key, preferenceFilter))
                    {
                        preferences.put(key, value);
                    }
                }
            }
        }
        catch (JSONException exception)
        {
            throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because there was an error parsing the JSON data.", exception);
        }

        if (log.isTraceEnabled()) 
        { 
            log.trace("result = " + preferences); 
        }
        
        return preferences;
    }
    
    public PagingResults<Pair<String, Serializable>> getPagedPreferences(String userName, String preferenceFilter, PagingRequest pagingRequest)
    {
        final Map<String, Serializable> prefs = getPreferences(userName, preferenceFilter);

        int totalSize = prefs.size();
        int skipCount = pagingRequest.getSkipCount();
        int maxItems = pagingRequest.getMaxItems();
        int end = maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? totalSize : skipCount + maxItems;
        int pageSize = (maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? totalSize : Math.max(maxItems, totalSize - skipCount));
        final boolean hasMoreItems = end < totalSize;

        final List<Pair<String, Serializable>> page = new ArrayList<Pair<String, Serializable>>(pageSize);
        Iterator<Map.Entry<String, Serializable>> it = prefs.entrySet().iterator();
        for(int counter = 0; counter < end && it.hasNext(); counter++)
        {
            Map.Entry<String, Serializable> pref = it.next();

            if(counter < skipCount)
            {
                continue;
            }
            
            if(counter > end - 1)
            {
                break;
            }

            page.add(new Pair<String, Serializable>(pref.getKey(), pref.getValue()));
        }

        return new PagingResults<Pair<String, Serializable>>()
        {
            @Override
            public List<Pair<String, Serializable>> getPage()
            {
                return page;
            }

            @Override
            public boolean hasMoreItems()
            {
                return hasMoreItems;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                Integer total = Integer.valueOf(prefs.size());
                return new Pair<Integer, Integer>(total, total);
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
        };
    }

    /**
     * Matches the preference name to the partial preference name provided
     * 
     * @param name preference name
     * @param matchTo match to the partial preference name provided
     * @return boolean true if matches, false otherwise
     */
    private boolean matchPreferenceNames(String name, String matchTo)
    {
        boolean result = true;

        // Split strings
        name = name.replace(".", "+");
        String[] nameArr = name.split("\\+");
        matchTo = matchTo.replace(".", "+");
        String[] matchToArr = matchTo.split("\\+");

        if(matchToArr.length > nameArr.length)
        {
            return false;
        }

        int index = 0;
        for (String matchToElement : matchToArr)
        {
            if (matchToElement.equals(nameArr[index]) == false)
            {
                result = false;
                break;
            }
            index++;
        }

        return result;
    }

    @Extend(traitAPI=PreferenceServiceTrait.class,extensionAPI=PreferenceServiceExtension.class)
    public void setPreferences(final String userName, final Map<String, Serializable> preferences)
    {
        // Get the user node reference
        final NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Cannot update preferences for " + userName
                + " because he/she does not exist.");
        }
        
        if (userCanWritePreferences(userName, personNodeRef))
        {
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    // Apply the preferences aspect if required
                    if (PreferenceServiceImpl.this.nodeService
                            .hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == false)
                    {
                        PreferenceServiceImpl.this.nodeService.addAspect(personNodeRef,
                                ContentModel.ASPECT_PREFERENCES, null);
                    }

                    try
                    {
                        // Get the current preferences
                        JSONObject jsonPrefs = new JSONObject();
                        ContentReader reader = PreferenceServiceImpl.this.contentService.getReader(personNodeRef,
                                ContentModel.PROP_PREFERENCE_VALUES);
                        if (reader != null)
                        {
                            jsonPrefs = new JSONObject(reader.getContentString());
                        }

                        // Update with the new preference values
                        for (Map.Entry<String, Serializable> entry : preferences.entrySet())
                        {
                            String key = entry.getKey();

                            // CLOUD-1518: remove extraneous site preferences, if present
                            if(key.startsWith(SHARE_SITES_PREFERENCE_KEY))
                            {
                                // remove any extraneous keys, if present
                                String siteId = key.substring(SHARE_SITES_PREFERENCE_KEY_LEN);

                                StringBuilder sb = new StringBuilder(SHARE_SITES_PREFERENCE_KEY);
                                sb.append(siteId);
                                sb.append(".favourited");
                                String testKey = sb.toString();
                                if(jsonPrefs.has(testKey))
                                {
                                    jsonPrefs.remove(testKey);
                                }
                                
                                sb = new StringBuilder(SHARE_SITES_PREFERENCE_KEY);
                                sb.append(siteId);
                                sb.append(".createdAt");
                                testKey = sb.toString();
                                if(jsonPrefs.has(testKey))
                                {
                                    jsonPrefs.remove(testKey);
                                }
                            }
                            
                            Serializable value = entry.getValue();
                            if(value != null && value.equals("CURRENT_DATE"))
                            {
                                Date date = new Date();
                                value = ISO8601DateFormat.format(date);
                            }
                            jsonPrefs.put(key, value);
                        }

                        // Save the updated preferences
                        ContentWriter contentWriter = PreferenceServiceImpl.this.contentService.getWriter(
                                personNodeRef, ContentModel.PROP_PREFERENCE_VALUES, true);
                        contentWriter.setEncoding("UTF-8");
                        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                        contentWriter.putContent(jsonPrefs.toString());
                    }
                    catch (JSONException exception)
                    {
                        throw new AlfrescoRuntimeException("Can not update preferences for " + userName
                                + " because there was an error pasing the JSON data.", exception);
                    }

                    return null;
                }
            }, AuthenticationUtil.SYSTEM_USER_NAME);
        }
        else
        {
            // The current user does not have sufficient permissions to update
            // the preferences for this user
            throw new AccessDeniedException("The current user " + AuthenticationUtil.getFullyAuthenticatedUser()
                    + " does not have sufficient permissions to update the preferences of the user " + userName);
        }
    }

    /**
     * @see org.alfresco.service.cmr.preference.PreferenceService#clearPreferences(java.lang.String)
     */
    public void clearPreferences(String userName)
    {
        clearPreferences(userName, null);
    }
    
    public void clearPreferences(final String userName, final String preferenceFilter)
    {
        // Get the user node reference
        final NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Cannot update preferences for " + userName
                + " because he/she does not exist.");
        }
        
        if (userCanWritePreferences(userName, personNodeRef))
        {
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    if (PreferenceServiceImpl.this.nodeService
                            .hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == true)
                    {
                        try
                        {
                            JSONObject jsonPrefs = new JSONObject();
                            if (preferenceFilter != null && preferenceFilter.length() != 0)
                            {
                                // Get the current preferences
                                ContentReader reader = PreferenceServiceImpl.this.contentService.getReader(
                                        personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
                                if (reader != null)
                                {
                                    jsonPrefs = new JSONObject(reader.getContentString());
                                }

                                // Remove the prefs that match the filter
                                List<String> removeKeys = new ArrayList<String>(10);
                                @SuppressWarnings("unchecked")
                                Iterator<String> keys = jsonPrefs.keys();
                                while (keys.hasNext())
                                {
                                    final String key = (String) keys.next();

                                    if (preferenceFilter == null || preferenceFilter.length() == 0 ||
                                        matchPreferenceNames(key, preferenceFilter) == true)
                                    {
                                        removeKeys.add(key);
                                    }
                                }
                                for (String removeKey : removeKeys)
                                {
                                    jsonPrefs.remove(removeKey);
                                }
                            }

                            // Put the updated JSON back into the repo
                            ContentWriter contentWriter = PreferenceServiceImpl.this.contentService.getWriter(
                                    personNodeRef, ContentModel.PROP_PREFERENCE_VALUES, true);
                            contentWriter.setEncoding("UTF-8");
                            contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                            contentWriter.putContent(jsonPrefs.toString());
                        }
                        catch (JSONException exception)
                        {
                            throw new AlfrescoRuntimeException("Can not update preferences for " + userName
                                    + " because there was an error pasing the JSON data.", exception);
                        }
                    }

                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
        else
        {
            // The current user does not have sufficient permissions to update
            // the preferences for this user
            throw new AccessDeniedException("The current user " + AuthenticationUtil.getFullyAuthenticatedUser()
                    + " does not have sufficient permissions to update the preferences of the user " + userName);
        }
    }

    /**
     * Helper to encapsulate the test for whether the currently authenticated user can write to the
     * preferences objects for the given username and person node reference.
     * 
     * @param userName          Username owner of the preferences object for modification test 
     * @param personNodeRef     Non-null person representing the given username
     * 
     * @return true if they are allowed to write to the user preferences, false otherwise
     */
    private boolean userCanWritePreferences(final String userName, final NodeRef personNodeRef)
    {
        final String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        return (userName.equals(currentUserName) ||
                personService.getUserIdentifier(userName).equals(personService.getUserIdentifier(currentUserName)) ||
                authenticationContext.isSystemUserName(currentUserName) ||
                permissionService.hasPermission(personNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED);
    }
    
    public static class PageDetails
    {
        private boolean hasMoreItems = false;
        private int pageSize;
        private int skipCount;
        private int maxItems;
        private int end;

        public PageDetails(int pageSize, boolean hasMoreItems, int skipCount, int maxItems, int end)
        {
            super();
            this.hasMoreItems = hasMoreItems;
            this.pageSize = pageSize;
            this.skipCount = skipCount;
            this.maxItems = maxItems;
            this.end = end;
        }

        public int getSkipCount()
        {
            return skipCount;
        }

        public int getMaxItems()
        {
            return maxItems;
        }

        public int getEnd()
        {
            return end;
        }

        public boolean hasMoreItems()
        {
            return hasMoreItems;
        }

        public int getPageSize()
        {
            return pageSize;
        }
    }
    
    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) preferenceServiceTrait;
    }
    
    public PreferenceServiceTrait createPreferenceServiceTrait()
    {
        return new PreferenceServiceTrait()
        {

            @Override
            public void setPreferences(final String userName, final Map<String, Serializable> preferences)
                        throws Throwable
            {

                AJExtender.run(new AJExtender.ExtensionBypass<Void>()
                {
                    @Override
                    public Void run()
                    {
                        PreferenceServiceImpl.this.setPreferences(userName, preferences);
                        return null;
                    };
                });

            }

        };
    }

}
