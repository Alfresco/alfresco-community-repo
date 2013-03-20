/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.preference;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
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
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Preference Service Implementation
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceImpl implements PreferenceService
{
    private static final String FAVOURITE_SITES_PREFIX = "org.alfresco.share.sites.favourites.";
    private static final int FAVOURITE_SITES_PREFIX_LENGTH = FAVOURITE_SITES_PREFIX.length();
    
    private NodeService nodeService;
    private ContentService contentService;
    private PersonService personService;
    private SiteService siteService;
    private PermissionService permissionService;
    private AuthenticationContext authenticationContext;
    
    private AuthorityService authorityService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
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

    /**
     * @see org.alfresco.repo.person.PersonService#getPreferences(java.lang.String, java.lang.String)
     *      java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Serializable> getPreferences(String userName, String preferenceFilter)
    {
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
                    
                    if (preferenceFilter == null ||
                        preferenceFilter.length() == 0 ||
                        matchPreferenceNames(key, preferenceFilter) == true)
                    {
                        preferences.put(key, (Serializable)jsonPrefs.get(key));
                    }
                }
            }
        }
        catch (JSONException exception)
        {
            throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because there was an error pasing the JSON data.", exception);
        }
        
        return preferences;
    }
    
    private PageDetails getPageDetails(PagingRequest pagingRequest, int totalSize)
    {
        int skipCount = pagingRequest.getSkipCount();
        int maxItems = pagingRequest.getMaxItems();
        int end = maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? totalSize : skipCount + maxItems;
    	int pageSize = (maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? totalSize : maxItems);
    	if(pageSize > totalSize - skipCount)
    	{
    		pageSize = totalSize - skipCount;
    	}

        boolean hasMoreItems = end < totalSize;
        
    	return new PageDetails(pageSize, hasMoreItems, skipCount, maxItems, end);
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
        if (userName.equals(currentUserName) ||
            personService.getUserIdentifier(userName).equals(personService.getUserIdentifier(currentUserName)) ||
            authenticationContext.isSystemUserName(currentUserName) ||
            authorityService.isAdminAuthority(currentUserName))
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
            throw new UnauthorizedAccessException("The current user " + currentUserName
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

    /**
     * @see org.alfresco.repo.person.PersonService#setPreferences(java.lang.String,
     *      java.util.HashMap)
     */
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
                            jsonPrefs.put(entry.getKey(), entry.getValue());
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
            throw new UnauthorizedAccessException("The current user " + AuthenticationUtil.getFullyAuthenticatedUser()
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
    
    /**
     * @see org.alfresco.repo.person.PersonService#clearPreferences(java.lang.String,
     *      java.lang.String)
     */
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
            throw new UnauthorizedAccessException("The current user " + AuthenticationUtil.getFullyAuthenticatedUser()
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
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#isFavouriteSite(java.lang.String, java.lang.String)
     */
    public boolean isFavouriteSite(String userName, String siteShortName)
    {
		StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
		prefKey.append(siteShortName);

    	String value = (String)getPreference(userName, prefKey.toString());
    	return (value == null ? false : value.equalsIgnoreCase("true"));
    }

    /**
     * @see org.alfresco.service.cmr.preference.PreferenceService#addFavouriteSite(java.lang.String, java.lang.String)
     */
    public void addFavouriteSite(String userName, String siteShortName)
    {
		StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
		prefKey.append(siteShortName);

		Map<String, Serializable> preferences = new HashMap<String, Serializable>(1);
		preferences.put(prefKey.toString(), Boolean.TRUE);
		setPreferences(userName, preferences);
    }

    /**
     * @see org.alfresco.service.cmr.preference.PreferenceService#removeFavouriteSite(java.lang.String, java.lang.String)
     */
    public void removeFavouriteSite(String userName, String siteShortName)
    {
		StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
		prefKey.append(siteShortName);

		clearPreferences(userName, prefKey.toString());
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#getFavouriteSites(java.lang.String, org.alfresco.query.PagingRequest)
     */
    public PagingResults<SiteInfo> getFavouriteSites(String userName, PagingRequest pagingRequest)
    {
    	final Collator collator = Collator.getInstance();

        final Set<SiteInfo> sortedFavouriteSites = new TreeSet<SiteInfo>(new Comparator<SiteInfo>()
        {
			@Override
			public int compare(SiteInfo o1, SiteInfo o2)
			{
				return collator.compare(o1.getTitle(), o2.getTitle());
			}
		});

        Map<String, Serializable> prefs = getPreferences(userName, FAVOURITE_SITES_PREFIX);
        for(String key : prefs.keySet())
        {
        	boolean isFavourite = false;
        	Serializable s = prefs.get(key);
        	if(s instanceof Boolean)
        	{
        		isFavourite = (Boolean)s;
        	}
        	if(isFavourite)
        	{
	        	String siteShortName = key.substring(FAVOURITE_SITES_PREFIX_LENGTH);
	        	SiteInfo siteInfo = siteService.getSite(siteShortName);
	        	if(siteInfo != null)
	        	{
	        		sortedFavouriteSites.add(siteInfo);
	        	}
        	}
        }

        int totalSize = sortedFavouriteSites.size();
        final PageDetails pageDetails = getPageDetails(pagingRequest, totalSize);

		final List<SiteInfo> page = new ArrayList<SiteInfo>(pageDetails.getPageSize());
		Iterator<SiteInfo> it = sortedFavouriteSites.iterator();
        for(int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
        {
        	SiteInfo favouriteSite = it.next();

			if(counter < pageDetails.getSkipCount())
			{
				continue;
			}
			
			if(counter > pageDetails.getEnd() - 1)
			{
				break;
			}

			page.add(favouriteSite);
        }

        return new PagingResults<SiteInfo>()
        {
			@Override
			public List<SiteInfo> getPage()
			{
				return page;
			}

			@Override
			public boolean hasMoreItems()
			{
				return pageDetails.hasMoreItems();
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount()
			{
				Integer total = Integer.valueOf(sortedFavouriteSites.size());
				return new Pair<Integer, Integer>(total, total);
			}

			@Override
			public String getQueryExecutionId()
			{
				return null;
			}
        };
    }
}
