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

package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.filefolder.traitextender.FileFolderServiceExtension;
import org.alfresco.repo.model.filefolder.traitextender.FileFolderServiceTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.page.PageCollationException;
import org.alfresco.repo.virtual.page.PageCollator;
import org.alfresco.repo.virtual.page.PageCollator.PagingResultsSource;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.ReferenceEncodingException;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class VirtualFileFolderServiceExtension
            extends VirtualSpringBeanExtension<FileFolderServiceExtension, FileFolderServiceTrait>
            implements FileFolderServiceExtension
{
    private VirtualStore smartStore;

    private ActualEnvironment environment;

    public VirtualFileFolderServiceExtension()
    {
        super(FileFolderServiceTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    public List<FileInfo> asFileInfos(List<Reference> references, VirtualStore smartStore,
                ActualEnvironment environment) throws VirtualizationException
    {
        List<FileInfo> fileInfos = new LinkedList<>();
        for (Reference reference : references)
        {

            FileInfo fileInfo = asFileInfo(smartStore,
                                           environment,
                                           reference);

            fileInfos.add(fileInfo);
        }

        return fileInfos;
    }

    public FileInfo asFileInfo(VirtualStore smartStore, ActualEnvironment environment, Reference reference)
                throws VirtualizationException
    {
        Map<QName, Serializable> properties = smartStore.getProperties(reference);
        QName qNameType = smartStore.getType(reference);
        FileFolderServiceType type = getTrait().getType(qNameType);

        boolean isFolder = type.equals(FileFolderServiceType.FOLDER);

        NodeRef nodeRef = reference.toNodeRef();

        return getTrait().createFileInfo(nodeRef,
                                         qNameType,
                                         isFolder,
                                         false,
                                         properties);
    }

    @Override
    public List<FileInfo> list(NodeRef contextNodeRef)
    {
        if (canVirtualize(contextNodeRef))
        {
            Reference reference = smartStore.virtualize(contextNodeRef);

            List<Reference> virtualNodes = smartStore.list(reference);
            List<FileInfo> searchResult = asFileInfos(virtualNodes,
                                                      smartStore,
                                                      environment);

            if (mergeActualNode(reference))
            {
                List<FileInfo> actualSearch = getTrait().list(actualNodeFrom(reference));
                searchResult.addAll(actualSearch);
            }

            return searchResult;
        }
        else
        {
            return getTrait().list(contextNodeRef);
        }
    }

    protected boolean mergeActualNode(Reference reference) throws VirtualizationException
    {
        return smartStore.canMaterialize(reference);
    }

    protected NodeRef actualNodeFrom(Reference reference) throws VirtualizationException
    {
        return smartStore.materialize(reference);
    }

    protected boolean canVirtualize(NodeRef nodeRef) throws VirtualizationException
    {
        return smartStore.canVirtualize(nodeRef);
    }

    private Set<QName>[] buildSearchAndIgnore(final boolean files, final boolean folders, Set<QName> ignoreQNames)
    {
        Set<QName>[] searchAndIgnore = (Set<QName>[]) Array.newInstance(Set.class,
                                                                        3);

        Pair<Set<QName>, Set<QName>> searchTypesAndIgnoreAspects = getTrait().buildSearchTypesAndIgnoreAspects(files,
                                                                                                               folders,
                                                                                                               ignoreQNames);
        if (searchTypesAndIgnoreAspects != null)
        {
            Set<QName> searchTypesQNames = searchTypesAndIgnoreAspects.getFirst();
            Set<QName> ignoreAspectsQNames = searchTypesAndIgnoreAspects.getSecond();

            Set<QName> ignoreTypesQNames = null;
            if ((searchTypesQNames != null || ignoreAspectsQNames != null) && ignoreQNames != null)
            {
                ignoreTypesQNames = new HashSet<>(ignoreQNames);
                if (searchTypesQNames != null)
                {
                    ignoreTypesQNames.removeAll(searchTypesQNames);
                }
                if (ignoreAspectsQNames != null)
                {
                    ignoreTypesQNames.removeAll(ignoreAspectsQNames);
                }
            }
            searchAndIgnore[0] = searchTypesQNames;
            searchAndIgnore[1] = ignoreTypesQNames;
            searchAndIgnore[2] = ignoreAspectsQNames;
        }

        return searchAndIgnore;
    }

    @Override
    public PagingResults<FileInfo> list(final NodeRef contextNodeRef, final boolean files, final boolean folders,
                final String pattern, final Set<QName> ignoreQNames, final List<Pair<QName, Boolean>> sortProps,
                final PagingRequest pagingRequest)
    {
        final FileFolderServiceTrait theTrait = getTrait();
        if (canVirtualize(contextNodeRef))
        {

            final Reference reference = smartStore.virtualize(contextNodeRef);

            Set<QName>[] searchAndIgnore = buildSearchAndIgnore(files,
                                                                folders,
                                                                ignoreQNames);
            if (mergeActualNode(reference))
            {
                PagingResults<Reference> virtualChildren = smartStore.list(reference,
                                                                             false,
                                                                             true,
                                                                             files,
                                                                             folders,
                                                                             pattern,
                                                                             searchAndIgnore[1],
                                                                             searchAndIgnore[2],
                                                                             sortProps,
                                                                             new PagingRequest(0));

                PagingResultsSource<FileInfo> superSource = new PagingResultsSource<FileInfo>()
                {
                    @Override
                    public PagingResults<FileInfo> retrieve(PagingRequest pr) throws PageCollationException
                    {
                        try
                        {
                            PagingResults<FileInfo> result = theTrait.list(actualNodeFrom(reference),
                                                                           files,
                                                                           folders,
                                                                           pattern,
                                                                           ignoreQNames,
                                                                           sortProps,
                                                                           pr);
                            return result;
                        }
                        catch (VirtualizationException e)
                        {
                            throw new PageCollationException(e);
                        }

                    }
                };

                FileInfoPropsComparator comparator = (sortProps != null && !sortProps.isEmpty())
                            ? new FileInfoPropsComparator(sortProps) : null;

                try
                {
                    return new PageCollator<FileInfo>().collate(asFileInfoResults(environment,
                                                                                  virtualChildren,
                                                                                  smartStore).getPage(),
                                                                superSource,
                                                                pagingRequest,
                                                                comparator);
                }
                catch (PageCollationException error)
                {
                    throw new VirtualizationException(error);
                }

            }
            else
            {

                PagingResults<Reference> children = smartStore.list(reference,
                                                                      true,
                                                                      true,
                                                                      files,
                                                                      folders,
                                                                      pattern,
                                                                      searchAndIgnore[1],
                                                                      searchAndIgnore[2],
                                                                      sortProps,
                                                                      pagingRequest);

                return asFileInfoResults(environment,
                                         children,
                                         smartStore);

            }

        }
        else
        {
            return theTrait.list(contextNodeRef,
                                 files,
                                 folders,
                                 pattern,
                                 ignoreQNames,
                                 sortProps,
                                 pagingRequest);
        }
    }

    public PagingResults<FileInfo> asFileInfoResults(ActualEnvironment environment,
                final PagingResults<Reference> results, VirtualStore store)
                            throws ReferenceEncodingException, VirtualizationException
    {

        List<Reference> virtualPage = results.getPage();

        final LinkedList<FileInfo> page = new LinkedList<FileInfo>();

        for (Reference ref : virtualPage)
        {

            FileInfo fileInfo = asFileInfo(store,
                                           environment,
                                           ref);
            page.add(fileInfo);
        }

        final boolean hasMoreItems = results.hasMoreItems();
        final Pair<Integer, Integer> totalResultCount = results.getTotalResultCount();
        final String queryExecutionId = results.getQueryExecutionId();

        return new PagingResults<FileInfo>()
        {

            @Override
            public List<FileInfo> getPage()
            {
                return page;
            }

            @Override
            public String getQueryExecutionId()
            {
                return queryExecutionId;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalResultCount;
            }

            @Override
            public boolean hasMoreItems()
            {
                return hasMoreItems;
            }
        };
    }

    @Override
    public PagingResults<FileInfo> list(final NodeRef rootNodeRef, final Set<QName> searchTypeQNames,
                final Set<QName> ignoreAspectQNames, final List<Pair<QName, Boolean>> sortProps,
                final PagingRequest pagingRequest)
    {
        if (canVirtualize(rootNodeRef))
        {
            final Reference reference = smartStore.virtualize(rootNodeRef);
            List<Pair<QName, Boolean>> sortingPropoerties = sortProps;
            if (sortingPropoerties == null || sortingPropoerties.isEmpty())
            {
                sortingPropoerties = Arrays.asList(new Pair<QName, Boolean>(ContentModel.PROP_NAME,
                                                                            true));
            }

            if (mergeActualNode(reference))
            {
                PagingResults<Reference> virtualChildren = smartStore.list(reference,
                                                                             false,
                                                                             true,
                                                                             searchTypeQNames,
                                                                             Collections.<QName> emptySet(),
                                                                             ignoreAspectQNames,
                                                                             sortProps,
                                                                             new PagingRequest(0));

                PagingResultsSource<FileInfo> superSource = new PagingResultsSource<FileInfo>()
                {
                    @Override
                    public PagingResults<FileInfo> retrieve(PagingRequest pr) throws PageCollationException
                    {
                        try
                        {
                            PagingResults<FileInfo> result = getTrait().list(actualNodeFrom(reference),
                                                                             searchTypeQNames,
                                                                             ignoreAspectQNames,
                                                                             sortProps,
                                                                             pr);
                            return result;
                        }
                        catch (VirtualizationException e)
                        {
                            throw new PageCollationException(e);
                        }

                    }
                };

                FileInfoPropsComparator comparator = new FileInfoPropsComparator(sortingPropoerties);

                try
                {
                    return new PageCollator<FileInfo>().collate(asFileInfoResults(environment,
                                                                                  virtualChildren,
                                                                                  smartStore).getPage(),
                                                                superSource,
                                                                pagingRequest,
                                                                comparator);
                }
                catch (PageCollationException error)
                {
                    throw new VirtualizationException(error);
                }

            }
            else
            {
                PagingResults<Reference> children = smartStore.list(reference,
                                                                      true,
                                                                      true,
                                                                      searchTypeQNames,
                                                                      Collections.<QName> emptySet(),
                                                                      ignoreAspectQNames,
                                                                      sortingPropoerties,
                                                                      pagingRequest);

                return asFileInfoResults(environment,
                                         children,
                                         smartStore);
            }
        }

        return getTrait().list(rootNodeRef,
                               searchTypeQNames,
                               ignoreAspectQNames,
                               sortProps,
                               pagingRequest);
    }

    @Override
    public List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders)
    {
        return search(contextNodeRef,
                      namePattern,
                      true,
                      true,
                      false);
    }

    @Override
    public List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean fileSearch, boolean folderSearch,
                boolean includeSubFolders)
    {
        // We merge the virtual search results wit actual results only in case
        // that
        // namePattern is null or *, since the right search results are obtained
        // from
        // searchService.selectNodes() that uses getChildAssociation from
        // VirtualNodeService witch was implemented for implementing the
        // download-as-zip feature issue

        if (namePattern == null || namePattern.equals("*"))
        {
            if (canVirtualize(contextNodeRef))
            {
                Reference reference = smartStore.virtualize(contextNodeRef);
                List<Reference> virtualNodes = Collections.emptyList();
                if (!includeSubFolders)
                {
                    virtualNodes = smartStore.search(reference,
                                                       namePattern,
                                                       fileSearch,
                                                       folderSearch,
                                                       false);
                }
                List<FileInfo> searchResult = asFileInfos(virtualNodes,
                                                          smartStore,
                                                          environment);

                if (mergeActualNode(reference))
                {
                    List<FileInfo> actualSearch = getTrait().search(actualNodeFrom(reference),
                                                                    namePattern,
                                                                    fileSearch,
                                                                    folderSearch,
                                                                    includeSubFolders);
                    searchResult.addAll(actualSearch);
                }

                return searchResult;
            }
        }
        return getTrait().search(contextNodeRef,
                                 namePattern,
                                 fileSearch,
                                 folderSearch,
                                 includeSubFolders);
    }

    @Override
    public FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return getTrait().rename(smartStore.materializeIfPossible(sourceNodeRef),
                                 newName);
    }

    @Override
    public PagingResults<FileInfo> list(NodeRef contextNodeRef, boolean files, boolean folders, Set<QName> ignoreQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {

        return VirtualFileFolderServiceExtension.this.list(contextNodeRef,
                                                           files,
                                                           folders,
                                                           null,
                                                           ignoreQNames,
                                                           sortProps,
                                                           pagingRequest);
    }
}
