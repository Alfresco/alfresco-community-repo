/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteTarget;
import org.alfresco.rest.api.model.Target;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper.WalkerCallbackAdapter;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.SortFields;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
    private NamespaceService namespaceService;

    // additional exclude properties for favourites as these can be already top-level properties
    private static final List<QName> EXCLUDED_PROPS = Arrays.asList(
            ContentModel.PROP_TITLE,
            ContentModel.PROP_DESCRIPTION,
            SiteModel.PROP_SITE_VISIBILITY,
            SiteModel.PROP_SITE_PRESET
            );

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

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

	private Target getTarget(PersonFavourite personFavourite, Parameters parameters)
	{
		Target target = null;
		NodeRef nodeRef = personFavourite.getNodeRef();
		Type type = personFavourite.getType();
		if(type.equals(Type.FILE))
		{
			Document document = nodes.getDocument(nodeRef);
            setPathInfo(document, parameters.getInclude());
            target = new DocumentTarget(document);
		}
		else if(type.equals(Type.FOLDER))
		{
			Folder folder = nodes.getFolder(nodeRef);
            setPathInfo(folder, parameters.getInclude());
			target = new FolderTarget(folder);
		}
		else if(type.equals(Type.SITE))
		{
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			String role = sites.getSiteRole(siteInfo.getShortName());
			Site site = new Site(siteInfo, role);
			target = new SiteTarget(site);
		}
		else
		{
			throw new AlfrescoRuntimeException("Unexpected favourite target type: " + type);
		}
		
		return target;
	}

	private Favourite getFavourite(PersonFavourite personFavourite, Parameters parameters)
	{
		Favourite fav = new Favourite();
		fav.setTargetGuid(personFavourite.getNodeRef().getId());
		fav.setCreatedAt(personFavourite.getCreatedAt());
		Target target = getTarget(personFavourite, parameters);
		fav.setTarget(target);

		// REPO-1147 allow retrieving additional properties
        if (parameters.getInclude().contains(PARAM_INCLUDE_PROPERTIES))
        {
            List<String> includeProperties = new LinkedList<>();
            includeProperties.add(PARAM_INCLUDE_PROPERTIES);
            // get node representation with only properties included
            Node node = nodes.getFolderOrDocument(personFavourite.getNodeRef(), null, null, includeProperties, null);
            // Create a map from node properties excluding properties already in this Favorite
            Map<String, Object> filteredNodeProperties = filterProps(node.getProperties(), EXCLUDED_PROPS);
            if(filteredNodeProperties.size() > 0)
            {
                fav.setProperties(filteredNodeProperties);
            }
        }

		return fav;
	}

    private Map<String, Object> filterProps(Map<String, Object> properties, List<QName> toRemove)
    {
        Map<String, Object> filteredProps = new HashMap<>(properties);
        List<String> propsToRemove = toRemove.stream().map(e -> e.toPrefixString(namespaceService)).collect(Collectors.toList());
        filteredProps.keySet().removeAll(propsToRemove);
        return filteredProps;
    }

    private CollectionWithPagingInfo<Favourite> wrap(Paging paging, PagingResults<PersonFavourite> personFavourites, Parameters parameters)
    {
    	final List<PersonFavourite> page = personFavourites.getPage();
    	final List<Favourite> list = new AbstractList<Favourite>()
		{
			@Override
			public Favourite get(int index)
			{
				PersonFavourite personFavourite = page.get(index);
				Favourite fav = getFavourite(personFavourite, parameters);
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
        Parameters parameters = getDefaultParameters(personId, null);
        return addFavourite(personId, favourite, parameters);
    }

    @Override
    public Favourite addFavourite(String personId, Favourite favourite, Parameters parameters)
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
            String guid = siteTarget.getSite().getGuid();
            SiteInfo siteInfo = sites.validateSite(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid));
            NodeRef siteNodeRef = siteInfo.getNodeRef();
            String siteId = siteInfo.getShortName();

            try
            {
                PersonFavourite personFavourite = favouritesService.addFavourite(personId, siteNodeRef);
                ret = getFavourite(personFavourite, parameters);
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
            ret = getFavourite(personFavourite, parameters);
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
            ret = getFavourite(personFavourite, parameters);
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

    @Override
    public Favourite getFavourite(String personId, String favouriteId)
    {
        Parameters parameters = getDefaultParameters(personId, favouriteId);
        return getFavourite(personId, favouriteId, parameters);
    }

    @Override
    public Favourite getFavourite(String personId, String favouriteId, Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateNode(favouriteId);
        personId = people.validatePerson(personId, true);

        PersonFavourite personFavourite = favouritesService.getFavourite(personId, nodeRef);
        if(personFavourite != null)
        {
            Favourite favourite = getFavourite(personFavourite, parameters);
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

    	List<Pair<FavouritesService.SortFields, Boolean>> sortProps = getSortProps(parameters);

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

        final PagingResults<PersonFavourite> favourites = favouritesService.getPagedFavourites(personId, filterTypes, sortProps, Util.getPagingRequest(paging));

    	return wrap(paging, favourites, parameters);
    }

    private void setPathInfo(Node node, List<String> includeParam)
    {
        if (includeParam.contains(PARAM_INCLUDE_PATH))
        {
            PathInfo pathInfo = nodes.lookupPathInfo(node.getNodeRef(), null);
            node.setPath(pathInfo);
        }
    }

    /**
     * Returns a {@code {@link Parameters} object where almost all of its values are null.
     * the non-null value is the {@literal include} and whatever value is passed for {@code personId} and {@code favouriteId}
     */
    private Parameters getDefaultParameters(String personId, String favouriteId)
    {
        Params.RecognizedParams recognizedParams = new Params.RecognizedParams(null, null, null, null, Collections.emptyList(), null, null, null,
                    false);
        Parameters parameters = Params.valueOf(recognizedParams, personId, favouriteId, null);
        return parameters;
    }

    private List<Pair<FavouritesService.SortFields, Boolean>> getSortProps(Parameters parameters)
    {
        List<Pair<FavouritesService.SortFields, Boolean>> sortProps = new ArrayList<>();
        List<SortColumn> sortCols = parameters.getSorting();
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            for (SortColumn sortCol : sortCols)
            {
                SortFields sortField;
                try
                {
                    sortField = SortFields.valueOf(sortCol.column);
                }
                catch (Exception e)
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
                }
                sortProps.add(new Pair<>(sortField, (sortCol.asc ? Boolean.TRUE : Boolean.FALSE)));
            }
        }
        else
        {
            // default sort order
            sortProps = FavouritesService.DEFAULT_SORT_PROPS;
        }
        return sortProps;
    }
}
