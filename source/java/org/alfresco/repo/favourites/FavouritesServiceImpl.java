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
package org.alfresco.repo.favourites;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.events.types.ActivityEvent;
import org.alfresco.events.types.Event;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;
import org.alfresco.repo.events.EventPreparator;
import org.alfresco.repo.events.EventPublisher;
import org.alfresco.repo.favourites.PersonFavourite.PersonFavouriteKey;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Favourites service implementation that uses the PreferencesService for persisting favourites.
 * 
 * Unfortunately, we are tied to the PreferencesService and to the preference names and data structure because Share uses the PreferenceService directly.
 * 
 * @author steveglover
 */
public class FavouritesServiceImpl implements FavouritesService, InitializingBean
{
    private static final Log logger = LogFactory.getLog(FavouritesServiceImpl.class);

	private Map<Type, PrefKeys> prefKeys;

    private PreferenceService preferenceService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private SiteService siteService;
    private PolicyComponent policyComponent;
    private PermissionService permissionService;
    private PersonService personService;
    private EventPublisher eventPublisher;

    /** Authentication Service */
    private AuthenticationContext authenticationContext;
    
    private ClassPolicyDelegate<OnAddFavouritePolicy> onAddFavouriteDelegate;
    private ClassPolicyDelegate<OnRemoveFavouritePolicy> onRemoveFavouriteDelegate;
    
    private Collator collator = Collator.getInstance();
    
    public interface OnAddFavouritePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAddfavourite");

