/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Preference Service Implementation
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceImpl implements PreferenceService
{
    /** Node service */    
    private NodeService nodeService;
    
    /** Content service */
    private ContentService contentService;
    
    /** Person service */
    private PersonService personService;
    
    /** Permission Service */
    private PermissionService permissionService;    
    
    /** Authentication Service */
    private AuthenticationContext authenticationContext;
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    the content service
     */
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
    
    /**
     * Set the permission service
     * 
     * @param permissionService     the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set the authentication component
     * 
     * @param authenticationContext   the authentication component
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
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
     */
    @SuppressWarnings("unchecked")
    public Map<String, Serializable> getPreferences(String userName, String preferenceFilter)
    {
        Map<String, Serializable> preferences = new HashMap<String, Serializable>(20);
        
        // Get the user node reference
        NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because he/she does not exist.");
        }
        
        try
        {
            // Check for preferences aspect
            if (this.nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == true)
            {
                // Get the preferences for this user
                JSONObject jsonPrefs = new JSONObject();
                ContentReader reader = this.contentService.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
                if (reader != null)
                {
                    jsonPrefs = new JSONObject(reader.getContentString());
                }
                
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

    /**
     * Matches the preference name to the partial preference name provided
     * 
     * @param name      preference name
     * @param matchTo   match to the partial preference name provided
     * @return boolean  true if matches, false otherwise
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
            index ++;
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.repo.person.PersonService#setPreferences(java.lang.String, java.util.HashMap)
     */
    public void setPreferences(final String userName, final Map<String, Serializable> preferences)
    {
        // Get the user node reference
        final NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not update preferences for " + userName + " because he/she does not exist.");
        }
        
        // Can only set preferences if the currently logged in user matches the user name being updated or
        // the user already has write permissions on the person node
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (authenticationContext.isSystemUserName(currentUserName) == true ||
            permissionService.hasPermission(personNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED || 
            userName.equals(currentUserName) == true)
        {     
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    // Apply the preferences aspect if required
                    if (PreferenceServiceImpl.this.nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == false)
                    {
                        PreferenceServiceImpl.this.nodeService.addAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES, null);
                    }
                    
                    try
                    {        
                        // Get the current preferences
                        JSONObject jsonPrefs = new JSONObject();
                        ContentReader reader = PreferenceServiceImpl.this.contentService.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
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
                        ContentWriter contentWriter = PreferenceServiceImpl.this.contentService.getWriter(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES, true);
                        contentWriter.setEncoding("UTF-8");
                        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                        contentWriter.putContent(jsonPrefs.toString());
                    }
                    catch (JSONException exception)
                    {
                        throw new AlfrescoRuntimeException("Can not update preferences for " + userName + " because there was an error pasing the JSON data.", exception);
                    }
   
                    return null;
                }
                
            }, AuthenticationUtil.SYSTEM_USER_NAME);            
        }
        else
        {
            // The current user does not have sufficient permissions to update the preferences for this user
            throw new AlfrescoRuntimeException("The current user " + currentUserName + " does not have sufficient permissions to update the preferences of the user " + userName);
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
     * @see org.alfresco.repo.person.PersonService#clearPreferences(java.lang.String, java.lang.String)
     */
    public void clearPreferences(final String userName, final String preferenceFilter)
    {
        // Get the user node reference
        final NodeRef personNodeRef = this.personService.getPerson(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not update preferences for " + userName + " because he/she does not exist.");
        }
        
        // Can only set preferences if the currently logged in user matches the user name being updated or
        // the user already has write permissions on the person node
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (authenticationContext.isSystemUserName(currentUserName) == true ||
            permissionService.hasPermission(personNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED || 
            userName.equals(currentUserName) == true)
        {     
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    if (PreferenceServiceImpl.this.nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES) == true)
                    {
                        try
                        {
                            JSONObject jsonPrefs = new JSONObject();
                            if (preferenceFilter != null && preferenceFilter.length() != 0)
                            {
                                // Get the current preferences
                                ContentReader reader = PreferenceServiceImpl.this.contentService.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
                                if (reader != null)
                                {
                                    jsonPrefs = new JSONObject(reader.getContentString());
                                }
                                
                                // Remove the prefs that match the filter
                                List<String> removeKeys = new ArrayList<String>(10);
                                Iterator<String> keys = jsonPrefs.keys();
                                while (keys.hasNext())
                                {
                                    String key = (String)keys.next();
                                    
                                    if (preferenceFilter == null ||
                                        preferenceFilter.length() == 0 ||
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
                            ContentWriter contentWriter = PreferenceServiceImpl.this.contentService.getWriter(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES, true);
                            contentWriter.setEncoding("UTF-8");
                            contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                            contentWriter.putContent(jsonPrefs.toString());                        
                        }
                        catch (JSONException exception)
                        {
                            throw new AlfrescoRuntimeException("Can not update preferences for " + userName + " because there was an error pasing the JSON data.", exception);
                        }
                    }
                    
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());               
        }
        else
        {
            // The current user does not have sufficient permissions to update the preferences for this user
            throw new AlfrescoRuntimeException("The current user " + currentUserName + " does not have sufficient permissions to update the preferences of the user " + userName);
        }
    }

}
