package org.alfresco.rest.api.impl;

import java.util.AbstractList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.api.Favourites;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.DocumentTarget;
import org.alfresco.rest.api.model.Favourite;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.FolderTarget;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteImpl;
import org.alfresco.rest.api.model.SiteTarget;
import org.alfresco.rest.api.model.Target;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper.WalkerCallbackAdapter;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Public REST API: Centralises access to favourites functionality and maps between representations repository and api representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class FavouritesImpl implements Favourites
{
    private static final Log logger = LogFactory.getLog(FavouritesImpl.class);

	private People people;
	private Sites sites;
	private Nodes nodes;
	private FavouritesService favouritesService;
	private SiteService siteService;

	public void setPeople(People people)
	{
		this.people = people;
	}

	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	public void setNodes(Nodes nodes)
	{
		this.nodes = nodes;
	}

	public void setFavouritesService(FavouritesService favouritesService)
	{
		this.favouritesService = favouritesService;
	}
	
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	private Target getTarget(PersonFavourite personFavourite)
	{
		Target target = null;
		NodeRef nodeRef = personFavourite.getNodeRef();
		Type type = personFavourite.getType();
		if(type.equals(Type.FILE))
		{
			Document document = nodes.getDocument(nodeRef);
			target = new DocumentTarget(document);
		}
		else if(type.equals(Type.FOLDER))
		{
			Folder folder = nodes.getFolder(nodeRef);
			target = new FolderTarget(folder);
		}
		else if(type.equals(Type.SITE))
		{
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			SiteRole role = sites.getSiteRole(siteInfo.getShortName());
			Site site = new SiteImpl(siteInfo, role);
			target = new SiteTarget(site);
		}
		else
		{
			throw new AlfrescoRuntimeException("Unexpected favourite target type: " + type);
		}
		
		return target;
	}
	
	private Favourite getFavourite(PersonFavourite personFavourite)
	{
		Favourite fav = new Favourite();
		fav.setTargetGuid(personFavourite.getNodeRef().getId());
		fav.setCreatedAt(personFavourite.getCreatedAt());
		Target target = getTarget(personFavourite);
		fav.setTarget(target);
		return fav;
	}

    private CollectionWithPagingInfo<Favourite> wrap(Paging paging, PagingResults<PersonFavourite> personFavourites)
    {
    	final List<PersonFavourite> page = personFavourites.getPage();
    	final List<Favourite> list = new AbstractList<Favourite>()
		{
			@Override
			public Favourite get(int index)
			{
				PersonFavourite personFavourite = page.get(index);
				Favourite fav = getFavourite(personFavourite);
				return fav;
			}

			@Override
			public int size()
			{
				return page.size();
			}
		};
		Pair<Integer, Integer> pair = personFavourites.getTotalResultCount();
		Integer total = null;
		if(pair.getFirst().equals(pair.getSecond()))
		{
			total = pair.getFirst();
		}
    	return CollectionWithPagingInfo.asPaged(paging, list, personFavourites.hasMoreItems(), total);
    }

    @Override
	public Favourite addFavourite(String personId, Favourite favourite)
    {
    	Favourite ret = null;

    	personId = people.validatePerson(personId, true);
    	Target target = favourite.getTarget();
    	if(target == null)
    	{
    		throw new InvalidArgumentException("target is missing");
    	}
    	else if(target instanceof SiteTarget)
    	{
    		SiteTarget siteTarget = (SiteTarget)target;
    		NodeRef guid = siteTarget.getSite().getGuid();
    		SiteInfo siteInfo = sites.validateSite(guid);
    		NodeRef siteNodeRef = siteInfo.getNodeRef();
    		String siteId = siteInfo.getShortName();

    		try
    		{
    			PersonFavourite personFavourite = favouritesService.addFavourite(personId, siteNodeRef);
    			ret = getFavourite(personFavourite);
    		}
    		catch(SiteDoesNotExistException e)
    		{
    			throw new RelationshipResourceNotFoundException(personId, siteId);
    		}
    	}
    	else if(target instanceof DocumentTarget)
    	{
    		DocumentTarget documentTarget = (DocumentTarget)target;
    		NodeRef nodeRef = documentTarget.getFile().getGuid();
    		if(!nodes.nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null))
    		{
    			throw new RelationshipResourceNotFoundException(personId, nodeRef.getId());
    		}
    		
    	   	PersonFavourite personFavourite = favouritesService.addFavourite(personId, nodeRef);
    	   	ret = getFavourite(personFavourite);
    	}
    	else if(target instanceof FolderTarget)
    	{
    		FolderTarget folderTarget = (FolderTarget)target;
    		NodeRef nodeRef = folderTarget.getFolder().getGuid();
    		if(!nodes.nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_FOLDER), Collections.singleton(SiteModel.TYPE_SITE)))
    		{
    			throw new RelationshipResourceNotFoundException(personId, nodeRef.getId());
    		}

    	   	PersonFavourite personFavourite = favouritesService.addFavourite(personId, nodeRef);
    	   	ret = getFavourite(personFavourite);
    	}

    	return ret;
    }

    @Override
    public void removeFavourite(String personId, String id)
    {
    	personId = people.validatePerson(personId, true);
    	NodeRef nodeRef = nodes.validateNode(id);
    	boolean exists = false;

    	Type type = favouritesService.getType(nodeRef);
    	if(type.equals(Type.SITE))
    	{
    		SiteInfo siteInfo = siteService.getSite(nodeRef);
    		if(siteInfo == null)
    		{
    			// shouldn't happen because the type implies it's a site
    			throw new AlfrescoRuntimeException("Unable to find site with nodeRef " + nodeRef);
    		}
    		exists = favouritesService.removeFavourite(personId, siteInfo.getNodeRef());
    	}
    	else if(type.equals(Type.FILE))
    	{
    		exists = favouritesService.removeFavourite(personId, nodeRef);
    	}
    	else if(type.equals(Type.FOLDER))
    	{
    		exists = favouritesService.removeFavourite(personId, nodeRef);
    	}
    	if(!exists)
    	{
    		throw new RelationshipResourceNotFoundException(personId, id);
    	}
    }
    
    public Favourite getFavourite(String personId, String favouriteId)
    {
    	NodeRef nodeRef = nodes.validateNode(favouriteId);
    	personId = people.validatePerson(personId, true);

    	PersonFavourite personFavourite = favouritesService.getFavourite(personId, nodeRef);
    	if(personFavourite != null)
    	{
	    	Favourite favourite = getFavourite(personFavourite);
	    	return favourite;
    	}
    	else
    	{
    		throw new RelationshipResourceNotFoundException(personId, favouriteId);
    	}
    }

	@Override
    public CollectionWithPagingInfo<Favourite> getFavourites(String personId, final Parameters parameters)
    {
    	personId = people.validatePerson(personId, true);

    	Paging paging = parameters.getPaging();
    	
    	final Set<Type> filteredByClientQuery = new HashSet<Type>();
    	Set<Type> filterTypes = FavouritesService.Type.ALL_FILTER_TYPES;  //Default all
    	
    	// filterType is of the form 'target.<site|file|folder>'
    	QueryHelper.walk(parameters.getQuery(), new WalkerCallbackAdapter()
        {
			@Override
			public void or() {
				//OR is supported but exists() will be called for each EXISTS so we don't
				//need to do anything here.  If we don't override it then it will be assumed
				//that OR in the grammar is not supported.
			}

			@Override
			public void exists(String filteredByClient, boolean negated) {
                if(filteredByClient != null)
                {
                    int idx = filteredByClient.lastIndexOf("/");
                    if(idx == -1 || idx == filteredByClient.length())
                    {
                        throw new InvalidArgumentException();
                    }
                    else
                    {
                    	String filtertype = filteredByClient.substring(idx + 1).toUpperCase();
                        filteredByClientQuery.add(Type.valueOf(filtertype));
                    }
                }
				
			}
        });
    	
    	if (filteredByClientQuery.size() > 0)
    	{
    	    filterTypes = filteredByClientQuery;
    	}

    	final PagingResults<PersonFavourite> favourites = favouritesService.getPagedFavourites(personId, filterTypes, FavouritesService.DEFAULT_SORT_PROPS,
    			Util.getPagingRequest(paging));
    	return wrap(paging, favourites);
    }
}