        /**
         * Called after a node has been <b>favourited</b>
         * 
         * @param userName the username of the person who favourited the node
         * @param nodeRef the node which was favourited
         */
        public void onAddFavourite(String userName, NodeRef nodeRef);
    }
    
    public interface OnRemoveFavouritePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRemovefavourite");

        /**
         * Called after a <b>favourite</b> has been removed
         * 
         * @param userName the username of the person who favourited the node
         * @param nodeRef the node that was un-favourited
         */
        public void onRemoveFavourite(String userName, NodeRef nodeRef);
    }

    public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}

	public void setPersonService(PersonService personService)
	{
		this.personService = personService;
	}

	public void setAuthenticationContext(AuthenticationContext authenticationContext)
	{
		this.authenticationContext = authenticationContext;
	}

	public void setPolicyComponent(PolicyComponent policyComponent)
    {
		this.policyComponent = policyComponent;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setPreferenceService(PreferenceService preferenceService)
	{
		this.preferenceService = preferenceService;
	}

	private static class PrefKeys
	{
		private String sharePrefKey;
		private String alfrescoPrefKey;
		
		public PrefKeys(String sharePrefKey, String alfrescoPrefKey)
		{
			super();
			this.sharePrefKey = sharePrefKey;
			this.alfrescoPrefKey = alfrescoPrefKey;
		}
		
		public String getSharePrefKey()
		{
			return sharePrefKey;
		}
		
		public String getAlfrescoPrefKey()
		{
			return alfrescoPrefKey;
		}
	}

    @Override
    public void afterPropertiesSet() throws Exception
	{
		this.prefKeys = new HashMap<Type, PrefKeys>();
    	this.prefKeys.put(Type.SITE, new PrefKeys("org.alfresco.share.sites.favourites.", "org.alfresco.ext.sites.favourites."));
    	this.prefKeys.put(Type.FILE, new PrefKeys("org.alfresco.share.documents.favourites", "org.alfresco.ext.documents.favourites."));
    	this.prefKeys.put(Type.FOLDER, new PrefKeys("org.alfresco.share.folders.favourites", "org.alfresco.ext.folders.favourites."));
	}
    
    public void init()
    {
        this.onAddFavouriteDelegate = policyComponent.registerClassPolicy(OnAddFavouritePolicy.class);
        this.onRemoveFavouriteDelegate = policyComponent.registerClassPolicy(OnRemoveFavouritePolicy.class);
    }

    private PrefKeys getPrefKeys(Type type)
    {
    	PrefKeys prefKey = prefKeys.get(type);
    	return prefKey;
    }

    private boolean removeFavouriteSite(String userName, NodeRef nodeRef)
    {
    	PrefKeys prefKeys = getPrefKeys(Type.SITE);
		boolean exists = false;

		SiteInfo siteInfo = siteService.getSite(nodeRef);
		if(siteInfo != null)
		{
			StringBuilder sitePrefKeyBuilder = new StringBuilder(prefKeys.getSharePrefKey());
			sitePrefKeyBuilder.append(siteInfo.getShortName());
			String sitePrefKey = sitePrefKeyBuilder.toString();

			String siteFavouritedKey = siteFavouritedKey(siteInfo);
	
			exists = preferenceService.getPreference(userName, siteFavouritedKey) != null;
			preferenceService.clearPreferences(userName, sitePrefKey);
		}
		else
		{
			throw new IllegalArgumentException("NodeRef " + nodeRef + " is not a site");
		}

    	return exists;
    }

    private String siteFavouritedKey(SiteInfo siteInfo)
    {
    	PrefKeys prefKeys = getPrefKeys(Type.SITE);

		StringBuilder sitePrefKeyBuilder = new StringBuilder(prefKeys.getSharePrefKey());
		sitePrefKeyBuilder.append(siteInfo.getShortName());
		String sitePrefKey = sitePrefKeyBuilder.toString();

		String favouritedKey = sitePrefKey;
		return favouritedKey;
    }
    
    private String siteCreatedAtKey(SiteInfo siteInfo)
    {
    	PrefKeys prefKeys = getPrefKeys(Type.SITE);

		StringBuilder sitePrefKeyBuilder = new StringBuilder(prefKeys.getAlfrescoPrefKey());
		sitePrefKeyBuilder.append(siteInfo.getShortName());
		String sitePrefKey = sitePrefKeyBuilder.toString();

		StringBuilder createdAtKeyBuilder = new StringBuilder(sitePrefKey);
		createdAtKeyBuilder.append(".createdAt");
		String createdAtKey = createdAtKeyBuilder.toString();
		return createdAtKey;
    }

    private Comparator<PersonFavouriteKey> getComparator(final List<Pair<FavouritesService.SortFields, Boolean>> sortProps)
    {
    	Comparator<PersonFavouriteKey> comparator = new Comparator<PersonFavouriteKey>()
    	{
			@Override
			public int compare(PersonFavouriteKey o1, PersonFavouriteKey o2)
			{
				int ret = 0;
				for(Pair<FavouritesService.SortFields, Boolean> sort : sortProps)
				{
					FavouritesService.SortFields field = sort.getFirst();
					Boolean ascending = sort.getSecond();
					if(field.equals(FavouritesService.SortFields.username))
					{
						if(ascending)
						{
							ret = collator.compare(o1.getUserName(), o2.getUserName());							
						}
						else
						{
							ret = o2.getUserName().compareTo(o1.getUserName());
						}

						if(ret != 0)
						{
							break;
						}
					}
					else if(field.equals(FavouritesService.SortFields.type))
					{
						if(ascending)
						{
							ret = o1.getType().compareTo(o2.getType());							
						}
						else
						{
							ret = o2.getType().compareTo(o1.getType());
						}

						if(ret != 0)
						{
							break;
						}
					}
					else if(field.equals(FavouritesService.SortFields.createdAt))
					{
						if(ascending)
						{
							if(o1.getCreatedAt() != null && o2.getCreatedAt() != null)
							{
								ret = o1.getCreatedAt().compareTo(o2.getCreatedAt());
							}
						}
						else
						{
							if(o1.getCreatedAt() != null && o2.getCreatedAt() != null)
							{
								ret = o2.getCreatedAt().compareTo(o1.getCreatedAt());
							}
						}

						if(ret != 0)
						{
							break;
						}
					}
					else if(field.equals(FavouritesService.SortFields.title))
					{
						if(ascending)
						{
							if(o1.getTitle() != null && o2.getTitle() != null)
							{
								ret = collator.compare(o1.getTitle(), o2.getTitle());
							}
						}
						else
						{
							if(o1.getTitle() != null && o2.getTitle() != null)
							{
								ret = collator.compare(o2.getTitle(), o1.getTitle());
							}
						}

						if(ret != 0)
						{
							break;
						}
					}
				}
				
				if(ret == 0)
				{
					// some favourites may not have a createdAt value, rendering this comparator less selective.
					// If the favourites are still regarded as the same, differentiate on nodeRef.
					ret = o1.getNodeRef().toString().compareTo(o2.getNodeRef().toString());
				}

				return ret;
			}
    	};
    	return comparator;
    }

    private PersonFavourite addFavouriteSite(String userName, NodeRef nodeRef)
    {
    	PersonFavourite favourite = null;

    	SiteInfo siteInfo = siteService.getSite(nodeRef);
    	if(siteInfo != null)
    	{
	    	favourite = getFavouriteSite(userName, siteInfo);
	    	if(favourite == null)
	    	{
	    		Map<String, Serializable> preferences = new HashMap<String, Serializable>(1);
	
	    		String siteFavouritedKey = siteFavouritedKey(siteInfo);
	    		preferences.put(siteFavouritedKey, Boolean.TRUE);

	    		// ISO8601 string format: PreferenceService works with strings only for dates it seems
	    		String siteCreatedAtKey = siteCreatedAtKey(siteInfo);
	    		Date createdAt = new Date();
	    		String createdAtStr = ISO8601DateFormat.format(createdAt);
	    		preferences.put(siteCreatedAtKey, createdAtStr);
	
	    		preferenceService.setPreferences(userName, preferences);
	
	    		favourite = new PersonFavourite(userName, siteInfo.getNodeRef(), Type.SITE, siteInfo.getTitle(), createdAt);
	
	    		QName nodeClass = nodeService.getType(nodeRef);
	            OnAddFavouritePolicy policy = onAddFavouriteDelegate.get(nodeRef, nodeClass);
	            policy.onAddFavourite(userName, nodeRef);
	    	}
    	}
    	else
    	{
    		// shouldn't happen, getType recognizes it as a site or subtype
    		logger.warn("Unable to get site for " + nodeRef);
    	}

    	return favourite;
    }

    private PersonFavourite getFavouriteSite(String userName, SiteInfo siteInfo)
    {
    	PersonFavourite favourite = null;

		String siteFavouritedKey = siteFavouritedKey(siteInfo);
		String siteCreatedAtKey = siteCreatedAtKey(siteInfo);

		Boolean isFavourited = false;
		Serializable s = preferenceService.getPreference(userName, siteFavouritedKey);
		if(s != null)
		{
			if(s instanceof String)
			{
				isFavourited = Boolean.valueOf((String)s);
			}
			else if(s instanceof Boolean)
			{
				isFavourited = (Boolean)s;
			}
			else
			{
				throw new AlfrescoRuntimeException("Unexpected favourites preference value");
			}
		}

		if(isFavourited)
		{
			String createdAtStr = (String)preferenceService.getPreference(userName, siteCreatedAtKey);
			Date createdAt = (createdAtStr == null ? null : ISO8601DateFormat.parse(createdAtStr));

			favourite = new PersonFavourite(userName, siteInfo.getNodeRef(), Type.SITE, siteInfo.getTitle(), createdAt);
		}
		
		return favourite;
    }
    
    private boolean isFavouriteSite(String userName, NodeRef nodeRef)
    {
		Boolean isFavourited = Boolean.FALSE;
    	SiteInfo siteInfo = siteService.getSite(nodeRef);
    	if(siteInfo != null)
    	{
    		String favouritedPrefKey = siteFavouritedKey(siteInfo);
			Serializable value = preferenceService.getPreference(userName, favouritedPrefKey);
	
			if(value != null)
			{
				if(value instanceof String)
				{
					isFavourited = Boolean.valueOf((String)value);
				}
				else if(value instanceof Boolean)
				{
					isFavourited = (Boolean)value;
				}
				else
				{
					throw new AlfrescoRuntimeException("Unexpected favourites preference value");
				}
			}
    	}
    	else
    	{
    		throw new IllegalArgumentException("NodeRef " + nodeRef + " is not a site");
    	}

		return isFavourited.booleanValue();
    }

    private void updateFavouriteNodes(String userName, Type type, Map<PersonFavouriteKey, PersonFavourite> favouriteNodes)
    {
    	PrefKeys prefKeys = getPrefKeys(type);

		Map<String, Serializable> preferences = new HashMap<String, Serializable>(1);

		StringBuilder values = new StringBuilder();
		for(PersonFavourite node : favouriteNodes.values())
		{
			NodeRef nodeRef = node.getNodeRef();

			values.append(nodeRef.toString());
			values.append(",");

    		// ISO8601 string format: PreferenceService works with strings only for dates it seems
	    	StringBuilder sb = new StringBuilder(prefKeys.getAlfrescoPrefKey());
	    	sb.append(nodeRef.toString());
	    	sb.append(".createdAt");
	    	String createdAtKey = sb.toString();
    		Date createdAt = node.getCreatedAt();
    		if(createdAt != null)
    		{
	    		String createdAtStr = ISO8601DateFormat.format(createdAt);
	    		preferences.put(createdAtKey, createdAtStr);
    		}
		}

		if(values.length() > 1)
		{
			values.delete(values.length() - 1, values.length());
		}

		preferences.put(prefKeys.getSharePrefKey(), values.toString());
		preferenceService.setPreferences(userName, preferences);
    }

    private Map<PersonFavouriteKey, PersonFavourite> getFavouriteNodes(String userName, Type type)
    {
    	PrefKeys prefKeys = getPrefKeys(type);
        Map<PersonFavouriteKey, PersonFavourite> favouriteNodes = Collections.emptyMap();
        Map<String, Serializable> prefs = preferenceService.getPreferences(userName, prefKeys.getSharePrefKey());
        String nodes = (String)prefs.get(prefKeys.getSharePrefKey());
        if(nodes != null)
        {
        	favouriteNodes = extractFavouriteNodes(userName, type, nodes);
        }
        else
        {
        	favouriteNodes = new HashMap<PersonFavouriteKey, PersonFavourite>();
        }
        
        return favouriteNodes;
    }
    
    /*
     * Extract favourite nodes of the given type from the comma-separated list in "nodes".
     */
    private Map<PersonFavouriteKey, PersonFavourite> extractFavouriteNodes(String userName, Type type, String nodes)
    {
    	PrefKeys prefKeys = getPrefKeys(type);
    	Map<PersonFavouriteKey, PersonFavourite> favouriteNodes = new HashMap<PersonFavouriteKey, PersonFavourite>();

        StringTokenizer st = new StringTokenizer(nodes, ",");
        while(st.hasMoreTokens())
        {
        	String nodeRefStr = st.nextToken();
        	nodeRefStr = nodeRefStr.trim();
        	if(!NodeRef.isNodeRef((String)nodeRefStr))
        	{
        		continue;
        	}

        	NodeRef nodeRef = new NodeRef((String)nodeRefStr);

        	if(!nodeService.exists(nodeRef))
        	{
        		continue;
        	}

			if(permissionService.hasPermission(nodeRef, PermissionService.READ_PROPERTIES) == AccessStatus.DENIED)
			{
				continue;
			}

        	// get createdAt for this favourited node
        	// use ISO8601
			StringBuilder builder = new StringBuilder(prefKeys.getAlfrescoPrefKey());
			builder.append(nodeRef.toString());
			builder.append(".createdAt");
			String prefKey = builder.toString();
			String createdAtStr = (String)preferenceService.getPreference(userName, prefKey);
			Date createdAt = (createdAtStr != null ? ISO8601DateFormat.parse(createdAtStr): null);

        	String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

        	PersonFavourite personFavourite = new PersonFavourite(userName, nodeRef, type, name, createdAt);
        	PersonFavouriteKey key = personFavourite.getKey();
        	favouriteNodes.put(key, personFavourite);
        }

        return favouriteNodes;
    }
    
    private void extractFavouriteSite(String userName, Type type, Map<PersonFavouriteKey, PersonFavourite> sortedFavouriteNodes, Map<String, Serializable> preferences, String key)
    {
    	// preference value indicates whether the site has been favourited   	
    	Serializable pref = preferences.get(key);
    	Boolean isFavourite = (Boolean)pref;
		if(isFavourite)
		{
	    	PrefKeys sitePrefKeys = getPrefKeys(Type.SITE);
			int length = sitePrefKeys.getSharePrefKey().length();
			String siteId = key.substring(length);

    		try
    		{
	        	SiteInfo siteInfo = siteService.getSite(siteId);
	        	if(siteInfo != null)
	        	{
	    			StringBuilder builder = new StringBuilder(sitePrefKeys.getAlfrescoPrefKey());
	    			builder.append(siteId);
	    			builder.append(".createdAt");
	    			String createdAtPrefKey = builder.toString();
	    			String createdAtStr = (String)preferences.get(createdAtPrefKey);
	    			Date createdAt = null;
	    			if(createdAtStr != null)
	    			{
						createdAt = (createdAtStr != null ? ISO8601DateFormat.parse(createdAtStr): null);
	    			}
	        		PersonFavourite personFavourite = new PersonFavourite(userName, siteInfo.getNodeRef(), Type.SITE, siteId, createdAt);
	        		sortedFavouriteNodes.put(personFavourite.getKey(), personFavourite);
	        	}
    		}
    		catch(AccessDeniedException ex)
    		{
    			// the user no longer has access to this site, skip over the favourite
    			// TODO remove the favourite preference 
    			return;
    		}
    	}
    }

    private PersonFavourite getFavouriteDocumentOrFolder(String userName, Type type, NodeRef nodeRef)
    {
    	PersonFavourite favourite = null;

    	PrefKeys prefKeys = getPrefKeys(type);
		Map<String, Serializable> preferences = preferenceService.getPreferences(userName, prefKeys.getSharePrefKey());
		String nodes = (String)preferences.get(prefKeys.getSharePrefKey());
		if(nodes != null)
		{
			Map<PersonFavouriteKey, PersonFavourite> favouriteNodes = extractFavouriteNodes(userName, type, nodes);
			String title = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
			PersonFavouriteKey key = new PersonFavouriteKey(userName, title, type, nodeRef);
			favourite = favouriteNodes.get(key);
		}
		return favourite;
    }

    private PersonFavourite getPersonFavourite(String userName, Type type, NodeRef nodeRef)
    {
    	PersonFavourite ret = null;
    	if(type.equals(Type.SITE))
    	{
        	SiteInfo siteInfo = siteService.getSite(nodeRef);
        	if(siteInfo != null)
        	{
        		ret = getFavouriteSite(userName, siteInfo);
        	}
        	else
        	{
        		// shouldn't happen, getType recognizes it as a site or subtype
        		logger.warn("Unable to get site for " + nodeRef);
        	}
    	}
    	else if(type.equals(Type.FILE))
    	{
    		ret = getFavouriteDocumentOrFolder(userName, type, nodeRef);
    	}
    	else if(type.equals(Type.FOLDER))
    	{
    		ret = getFavouriteDocumentOrFolder(userName, type, nodeRef);
    	}
    	else
    	{
    		// shouldn't happen
    		throw new AlfrescoRuntimeException("Unexpected favourite type");
    	}

    	return ret;
    }

    private PersonFavourite addFavouriteDocumentOrFolder(String userName, Type type, NodeRef nodeRef)
    {
		Map<PersonFavouriteKey, PersonFavourite> personFavourites = getFavouriteNodes(userName, type);
		PersonFavourite personFavourite = getPersonFavourite(userName, type, nodeRef);
		if(personFavourite == null)
		{
			Date createdAt = new Date();
			final String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			personFavourite = new PersonFavourite(userName, nodeRef, type, name, createdAt);
			personFavourites.put(personFavourite.getKey(), personFavourite);
			updateFavouriteNodes(userName, type, personFavourites);
			
			QName nodeClass = nodeService.getType(nodeRef);
			final String finalRef = nodeRef.getId();
			final QName nodeType = nodeClass;
			
            eventPublisher.publishEvent(new EventPreparator(){
                @Override
                public Event prepareEvent(String user, String networkId, String transactionId)
                {            
                    return new ActivityEvent("favorite.added", transactionId, networkId, user, finalRef,
                                null, nodeType==null?null:nodeType.toString(),  Client.asType(ClientType.script), null,
                                name, null, 0l, null);
                }
            });
            
	        OnAddFavouritePolicy policy = onAddFavouriteDelegate.get(nodeRef, nodeClass);
	        policy.onAddFavourite(userName, nodeRef);
		}

		return personFavourite;
    }
    
    private boolean isFavouriteNode(String userName, Type type, NodeRef nodeRef)
    {
		Map<PersonFavouriteKey, PersonFavourite> personFavourites = getFavouriteNodes(userName, type);
		PersonFavouriteKey personFavouriteKey = new PersonFavouriteKey(userName, null, type, nodeRef);
		boolean isFavourite = personFavourites.containsKey(personFavouriteKey);
		return isFavourite;
    }

    public Type getType(NodeRef nodeRef)
    {
    	Type favouriteType = null;

        QName type = nodeService.getType(nodeRef);
        boolean isSite = dictionaryService.isSubClass(type, SiteModel.TYPE_SITE);
        if(isSite)
        {
        	favouriteType = Type.SITE;
        }
        else
        {
        	boolean isContainer = (dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) &&
        			!dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER));
        	if(isContainer)
        	{
        		favouriteType = Type.FOLDER;
        	}
        	else
        	{
            	boolean isFile = dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
            	if(isFile)
            	{
            		favouriteType = Type.FILE;
            	}
        	}
        }

        return favouriteType;
    }

    private boolean removeFavouriteNode(String userName, Type type, NodeRef nodeRef)
    {
		boolean exists = false;

		Map<PersonFavouriteKey, PersonFavourite> personFavourites = getFavouriteNodes(userName, type);

		PersonFavouriteKey personFavouriteKey = new PersonFavouriteKey(userName, null, type, nodeRef);
		PersonFavourite personFavourite = personFavourites.remove(personFavouriteKey);
		exists = personFavourite != null;
		updateFavouriteNodes(userName, type, personFavourites);

		QName nodeClass = nodeService.getType(nodeRef);
        final String finalRef = nodeRef.getId();
        final QName nodeType = nodeClass;
        
        eventPublisher.publishEvent(new EventPreparator(){
            @Override
            public Event prepareEvent(String user, String networkId, String transactionId)
            {            
                return new ActivityEvent("favorite.removed", transactionId, networkId, user, finalRef,
                            null, nodeType==null?null:nodeType.toString(),  Client.asType(ClientType.script), null,
                            null, null, 0l, null);
            }
        });
        
        OnRemoveFavouritePolicy policy = onRemoveFavouriteDelegate.get(nodeRef, nodeClass);
        policy.onRemoveFavourite(userName, nodeRef);

    	return exists;
    }
    
    @Override
    public PersonFavourite addFavourite(String userName, NodeRef nodeRef)
    {
    	PersonFavourite personFavourite = null;

    	Type type = getType(nodeRef);
    	if(type == null)
    	{
    		throw new IllegalArgumentException("Cannot favourite this node");
    	}
    	else if(type.equals(Type.FILE))
    	{
    		personFavourite = addFavouriteDocumentOrFolder(userName, Type.FILE, nodeRef);
    	}
    	else if(type.equals(Type.FOLDER))
    	{
    		personFavourite = addFavouriteDocumentOrFolder(userName, Type.FOLDER, nodeRef);
    	}
    	else if(type.equals(Type.SITE))
    	{
    		personFavourite = addFavouriteSite(userName, nodeRef);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Cannot favourite this node");
    	}

		return personFavourite;
    }

    @Override
    public boolean removeFavourite(String userName, NodeRef nodeRef)
    {
    	boolean exists = false;

    	Type type = getType(nodeRef);
    	if(type == null)
    	{
    		throw new IllegalArgumentException("Cannot un-favourite this node");
    	}
    	else if(type.equals(Type.FILE))
    	{
    		exists = removeFavouriteNode(userName, type, nodeRef);
    	}
    	else if(type.equals(Type.FOLDER))
    	{
    		exists = removeFavouriteNode(userName, type, nodeRef);
    	}
    	else if(type.equals(Type.SITE))
    	{
    		exists = removeFavouriteSite(userName, nodeRef);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Cannot un-favourite this node");
    	}

    	return exists;
    }
    
    @Override
    public boolean isFavourite(String userName, NodeRef nodeRef)
    {
    	boolean isFavourite = false;

    	Type type = getType(nodeRef);
    	if(type == null)
    	{
    		throw new IllegalArgumentException("Unsupported node type");
    	}
    	else if(type.equals(Type.FILE))
    	{
    		isFavourite = isFavouriteNode(userName, type, nodeRef);
    	}
    	else if(type.equals(Type.FOLDER))
    	{
    		isFavourite = isFavouriteNode(userName, type, nodeRef);
    	}
    	else if(type.equals(Type.SITE))
    	{
    		isFavourite = isFavouriteSite(userName, nodeRef);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Unsupported node type");
    	}

    	return isFavourite;
    }

    @Override
    public PagingResults<PersonFavourite> getPagedFavourites(String userName, Set<Type> types,
    		List<Pair<FavouritesService.SortFields, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        // Get the user node reference
        final NodeRef personNodeRef = personService.getPerson(userName);
        if(personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because he/she does not exist.");
        }

        boolean includeFiles = types.contains(Type.FILE);
        boolean includeFolders = types.contains(Type.FOLDER);
        boolean includeSites = types.contains(Type.SITE);

        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (authenticationContext.isSystemUserName(currentUserName) == true ||
            permissionService.hasPermission(personNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED || 
            userName.equals(currentUserName) == true)
        {
			// we may have more than one favourite that is considered the same w.r.t. the PersonFavourite comparator
	    	final Map<PersonFavouriteKey, PersonFavourite> sortedFavouriteNodes = new TreeMap<PersonFavouriteKey, PersonFavourite>(getComparator(sortProps));
	
	    	PrefKeys sitePrefKeys = getPrefKeys(Type.SITE);
	    	PrefKeys documentsPrefKeys = getPrefKeys(Type.FILE);
	    	PrefKeys foldersPrefKeys = getPrefKeys(Type.FOLDER);
	
			Map<String, Serializable> preferences = preferenceService.getPreferences(userName);
			for(String key : preferences.keySet())
			{
				if(includeFiles && key.startsWith(documentsPrefKeys.sharePrefKey))
				{
			        String nodes = (String)preferences.get(key);
			        if(nodes != null)
			        {
			        	sortedFavouriteNodes.putAll(extractFavouriteNodes(userName, Type.FILE, nodes));
			        }
				}
				else if(includeFolders && key.startsWith(foldersPrefKeys.sharePrefKey))
				{
			        String nodes = (String)preferences.get(key);
			        if(nodes != null)
			        {
			        	sortedFavouriteNodes.putAll(extractFavouriteNodes(userName, Type.FOLDER, nodes));
			        }
				}
	        	else if(includeSites && key.startsWith(sitePrefKeys.getSharePrefKey()) && !key.endsWith(".createdAt"))
	        	{
	        		// key is either of the form org.alfresco.share.sites.favourites.<siteId>.favourited or
	        		// org.alfresco.share.sites.favourites.<siteId>
	        		extractFavouriteSite(userName, Type.SITE, sortedFavouriteNodes, preferences, key);
	        	}
			}
	
	        int totalSize = sortedFavouriteNodes.size();
	        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);
	
			final List<PersonFavourite> page = new ArrayList<PersonFavourite>(pageDetails.getPageSize());
			Iterator<PersonFavourite> it = sortedFavouriteNodes.values().iterator();
	        for(int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
	        {
	        	PersonFavourite favouriteNode = it.next();
	
				if(counter < pageDetails.getSkipCount())
				{
					continue;
				}
				
				if(counter > pageDetails.getEnd() - 1)
				{
					break;
				}
	
				page.add(favouriteNode);
	        }
	
	        return new PagingResults<PersonFavourite>()
	        {
				@Override
				public List<PersonFavourite> getPage()
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
					Integer total = Integer.valueOf(sortedFavouriteNodes.size());
					return new Pair<Integer, Integer>(total, total);
				}
	
				@Override
				public String getQueryExecutionId()
				{
					return null;
				}
	        };
        }
	    else
	    {
	        // The current user does not have sufficient permissions to update the preferences for this user
	        throw new AccessDeniedException("The current user " + currentUserName + " does not have sufficient permissions to get the favourites of the user " + userName);
	    }
    }
    
    public PersonFavourite getFavourite(String userName, NodeRef nodeRef)
    {
    	Type type = getType(nodeRef);
    	return getPersonFavourite(userName, type, nodeRef);
    }

    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
}
