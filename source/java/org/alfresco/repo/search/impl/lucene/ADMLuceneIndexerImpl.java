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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.MLTokenDuplicator;
import org.alfresco.repo.search.impl.lucene.analysis.VerbatimAnalyser;
import org.alfresco.repo.search.impl.lucene.fts.FTSIndexerAware;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * The implementation of the lucene based indexer. Supports basic transactional behaviour if used on its own.
 * 
 * @author andyh
 * @author dward
 */
public class ADMLuceneIndexerImpl extends AbstractLuceneIndexerImpl<NodeRef> implements ADMLuceneIndexer
{
    /**
     * The maximum number of parent associations a node can have before we choose to cascade reindex its parent rather than itself.
     */
    private static final int PATH_GENERATION_FACTOR = 5;
    
    static Log s_logger = LogFactory.getLog(ADMLuceneIndexerImpl.class);

    /**
     * The node service we use to get information about nodes
     */
    NodeService nodeService;

    /**
     * The tenant service we use for multi-tenancy
     */
    TenantService tenantService;

    /**
     * Content service to get content for indexing.
     */
    ContentService contentService;

    /**
     * Call back to make after doing non atomic indexing
     */
    FTSIndexerAware callBack;

    /**
     * Count of remaining items to index non atomically
     */
    int remainingCount = 0;

    /**
     * A list of stuff that requires non atomic indexing
     */
    private Map<String, Deque<Helper>> toFTSIndex = Collections.emptyMap();

    /**
     * Default construction
     */
    ADMLuceneIndexerImpl()
    {
        super();
    }

    /**
     * IOC setting of the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * IOC setting of the tenant service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * IOC setting of the content service
     * 
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /*
     * Indexer Implementation
     */

    public void createNode(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            NodeRef parentRef = relationshipRef.getParentRef();
            Path path = parentRef == null ? new Path() : nodeService.getPath(parentRef);
            path.append(new ChildAssocElement(relationshipRef));
            s_logger.debug("Create node " + path + " " + relationshipRef.getChildRef());
        }
        checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
        try
        {
            NodeRef childRef = relationshipRef.getChildRef();
            if (!childRef.getStoreRef().equals(store))
            {
                throw new LuceneIndexException("Create node failed - node is not in the required store");
            }
            // If we have the root node we delete all other root nodes first
            if ((relationshipRef.getParentRef() == null) && tenantService.getBaseName(childRef).equals(nodeService.getRootNode(childRef.getStoreRef())))
            {
                addRootNodesToDeletionList();
                s_logger.warn("Detected root node addition: deleting all nodes from the index");
            }
            index(childRef);
        }
        catch (LuceneIndexException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException("Create node failed", e);
        }
    }

    private void addRootNodesToDeletionList()
    {
        IndexReader mainReader = null;
        try
        {
            try
            {
                mainReader = getReader();
                TermDocs td = mainReader.termDocs(new Term("ISROOT", "T"));
                while (td.next())
                {
                    int doc = td.doc();
                    Document document = mainReader.document(doc);
                    String id = document.get("ID");
                    NodeRef ref = new NodeRef(id);
                    deleteImpl(ref.toString(), getDeltaReader(), mainReader);
                }
                td.close();
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("Failed to delete all primary nodes", e);
            }
        }
        finally
        {
            if (mainReader != null)
            {
                try
                {
                    mainReader.close();
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Filed to close main reader", e);
                }
            }
            try
            {
                closeDeltaReader();
            }
            catch (Exception e)
            {
                s_logger.warn("Failed to close delta reader", e);
            }
        }
    }

    public void updateNode(NodeRef nodeRef) throws LuceneIndexException
    {
        nodeRef = tenantService.getName(nodeRef);

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Update node " + nodeService.getPath(nodeRef) + " " + nodeRef);
        }
        checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
        try
        {
            if (!nodeRef.getStoreRef().equals(store))
            {
                throw new LuceneIndexException("Update node failed - node is not in the required store");
            }
            reindex(nodeRef, false);
        }
        catch (LuceneIndexException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException("Update node failed", e);
        }
    }

    public void deleteNode(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            NodeRef parentRef = relationshipRef.getParentRef();
            Path path = parentRef == null ? new Path() : nodeService.getPath(parentRef);
            path.append(new ChildAssocElement(relationshipRef));
            s_logger.debug("Delete node " + path + " " + relationshipRef.getChildRef());
        }
        checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
        try
        {
            if (!relationshipRef.getChildRef().getStoreRef().equals(store))
            {
                throw new LuceneIndexException("Delete node failed - node is not in the required store");
            }
            delete(relationshipRef.getChildRef());
        }
        catch (LuceneIndexException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException("Delete node failed", e);
        }
    }

    private void childRelationshipEvent(ChildAssociationRef relationshipRef, String event) throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            NodeRef parentRef = relationshipRef.getParentRef();
            Path path = parentRef == null ? new Path() : nodeService.getPath(parentRef);
            path.append(new ChildAssocElement(relationshipRef));
            s_logger.debug(event + " " + path + " " + relationshipRef.getChildRef());
        }
        checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
        try
        {
            if (!relationshipRef.getChildRef().getStoreRef().equals(store))
            {
                throw new LuceneIndexException(event + " failed - node is not in the required store");
            }
            final NodeRef parentRef = relationshipRef.getParentRef();
            final NodeRef childRef = relationshipRef.getChildRef(); 
            if (parentRef != null)
            {
                // If the child has a lot of secondary parents, its cheaper to cascade reindex its parent
                // rather than itself
                if (doInReadthroughTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    @Override
                    public Boolean execute() throws Throwable
                    {
                        try
                        {
                            return nodeService.getParentAssocs(childRef).size() > PATH_GENERATION_FACTOR;
                        }
                        catch (InvalidNodeRefException e)
                        {
                            // The node may have disappeared under our feet!
                            return false;
                        }
                    }
                }))
                {
                    reindex(parentRef, true);
                    reindex(childRef, false);
                }
                // Otherwise, it's cheaper to re-evaluate all the paths to this node
                else
                {
                    reindex(childRef, true);
                }
            }
            else
            {
                reindex(childRef, true);                                
            }
        }
        catch (LuceneIndexException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException(event + " failed", e);
        }
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        childRelationshipEvent(relationshipRef, "Create child relationship");
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef) throws LuceneIndexException
    {
        childRelationshipEvent(relationshipBeforeRef, "Update child relationship");
        childRelationshipEvent(relationshipAfterRef, "Update child relationship");
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        childRelationshipEvent(relationshipRef, "Delete child relationship");
    }

    /**
     * Are we deleting leaves only (not meta data)
     * 
     * @return - deleting only nodes.
     */
    public boolean getDeleteOnlyNodes()
    {
        return true;
    }

    /**
     * Generate an indexer
     * 
     * @param storeRef
     * @param deltaId
     * @param config
     * @return - the indexer instance
     * @throws LuceneIndexException
     */
    public static ADMLuceneIndexerImpl getUpdateIndexer(StoreRef storeRef, String deltaId, LuceneConfig config) throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Creating indexer");
        }
        ADMLuceneIndexerImpl indexer = new ADMLuceneIndexerImpl();
        indexer.setLuceneConfig(config);
        indexer.initialise(storeRef, deltaId);
        return indexer;
    }

    public static ADMLuceneNoActionIndexerImpl getNoActionIndexer(StoreRef storeRef, String deltaId, LuceneConfig config) throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Creating indexer");
        }
        ADMLuceneNoActionIndexerImpl indexer = new ADMLuceneNoActionIndexerImpl();
        indexer.setLuceneConfig(config);
        indexer.initialise(storeRef, deltaId);
        return indexer;
    }

    /*
     * Transactional support Used by the resource manager for indexers.
     */

    void doFTSIndexCommit() throws LuceneIndexException
    {
        IndexReader mainReader = null;
        IndexReader deltaReader = null;
        IndexSearcher mainSearcher = null;
        IndexSearcher deltaSearcher = null;

        try
        {
            try
            {
                mainReader = getReader();
                deltaReader = getDeltaReader();
                mainSearcher = new IndexSearcher(mainReader);
                deltaSearcher = new IndexSearcher(deltaReader);

                for (Map.Entry<String, Deque<Helper>> entry : toFTSIndex.entrySet())
                {
                    // Delete both the document and the supplementary FTSSTATUS documents (if there are any)
                    deletions.add(entry.getKey());
                    for (Helper helper : entry.getValue())
                    {
                        deletions.add(helper.id);
                    }
                }

            }
            finally
            {
                if (deltaSearcher != null)
                {
                    try
                    {
                        deltaSearcher.close();
                    }
                    catch (IOException e)
                    {
                        s_logger.warn("Failed to close delta searcher", e);
                    }
                }
                if (mainSearcher != null)
                {
                    try
                    {
                        mainSearcher.close();
                    }
                    catch (IOException e)
                    {
                        s_logger.warn("Failed to close main searcher", e);
                    }
                }
                try
                {
                    closeDeltaReader();
                }
                catch (LuceneIndexException e)
                {
                    s_logger.warn("Failed to close delta reader", e);
                }
                if (mainReader != null)
                {
                    try
                    {
                        mainReader.close();
                    }
                    catch (IOException e)
                    {
                        s_logger.warn("Failed to close main reader", e);
                    }
                }
            }

            setInfo(docs, getDeletions(), getContainerDeletions(), getDeleteOnlyNodes());
            // mergeDeltaIntoMain(new LinkedHashSet<Term>());
        }
        catch (IOException e)
        {
            // If anything goes wrong we try and do a roll back
            rollback();
            throw new LuceneIndexException("Commit failed", e);
        }
        catch (LuceneIndexException e)
        {
            // If anything goes wrong we try and do a roll back
            rollback();
            throw new LuceneIndexException("Commit failed", e);
        }
        finally
        {
            // Make sure we tidy up
            // deleteDelta();
        }

    }

    private static class Pair<F, S>
    {
        private F first;

        private S second;

        /**
         * Helper class to hold two related objects
         * 
         * @param first
         * @param second
         */
        public Pair(F first, S second)
        {
            this.first = first;
            this.second = second;
        }

        /**
         * Get the first
         * 
         * @return - first
         */
        public F getFirst()
        {
            return first;
        }

        /**
         * Get the second
         * 
         * @return -second
         */
        public S getSecond()
        {
            return second;
        }
    }

    protected Set<String> deleteImpl(String nodeRef, IndexReader deltaReader, IndexReader mainReader)
            throws LuceneIndexException, IOException
    {
        Set<String> containerRefs = new LinkedHashSet<String>();
        // Delete all and reindex as they could be secondary links we have deleted and they need to be updated.
        // Most will skip any indexing as they will really have gone.
        Set <String> temp = deleteContainerAndBelow(nodeRef, deltaReader, true, true);
        containerRefs.addAll(temp);
        temp = deleteContainerAndBelow(nodeRef, mainReader, false, true);
        containerRefs.addAll(temp);
        // Only mask out the container if it is present in the main index
        if (!temp.isEmpty())
        {
            containerDeletions.add(nodeRef);
        }

        Set<String> leafrefs = new LinkedHashSet<String>();
        temp = deletePrimary(containerRefs, deltaReader, true);
        leafrefs.addAll(temp);
        temp = deletePrimary(containerRefs, mainReader, false);
        leafrefs.addAll(temp);
        
        Set<String> refs = new LinkedHashSet<String>();
        refs.addAll(containerRefs);
        refs.addAll(leafrefs);
        deletions.addAll(refs);

        // make sure leaves are also removed from the delta before reindexing

        for(String id : refs)
        {
            // Only delete the leaves, as we may have hit secondary associations
            deleteLeafOnly(id, deltaReader, true);
        }
        return refs;    
    }

    public List<Document> createDocuments(final String stringNodeRef, final FTSStatus ftsStatus,
            final boolean indexAllProperties, final boolean includeDirectoryDocuments, final boolean cascade,
            final Set<Path> pathsProcessedSinceFlush,
            final Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush, final IndexReader deltaReader,
            final IndexReader mainReader)
    {
        if (tenantService.isEnabled() && ((AuthenticationUtil.getRunAsUser() == null) || (AuthenticationUtil.isRunAsUserTheSystemUser())))
        {
            // ETHREEOH-2014 - dictionary access should be in context of tenant (eg. full reindex with MT dynamic
            // models)
            return AuthenticationUtil.runAs(new RunAsWork<List<Document>>()
            {
                public List<Document> doWork()
                {
                    return createDocumentsImpl(stringNodeRef, ftsStatus, indexAllProperties, includeDirectoryDocuments,
                            cascade, pathsProcessedSinceFlush, childAssociationsSinceFlush, deltaReader, mainReader);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantService.getDomain(new NodeRef(stringNodeRef).getStoreRef().getIdentifier())));
        }
        else
        {
            return createDocumentsImpl(stringNodeRef, ftsStatus, indexAllProperties, includeDirectoryDocuments,
                    cascade, pathsProcessedSinceFlush, childAssociationsSinceFlush, deltaReader, mainReader);
        }
    }

    private List<Document> createDocumentsImpl(final String stringNodeRef, FTSStatus ftsStatus,
            boolean indexAllProperties, boolean includeDirectoryDocuments, final boolean cascade,
            final Set<Path> pathsProcessedSinceFlush,
            final Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush, final IndexReader deltaReader,
            final IndexReader mainReader)
    {
        final NodeRef nodeRef = new NodeRef(stringNodeRef);
        final NodeRef.Status nodeStatus = nodeService.getNodeStatus(nodeRef);             // DH: Let me know if this field gets dropped (performance)
        final List<Document> docs = new LinkedList<Document>();
        if (nodeStatus == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);            
        }
        else if (nodeStatus.isDeleted())
        {
            // If we are being called in non FTS mode on a deleted node, we must still create a new FTS marker
            // document, in case FTS is currently in progress and about to restore our node!
            addFtsStatusDoc(docs, ftsStatus, nodeRef, nodeStatus);
            return docs;
        }

        final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        boolean isRoot = nodeRef.equals(tenantService.getName(nodeService.getRootNode(nodeRef.getStoreRef())));

        // Generate / regenerate all applicable parent paths as the system user (the current user doesn't necessarily have access
        // to all of these)
        if (includeDirectoryDocuments)
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // We we must cope with the possibility of the container not existing for some of this node's parents
                    for (ChildAssociationRef assocRef: nodeService.getParentAssocs(nodeRef))
                    {
                        NodeRef parentRef = tenantService.getName(assocRef.getParentRef());
                        if (!childAssociationsSinceFlush.containsKey(parentRef))
                        {
                            String parentRefSString = parentRef.toString();
                            if (!locateContainer(parentRefSString, deltaReader)
                                    && !locateContainer(parentRefSString, mainReader))
                            {
                                generateContainersAndBelow(nodeService.getPaths(parentRef, false), docs, false,
                                        pathsProcessedSinceFlush, childAssociationsSinceFlush);
                            }
                        }
                    }
                    
                    // Now regenerate the containers for this node, cascading if necessary
                    // Only process 'containers' - not leaves
                    if (isCategory(getDictionaryService().getType(nodeService.getType(nodeRef)))
                            || mayHaveChildren(nodeRef)
                            && !getCachedChildren(childAssociationsSinceFlush, nodeRef).isEmpty())
                    {
                        generateContainersAndBelow(nodeService.getPaths(nodeRef, false), docs, cascade,
                                pathsProcessedSinceFlush, childAssociationsSinceFlush);
                    }
                    
                    return null;
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantService.getDomain(nodeRef
                    .getStoreRef().getIdentifier())));
        }

        // check index control
        
        if(properties.containsKey(ContentModel.PROP_IS_INDEXED))
        {
            Serializable sValue = properties.get(ContentModel.PROP_IS_INDEXED);
            if(sValue != null)
            {
                Boolean isIndexed = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
                if((isIndexed != null) && (isIndexed.booleanValue() == false))
                {
                    return docs;
                }
            }
        }
        
        boolean isContentIndexedForNode = true;
        if(properties.containsKey(ContentModel.PROP_IS_CONTENT_INDEXED))
        {
            Serializable sValue = properties.get(ContentModel.PROP_IS_CONTENT_INDEXED);
            if(sValue != null)
            {
                Boolean isIndexed = DefaultTypeConverter.INSTANCE.convert(Boolean.class, sValue);
                if((isIndexed != null) && (isIndexed.booleanValue() == false))
                {
                    isContentIndexedForNode = false;
                }
            }
        }

        Document xdoc = new Document();
        xdoc.add(new Field("ID", stringNodeRef, Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
        xdoc.add(new Field("TX", nodeStatus.getChangeTxnId(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
        boolean isAtomic = true;
        for (QName propertyName : properties.keySet())
        {
            Serializable value = properties.get(propertyName);

            value = convertForMT(propertyName, value);

            if (indexAllProperties)
            {
                indexProperty(nodeRef, propertyName, value, xdoc, false, isContentIndexedForNode);
            }
            else
            {
                isAtomic &= indexProperty(nodeRef, propertyName, value, xdoc, true, isContentIndexedForNode);
            }
        }

        StringBuilder qNameBuffer = new StringBuilder(64);
        StringBuilder assocTypeQNameBuffer = new StringBuilder(64);

        if (!isRoot)
        {
            for (Pair<ChildAssociationRef, QName> pair : getAllParents(nodeRef, properties))
            {
                ChildAssociationRef qNameRef = tenantService.getName(pair.getFirst());
                if ((qNameRef != null) && (qNameRef.getParentRef() != null) && (qNameRef.getQName() != null))
                {
                    if (qNameBuffer.length() > 0)
                    {
                        qNameBuffer.append(";/");
                        assocTypeQNameBuffer.append(";/");
                    }
                    qNameBuffer.append(ISO9075.getXPathName(qNameRef.getQName()));
                    assocTypeQNameBuffer.append(ISO9075.getXPathName(qNameRef.getTypeQName()));
                    xdoc.add(new Field("PARENT", qNameRef.getParentRef().toString(), Field.Store.YES,
                            Field.Index.NO_NORMS, Field.TermVector.NO));
                    // xdoc.add(new Field("ASSOCTYPEQNAME", ISO9075.getXPathName(qNameRef.getTypeQName()),
                    // Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
                    xdoc.add(new Field("LINKASPECT", (pair.getSecond() == null) ? "" : ISO9075.getXPathName(pair
                            .getSecond()), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
                }
            }

        }

        // Root Node
        if (isRoot)
        {
            // TODO: Does the root element have a QName?
            xdoc.add(new Field("ISCONTAINER", "T", Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            xdoc.add(new Field("PATH", "", Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            xdoc.add(new Field("QNAME", "", Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            xdoc.add(new Field("ISROOT", "T", Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
            xdoc.add(new Field("PRIMARYASSOCTYPEQNAME", ISO9075.getXPathName(ContentModel.ASSOC_CHILDREN), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            xdoc.add(new Field("ISNODE", "T", Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
            docs.add(xdoc);

        }
        else
        // not a root node
        {
            xdoc.add(new Field("QNAME", qNameBuffer.toString(), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            xdoc.add(new Field("ASSOCTYPEQNAME", assocTypeQNameBuffer.toString(), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            // xdoc.add(new Field("PARENT", parentBuffer.toString(), true, true,
            // true));

            ChildAssociationRef primary = nodeService.getPrimaryParent(nodeRef);
            xdoc.add(new Field("PRIMARYPARENT", tenantService.getName(primary.getParentRef()).toString(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            xdoc.add(new Field("PRIMARYASSOCTYPEQNAME", ISO9075.getXPathName(primary.getTypeQName()), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
            QName typeQName = nodeService.getType(nodeRef);

            xdoc.add(new Field("TYPE", ISO9075.getXPathName(typeQName), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            for (QName classRef : nodeService.getAspects(nodeRef))
            {
                xdoc.add(new Field("ASPECT", ISO9075.getXPathName(classRef), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            }

            xdoc.add(new Field("ISROOT", "F", Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
            xdoc.add(new Field("ISNODE", "T", Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));

            // Record the need for FTS on this node and transaction with a supplementary document. That way we won't
            // forget about it if FTS is already in progress for an earlier transaction!
            if (!isAtomic && !indexAllProperties)
            {
                addFtsStatusDoc(docs, ftsStatus, nodeRef, nodeStatus);
            }

            // {
            docs.add(xdoc);
            // }
        }
        return docs;
    }
    
    private void generateContainersAndBelow(List<Path> paths, List<Document> docs, boolean cascade,
            Set<Path> pathsProcessedSinceFlush, Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush)
    {
        if (paths.isEmpty())
        {
            return;
        }

        for (Path path: paths)
        {
            NodeRef nodeRef = tenantService.getName(((ChildAssocElement) path.last()).getRef().getChildRef());
            
            // Prevent duplication of path cascading
            if (pathsProcessedSinceFlush.add(path))
            {
                // Categories have special powers - generate their container regardless of their actual children
                boolean isCategory = isCategory(getDictionaryService().getType(nodeService.getType(nodeRef)));

                // For other containers, we only add a doc if they actually have children
                if (!isCategory)
                {
                    // Only process 'containers' - not leaves
                    if (!mayHaveChildren(nodeRef))
                    {
                        continue;
                    }
        
                    // Only process 'containers' - not leaves
                    if (getCachedChildren(childAssociationsSinceFlush, nodeRef).isEmpty())
                    {
                        continue;
                    }
                }

                // Skip the root, which is a single document
                if (path.size() > 1)
                {
                    String pathString = path.toString();    
                    if ((pathString.length() > 0) && (pathString.charAt(0) == '/'))
                    {
                        pathString = pathString.substring(1);
                    }
                    Document directoryEntry = new Document();
                    directoryEntry.add(new Field("ID", nodeRef.toString(), Field.Store.YES,
                            Field.Index.NO_NORMS, Field.TermVector.NO));
                    directoryEntry.add(new Field("PATH", pathString, Field.Store.YES, Field.Index.TOKENIZED,
                            Field.TermVector.NO));
                    for (NodeRef parent : getParents(path))
                    {
                        directoryEntry.add(new Field("ANCESTOR", tenantService.getName(parent).toString(),
                                Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                    }
                    directoryEntry.add(new Field("ISCONTAINER", "T", Field.Store.YES, Field.Index.NO_NORMS,
                            Field.TermVector.NO));
            
                    if (isCategory)
                    {
                        directoryEntry.add(new Field("ISCATEGORY", "T", Field.Store.YES, Field.Index.NO_NORMS,
                                Field.TermVector.NO));
                    }
            
                    docs.add(directoryEntry);
                }
            }
        
            if (cascade)
            {
                List<Path> childPaths = new LinkedList<Path>();
                for (ChildAssociationRef childRef : getCachedChildren(childAssociationsSinceFlush, nodeRef))
                {
                    childPaths.add(new Path().append(path).append(new Path.ChildAssocElement(childRef)));
                }
                generateContainersAndBelow(childPaths, docs, true, pathsProcessedSinceFlush,
                        childAssociationsSinceFlush);
            }
        }
    }

    private List<ChildAssociationRef> getCachedChildren(
            Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush, NodeRef nodeRef)
    {
        List <ChildAssociationRef> children = childAssociationsSinceFlush.get(nodeRef);

        // Cache the children in case there are many paths to the same node
        if (children == null)
        {
            children = nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childRef : children)
            {
                // We don't want index numbers in generated paths
                childRef.setNthSibling(-1);                
            }
            childAssociationsSinceFlush.put(nodeRef, children);
        }
        return children;
    }

    private void addFtsStatusDoc(List<Document> docs, FTSStatus ftsStatus, NodeRef nodeRef,
            NodeRef.Status nodeStatus)
    {
        // If we are being called during FTS failover, then don't bother generating a new doc
        if (ftsStatus == FTSStatus.Clean)
        {
            return;
        }
        Document doc = new Document();
        doc.add(new Field("ID", GUID.generate(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
        doc.add(new Field("FTSREF", nodeRef.toString(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
        doc
                .add(new Field("TX", nodeStatus.getChangeTxnId(), Field.Store.YES, Field.Index.NO_NORMS,
                        Field.TermVector.NO));
        doc.add(new Field("FTSSTATUS", ftsStatus.name(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
        docs.add(doc);
    }
    
    /**
     * @throws LuceneIndexException
     */
    public void flushPending() throws LuceneIndexException
    {
        IndexReader mainReader = null;
        try
        {
            saveDelta();            
            
            if (commandList.isEmpty())
            {
                return;
            }
            
            Map<String,Action> nodeActionMap = new LinkedHashMap<String, Action>(commandList.size() * 2);

            // First, apply deletions and work out a 'flattened' list of reindex actions
            mainReader = getReader();
            IndexReader deltaReader = getDeltaReader();
            for (Command<NodeRef> command : commandList)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(command.action + ": " + command.ref);
                }
                String nodeRef = command.ref.toString();
                switch(command.action)
                {
                case INDEX:
                    // No deletions
                    if (nodeActionMap.get(nodeRef) != Action.CASCADEREINDEX)
                    {
                        nodeActionMap.put(nodeRef, Action.INDEX);
                    }
                    break;
                case REINDEX:
                    // Remove from delta if present
                    deleteLeafOnly(nodeRef, deltaReader, true);
                    
                    // Only mask out the node if it is present in the main index
                    if (deleteLeafOnly(nodeRef, mainReader, false));
                    {
                        deletions.add(nodeRef);
                    }                    
                    if (!nodeActionMap.containsKey(nodeRef))
                    {
                        nodeActionMap.put(nodeRef, Action.REINDEX);
                    }
                    break;
                case DELETE:
                    // First ensure leaves with secondary references to the deleted node are reindexed
                    Set<String> nodeRefSet = Collections.singleton(nodeRef);
                    deleteReference(nodeRefSet, deltaReader, true);
                    Set<String> temp = deleteReference(nodeRefSet, mainReader, false);
                    if (!temp.isEmpty())
                    {
                        deletions.addAll(temp);
                        for (String ref : temp)
                        {
                            if (!nodeActionMap.containsKey(ref))
                            {
                                nodeActionMap.put(ref, Action.REINDEX);
                            }
                        }
                    }
                    // Now fall through to cascade reindex case
                case CASCADEREINDEX:
                    deleteContainerAndBelow(nodeRef, deltaReader, true, true);
                    // Only mask out the container if it is present in the main index
                    temp = deleteContainerAndBelow(nodeRef, mainReader, false, true);
                    if (!temp.isEmpty())
                    {
                        containerDeletions.add(nodeRef);
                    }
                    // Only mask out the node if it is present in the main index
                    if (temp.contains(nodeRef))
                    {
                        deletions.add(nodeRef);
                    }
                    nodeActionMap.put(nodeRef, Action.CASCADEREINDEX);
                    break;
                }
            }
            
            // Now reindex what needs indexing!
            Set<Path> pathsProcessedSinceFlush = new HashSet<Path>(97);
            Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush = new HashMap<NodeRef, List<ChildAssociationRef>>(97);

            // First do the reading
            List<Document> docs = new LinkedList<Document>();
            for (Map.Entry<String, Action> entry : nodeActionMap.entrySet())
            {
                String nodeRef = entry.getKey();
                try
                {
                    switch (entry.getValue())
                    {
                    case INDEX:
                        docs.addAll(readDocuments(nodeRef, FTSStatus.New, false, true, false, pathsProcessedSinceFlush,
                                childAssociationsSinceFlush, deltaReader, mainReader));
                        break;
                    case REINDEX:
                        docs.addAll(readDocuments(nodeRef, FTSStatus.Dirty, false, false, false,
                                pathsProcessedSinceFlush, childAssociationsSinceFlush, deltaReader, mainReader));
                        break;
                    case CASCADEREINDEX:
                        // Add the nodes for index
                        docs.addAll(readDocuments(nodeRef, FTSStatus.Dirty, false, true, true,
                                pathsProcessedSinceFlush, childAssociationsSinceFlush, deltaReader, mainReader));
                        break;
                    }
                }
                catch (InvalidNodeRefException e)
                {
                    // The node does not exist
                }
            }
            closeDeltaReader();

            // Now the writing
            IndexWriter writer = getDeltaWriter();        
            for (Document doc : docs)
            {
                try
                {
                    writer.addDocument(doc);
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Failed to add document to index", e);
                }
            }
            
            commandList.clear();
            this.docs = writer.docCount();
        }
        catch (IOException e)
        {
            // If anything goes wrong we try and do a roll back
            throw new LuceneIndexException("Failed to flush index", e);
        }
        finally
        {
            if (mainReader != null)
            {
                try
                {
                    mainReader.close();
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Filed to close main reader", e);
                }
            }
            // Make sure deletes are sent
            try
            {
                closeDeltaReader();
            }
            catch (IOException e)
            {

            }
            // Make sure writes and updates are sent.
            try
            {
                closeDeltaWriter();
            }
            catch (IOException e)
            {

            }
        }
    }

    private Serializable convertForMT(QName propertyName, Serializable inboundValue)
    {
        if (!tenantService.isEnabled())
        {
            // no conversion
            return inboundValue;
        }

        PropertyDefinition propertyDef = getDictionaryService().getProperty(propertyName);
        if ((propertyDef != null)
                && ((propertyDef.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) || (propertyDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))))
        {
            if (inboundValue instanceof Collection)
            {
                Collection<NodeRef> in = (Collection<NodeRef>) inboundValue;
                ArrayList<NodeRef> out = new ArrayList<NodeRef>(in.size());
                for (NodeRef o : in)
                {
                    out.add(tenantService.getName(o));
                }
                return out;
            }
            else
            {
                return tenantService.getName((NodeRef) inboundValue);
            }
        }

        return inboundValue;
    }

    /**
     * @param indexAtomicPropertiesOnly
     *            true to ignore all properties that must be indexed non-atomically
     * @return Returns true if the property was indexed atomically, or false if it should be done asynchronously
     */
    protected boolean indexProperty(NodeRef nodeRef, QName propertyName, Serializable value, Document doc, boolean indexAtomicPropertiesOnly, boolean isContentIndexedForNode)
    {
        String attributeName = "@" + QName.createQName(propertyName.getNamespaceURI(), ISO9075.encode(propertyName.getLocalName()));

        boolean store = true;
        boolean index = true;
        IndexTokenisationMode tokenise = IndexTokenisationMode.TRUE;
        boolean atomic = true;
        boolean isContent = false;
        boolean isMultiLingual = false;
        boolean isText = false;
        boolean isDateTime = false;

        PropertyDefinition propertyDef = getDictionaryService().getProperty(propertyName);
        if (propertyDef != null)
        {
            index = propertyDef.isIndexed();
            store = propertyDef.isStoredInIndex();
            tokenise = propertyDef.getIndexTokenisationMode();
            atomic = propertyDef.isIndexedAtomically();
            isContent = propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT);
            isMultiLingual = propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT);
            isText = propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT);
            if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
            {
                DataTypeDefinition dataType = propertyDef.getDataType();
                String analyserClassName = propertyDef.resolveAnalyserClassName();
                isDateTime = analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName());
            }
        }
        if (value == null)
        {
            // the value is null
            return true;
        }
        else if (indexAtomicPropertiesOnly && !atomic)
        {
            // we are only doing atomic properties and the property is definitely non-atomic
            return false;
        }

        if (!indexAtomicPropertiesOnly)
        {
            doc.removeFields(propertyName.toString());
        }
        boolean wereAllAtomic = true;
        // convert value to String
        for (Serializable serializableValue : DefaultTypeConverter.INSTANCE.getCollection(Serializable.class, value))
        {
            String strValue = null;
            try
            {
                strValue = DefaultTypeConverter.INSTANCE.convert(String.class, serializableValue);
            }
            catch (TypeConversionException e)
            {
                doc.add(new Field(attributeName, NOT_INDEXED_NO_TYPE_CONVERSION, Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
                continue;
            }
            if (strValue == null)
            {
                // nothing to index
                continue;
            }

            if (isContent)
            {
                // Content is always tokenised

                ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, serializableValue);
                if (!index || contentData.getMimetype() == null)
                {
                    // no mimetype or property not indexed
                    continue;
                }
                // store mimetype in index - even if content does not index it is useful
                // Added szie and locale - size needs to be tokenised correctly
                doc.add(new Field(attributeName + ".mimetype", contentData.getMimetype(), Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
                doc.add(new Field(attributeName + ".size", Long.toString(contentData.getSize()), Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));

                // TODO: Use the node locale in preferanced to the system locale
                Locale locale = contentData.getLocale();
                if (locale == null)
                {
                    Serializable localeProperty = nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
                    if (localeProperty != null)
                    {
                        locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeProperty);
                    }
                }
                if (locale == null)
                {
                    locale = I18NUtil.getLocale();
                }
                doc.add(new Field(attributeName + ".locale", locale.toString().toLowerCase(), Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

                if(getLuceneConfig().isContentIndexingEnabled() && isContentIndexedForNode)
                {
                    // Avoid handling (or even getting) a reader if (maxAtomicTransformationTime <= 0)
                    // ALF-5677: Extremely long launch of the Alfresco server with connector V1.2
                    boolean avoidReader = maxAtomicTransformationTime <= 0 && indexAtomicPropertiesOnly;
                    ContentReader reader = avoidReader ? null : contentService.getReader(nodeRef, propertyName);
                    if (reader != null && reader.exists())
                    {
                        // We have a reader, so use it
                        boolean readerReady = true;
                        // transform if necessary (it is not a UTF-8 text document)
                        if (!EqualsHelper.nullSafeEquals(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN) || !EqualsHelper.nullSafeEquals(reader.getEncoding(), "UTF-8"))
                        {
                            // get the transformer
                            ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
                            // is this transformer good enough?
                            if (transformer == null)
                            {
                                // log it
                                if (s_logger.isInfoEnabled())
                                {
                                    s_logger.info("Not indexed: No transformation: \n"
                                            + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN + " at " + nodeService.getPath(nodeRef));
                                }
                                // don't index from the reader
                                readerReady = false;
                                // not indexed: no transformation
                                // doc.add(new Field("TEXT", NOT_INDEXED_NO_TRANSFORMATION, Field.Store.NO,
                                // Field.Index.TOKENIZED, Field.TermVector.NO));
                                doc.add(new Field(attributeName, NOT_INDEXED_NO_TRANSFORMATION, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
                            }
                            else if (indexAtomicPropertiesOnly && transformer.getTransformationTime() > maxAtomicTransformationTime)
                            {
                                // only indexing atomic properties
                                // indexing will take too long, so push it to the background
                                wereAllAtomic = false;
                                readerReady = false;
                            }
                            else
                            {
                                // We have a transformer that is fast enough
                                ContentWriter writer = contentService.getTempWriter();
                                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                                // this is what the analyzers expect on the stream
                                writer.setEncoding("UTF-8");
                                try
                                {
                                    transformer.transform(reader, writer);
                                    // point the reader to the new-written content
                                    reader = writer.getReader();
                                    // Check that the reader is a view onto something concrete
                                    if (!reader.exists())
                                    {
                                        throw new ContentIOException("The transformation did not write any content, yet: \n"
                                                + "   transformer:     " + transformer + "\n" + "   temp writer:     " + writer);
                                    }
                                }
                                catch (ContentIOException e)
                                {
                                    // log it
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Not indexed: Transformation failed at " + nodeService.getPath(nodeRef), e);
                                    }
                                    // don't index from the reader
                                    readerReady = false;
                                    doc.add(new Field(attributeName, NOT_INDEXED_TRANSFORMATION_FAILED, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
                                }
                            }
                        }
                        // add the text field using the stream from the
                        // reader, but only if the reader is valid
                        if (readerReady)
                        {
                            InputStreamReader isr = null;
                            InputStream ris = reader.getReader().getContentInputStream();
                            try
                            {
                                isr = new InputStreamReader(ris, "UTF-8");
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                isr = new InputStreamReader(ris);
                            }
                            StringBuilder builder = new StringBuilder();
                            builder.append("\u0000").append(locale.toString()).append("\u0000");
                            StringReader prefix = new StringReader(builder.toString());
                            Reader multiReader = new MultiReader(prefix, isr);
                            doc.add(new Field(attributeName, multiReader, Field.TermVector.NO));
                        }
                    }
                    else if (avoidReader)
                    {
                        // Reader was deliberately not used; process in the background
                        wereAllAtomic = false;
                    }
                    else
                        // URL not present (null reader) or no content at the URL (file missing)
                    {
                        // log it
                        if (s_logger.isInfoEnabled())
                        {
                            s_logger.info("Not indexed: Content Missing \n"
                                    + "   node: " + nodeRef + " at " + nodeService.getPath(nodeRef) + "\n" + "   reader: " + reader + "\n" + "   content exists: "
                                    + (reader == null ? " --- " : Boolean.toString(reader.exists())));
                        }
                        // not indexed: content missing
                        doc.add(new Field(attributeName, NOT_INDEXED_CONTENT_MISSING, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
                    }
                }
                else
                {
                    wereAllAtomic = false;
                }
            }
            else
            {
                Field.Store fieldStore = store ? Field.Store.YES : Field.Store.NO;
                Field.Index fieldIndex;

                if (index)
                {
                    switch (tokenise)
                    {
                    case TRUE:
                    case BOTH:
                    default:
                        fieldIndex = Field.Index.TOKENIZED;
                        break;
                    case FALSE:
                        fieldIndex = Field.Index.UN_TOKENIZED;
                        break;

                    }
                }
                else
                {
                    fieldIndex = Field.Index.NO;
                }

                if ((fieldIndex != Field.Index.NO) || (fieldStore != Field.Store.NO))
                {
                    if (isMultiLingual)
                    {
                        MLText mlText = DefaultTypeConverter.INSTANCE.convert(MLText.class, serializableValue);
                        for (Locale locale : mlText.getLocales())
                        {
                            String localeString = mlText.getValue(locale);
                            if (localeString == null)
                            {
                                // No text for that locale
                                continue;
                            }
                            StringBuilder builder;
                            MLAnalysisMode analysisMode;
                            VerbatimAnalyser vba;
                            MLTokenDuplicator duplicator;
                            Token t;
                            switch (tokenise)
                            {
                            case TRUE:
                                builder = new StringBuilder();
                                builder.append("\u0000").append(locale.toString()).append("\u0000").append(localeString);
                                doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));
                                break;
                            case FALSE:
                                // analyse ml text
                                analysisMode = getLuceneConfig().getDefaultMLIndexAnalysisMode();
                                // Do the analysis here
                                vba = new VerbatimAnalyser(false);
                                duplicator = new MLTokenDuplicator(vba.tokenStream(attributeName, new StringReader(localeString)), locale, null, analysisMode);
                                try
                                {
                                    while ((t = duplicator.next()) != null)
                                    {
                                        String localeText = "";
                                        if (t.termText().indexOf('{') == 0)
                                        {
                                            int end = t.termText().indexOf('}', 1);
                                            if (end != -1)
                                            {
                                                localeText = t.termText().substring(1, end);
                                            }
                                        }
                                        if (localeText.length() > 0)
                                        {
                                            doc.add(new Field(attributeName + "." + localeText + ".sort", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                        }
                                        // locale free identifiers are in the default field

                                        doc.add(new Field(attributeName, t.termText(), fieldStore, Field.Index.NO_NORMS, Field.TermVector.NO));

                                    }
                                }
                                catch (IOException e)
                                {
                                    // TODO ??
                                }

                                break;
                            case BOTH:
                                builder = new StringBuilder();
                                builder.append("\u0000").append(locale.toString()).append("\u0000").append(localeString);
                                doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));

                                // analyse ml text
                                analysisMode = getLuceneConfig().getDefaultMLIndexAnalysisMode();
                                // Do the analysis here
                                vba = new VerbatimAnalyser(false);
                                duplicator = new MLTokenDuplicator(vba.tokenStream(attributeName, new StringReader(localeString)), locale, null, analysisMode);
                                try
                                {
                                    while ((t = duplicator.next()) != null)
                                    {
                                        String localeText = "";
                                        if (t.termText().indexOf('{') == 0)
                                        {
                                            int end = t.termText().indexOf('}', 1);
                                            if (end != -1)
                                            {
                                                localeText = t.termText().substring(1, end);
                                            }
                                        }
                                        if (localeText.length() > 0)
                                        {
                                            doc.add(new Field(attributeName + "." + localeText + ".sort", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                        }
                                        else
                                        {
                                            // no locale
                                            doc.add(new Field(attributeName + ".no_locale", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                        }

                                    }
                                }
                                catch (IOException e)
                                {
                                    // TODO ??
                                }

                                break;
                            }
                        }
                    }
                    else if (isText)
                    {
                        // Temporary special case for uids and gids
                        if (propertyName.equals(ContentModel.PROP_USER_USERNAME)
                                || propertyName.equals(ContentModel.PROP_USERNAME) || propertyName.equals(ContentModel.PROP_AUTHORITY_NAME))
                        {
                            doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
                        }

                        // TODO: Use the node locale in preferanced to the system locale
                        Locale locale = null;

                        Serializable localeProperty = nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
                        if (localeProperty != null)
                        {
                            locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeProperty);
                        }

                        if (locale == null)
                        {
                            locale = I18NUtil.getLocale();
                        }

                        StringBuilder builder;
                        MLAnalysisMode analysisMode;
                        VerbatimAnalyser vba;
                        MLTokenDuplicator duplicator;
                        Token t;
                        switch (tokenise)
                        {
                        default:
                        case TRUE:
                            builder = new StringBuilder();
                            builder.append("\u0000").append(locale.toString()).append("\u0000").append(strValue);
                            doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));
                            break;
                        case FALSE:
                            analysisMode = getLuceneConfig().getDefaultMLIndexAnalysisMode();
                            // Do the analysis here
                            vba = new VerbatimAnalyser(false);
                            duplicator = new MLTokenDuplicator(vba.tokenStream(attributeName, new StringReader(strValue)), locale, null, analysisMode);
                            try
                            {
                                while ((t = duplicator.next()) != null)
                                {
                                    String localeText = "";
                                    if (t.termText().indexOf('{') == 0)
                                    {
                                        int end = t.termText().indexOf('}', 1);
                                        if (end != -1)
                                        {
                                            localeText = t.termText().substring(1, end);
                                        }
                                    }
                                    if (localeText.length() > 0)
                                    {
                                        doc.add(new Field(attributeName + "." + localeText + ".sort", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                    }
                                    doc.add(new Field(attributeName, t.termText(), fieldStore, Field.Index.NO_NORMS, Field.TermVector.NO));
                                }
                            }
                            catch (IOException e)
                            {
                                // TODO ??
                            }

                            break;
                        case BOTH:
                            builder = new StringBuilder();
                            builder.append("\u0000").append(locale.toString()).append("\u0000").append(strValue);
                            doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));

                            analysisMode = getLuceneConfig().getDefaultMLIndexAnalysisMode();
                            // Do the analysis here
                            vba = new VerbatimAnalyser(false);
                            duplicator = new MLTokenDuplicator(vba.tokenStream(attributeName, new StringReader(strValue)), locale, null, analysisMode);
                            try
                            {
                                while ((t = duplicator.next()) != null)
                                {
                                    String localeText = "";
                                    if (t.termText().indexOf('{') == 0)
                                    {
                                        int end = t.termText().indexOf('}', 1);
                                        if (end != -1)
                                        {
                                            localeText = t.termText().substring(1, end);
                                        }
                                        else
                                        {

                                        }
                                    }

                                    // localised sort support
                                    if (localeText.length() > 0)
                                    {
                                        doc.add(new Field(attributeName + "." + localeText + ".sort", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                    }
                                    else
                                    {
                                        // All identifiers for cross language search as supported by false
                                        doc.add(new Field(attributeName + ".no_locale", t.termText(), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                // TODO ??
                            }
                            break;
                        }
                    }
                    else if (isDateTime)
                    {
                        SimpleDateFormat df;
                        Date date;
                        switch (tokenise)
                        {
                        default:
                        case TRUE:
                            doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
                            break;
                        case FALSE:
                            df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
                            try
                            {
                                date = df.parse(strValue);
                                doc.add(new Field(attributeName, df.format(date), fieldStore, Field.Index.NO_NORMS, Field.TermVector.NO));
                            }
                            catch (ParseException e)
                            {
                                // ignore for ordering
                            }
                            break;
                        case BOTH:
                            doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));

                            df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
                            try
                            {
                                date = df.parse(strValue);
                                doc.add(new Field(attributeName + ".sort", df.format(date), Field.Store.NO, Field.Index.NO_NORMS, Field.TermVector.NO));
                            }
                            catch (ParseException e)
                            {
                                // ignore for ordering
                            }
                            break;
                        }
                    }
                    else
                    {
                        doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
                    }
                }
            }
        }

        return wereAllAtomic;
    }

    /**
     * Does the node type or any applied aspect allow this node to have child associations?
     * 
     * @param nodeRef
     * @return true if the node may have children
     */
    private boolean mayHaveChildren(NodeRef nodeRef)
    {
        // 1) Does the type support children?
        QName nodeTypeRef = nodeService.getType(nodeRef);
        TypeDefinition nodeTypeDef = getDictionaryService().getType(nodeTypeRef);
        if ((nodeTypeDef != null) && (nodeTypeDef.getChildAssociations().size() > 0))
        {
            return true;
        }
        // 2) Do any of the applied aspects support children?
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            AspectDefinition aspectDef = getDictionaryService().getAspect(aspect);
            if ((aspectDef != null) && (aspectDef.getChildAssociations().size() > 0))
            {
                return true;
            }
        }
        return false;
    }

    private ArrayList<NodeRef> getParents(Path path)
    {
        ArrayList<NodeRef> parentsInDepthOrderStartingWithSelf = new ArrayList<NodeRef>(8);
        for (Iterator<Path.Element> elit = path.iterator(); elit.hasNext(); /**/)
        {
            Path.Element element = elit.next();
            if (!(element instanceof Path.ChildAssocElement))
            {
                throw new IndexerException("Confused path: " + path);
            }
            Path.ChildAssocElement cae = (Path.ChildAssocElement) element;
            parentsInDepthOrderStartingWithSelf.add(0, tenantService.getName(cae.getRef().getChildRef()));

        }
        return parentsInDepthOrderStartingWithSelf;
    }

    private Collection<Pair<ChildAssociationRef, QName>> getAllParents(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        List<Pair<ChildAssociationRef, QName>> allParents = new LinkedList<Pair<ChildAssociationRef, QName>>();
        // First get the real parents
        StoreRef storeRef = nodeRef.getStoreRef();
        Set<NodeRef> allRootNodes = nodeService.getAllRootNodes(storeRef);
        for (ChildAssociationRef assocRef : nodeService.getParentAssocs(nodeRef))
        {
            allParents.add(new Pair<ChildAssociationRef, QName>(assocRef, null));            

            // Add a fake association to the store root if a real parent is a 'fake' root
            NodeRef parentRef = tenantService.getBaseName(assocRef.getParentRef());
            if (allRootNodes.contains(parentRef))
            {
                NodeRef rootNodeRef = nodeService.getRootNode(parentRef.getStoreRef());
                if (!parentRef.equals(rootNodeRef))
                {
                    allParents.add(new Pair<ChildAssociationRef, QName>(new ChildAssociationRef(
                            assocRef.getTypeQName(), rootNodeRef, assocRef.getQName(), nodeRef), null));
                }
            }
        }

        // Now add the 'fake' parents, including their aspect QName
        for (QName classRef : nodeService.getAspects(nodeRef))
        {
            AspectDefinition aspDef = getDictionaryService().getAspect(classRef);
            if (isCategorised(aspDef))
            {
                for (PropertyDefinition propDef : aspDef.getProperties().values())
                {
                    if (propDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                    {
                        for (NodeRef catRef : DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, properties.get(propDef.getName())))
                        {
                            if (catRef != null)
                            {
                                // can be running in context of System user, hence use input nodeRef
                                catRef = tenantService.getName(nodeRef, catRef);

                                try
                                {
                                    for (ChildAssociationRef assocRef : nodeService.getParentAssocs(catRef))
                                    {
                                        allParents
                                                .add(new Pair<ChildAssociationRef, QName>(new ChildAssociationRef(assocRef.getTypeQName(), assocRef.getChildRef(), QName.createQName("member"), nodeRef), aspDef.getName()));
                                    }
                                }
                                catch (InvalidNodeRefException e)
                                {
                                    // If the category does not exists we move on the next
                                }

                            }
                        }
                    }
                }
            }
        }
        return allParents;
    }

    private boolean isCategorised(AspectDefinition aspDef)
    {
        if(aspDef == null)
        {
            return false;
        }
        AspectDefinition current = aspDef;
        while (current != null)
        {
            if (current.getName().equals(ContentModel.ASPECT_CLASSIFIABLE))
            {
                return true;
            }
            else
            {
                QName parentName = current.getParentName();
                if (parentName == null)
                {
                    break;
                }
                current = getDictionaryService().getAspect(parentName);
            }
        }
        return false;
    }

    private boolean isCategory(TypeDefinition typeDef)
    {
        if (typeDef == null)
        {
            return false;
        }
        TypeDefinition current = typeDef;
        while (current != null)
        {
            if (current.getName().equals(ContentModel.TYPE_CATEGORY))
            {
                return true;
            }
            else
            {
                QName parentName = current.getParentName();
                if (parentName == null)
                {
                    break;
                }
                current = getDictionaryService().getType(parentName);
            }
        }
        return false;
    }

    public int updateFullTextSearch(int size) throws LuceneIndexException
    {
        if(getLuceneConfig().isContentIndexingEnabled() == false)
        {
            return 0;
        }
        
        checkAbleToDoWork(IndexUpdateStatus.ASYNCHRONOUS);
        // if (!mainIndexExists())
        // {
        // remainingCount = size;
        // return;
        // }
        try
        {
            toFTSIndex = new LinkedHashMap<String, Deque<Helper>>(size * 2);
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new TermQuery(new Term("FTSSTATUS", "Dirty")), Occur.SHOULD);
            booleanQuery.add(new TermQuery(new Term("FTSSTATUS", "New")), Occur.SHOULD);

            int count = 0;
            Searcher searcher = null;
            try
            {
                searcher = getSearcher(null);
                // commit on another thread - appears like there is no index ...try later
                if (searcher == null)
                {
                    remainingCount = size;
                    return 0;
                }
                Hits hits;
                try
                {
                    hits = searcher.search(booleanQuery);
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Failed to execute query to find content which needs updating in the index", e);
                }

                for (int i = 0; i < hits.length(); i++)
                {
                    Document doc = hits.doc(i);

                    // For backward compatibility with existing indexes, cope with FTSSTATUS being stored directly on
                    // the real document without an FTSREF
                    Field ftsRef = doc.getField("FTSREF");
                    String id = doc.getField("ID").stringValue();
                    String ref = ftsRef == null ? id : ftsRef.stringValue();
                    Helper helper = new Helper(id, doc.getField("TX").stringValue());
                    Deque<Helper> helpers = toFTSIndex.get(ref);
                    if (helpers == null)
                    {
                        helpers = new LinkedList<Helper>();
                        toFTSIndex.put(ref, helpers);
                        count++;
                    }
                    helpers.add(helper);
                    if (count >= size)
                    {
                        break;
                    }
                }

                count = hits.length();
            }
            finally
            {
                if (searcher != null)
                {
                    try
                    {
                        searcher.close();
                    }
                    catch (IOException e)
                    {
                        throw new LuceneIndexException("Failed to close searcher", e);
                    }
                }
            }

            if (toFTSIndex.size() > 0)
            {
                checkAbleToDoWork(IndexUpdateStatus.ASYNCHRONOUS);

                IndexWriter writer = null;
                try
                {
                    writer = getDeltaWriter();
                    int done = 0;
                    for (Map.Entry<String, Deque<Helper>> entry : toFTSIndex.entrySet())
                    {
                        // Document document = helper.document;
                        NodeRef ref = new NodeRef(entry.getKey());
                        done += entry.getValue().size();

                        List<Document> docs;
                        try
                        {
                            docs = readDocuments(ref.toString(), FTSStatus.Clean, true, false, false, null, null, null, null);
                        }
                        catch (Throwable t)
                        {
                            // Try to recover from failure
                            s_logger.error("FTS index of " + ref + " failed. Reindexing without FTS", t);
                            docs = readDocuments(ref.toString(), FTSStatus.Clean, false, false, false, null, null, null, null);
                        }
                        for (Document doc : docs)
                        {
                            try
                            {
                                writer.addDocument(doc /*
                                                        * TODO: Select the language based analyser
                                                        */);
                            }
                            catch (IOException e)
                            {
                                throw new LuceneIndexException("Failed to add document while updating fts index", e);
                            }
                        }

                        if (writer.docCount() > size)
                        {
                            break;
                        }
                    }

                    remainingCount = count - done;
                    return done;
                }
                finally
                {
                    if (writer != null)
                    {
                        closeDeltaWriter();
                    }
                }
            }
            else
            {
                return 0;
            }
        }
        catch (IOException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException("Failed FTS update", e);
        }
        catch (LuceneIndexException e)
        {
            setRollbackOnly();
            throw new LuceneIndexException("Failed FTS update", e);
        }
    }

    protected List<Document> readDocuments(final String stringNodeRef, final FTSStatus ftsStatus,
            final boolean indexAllProperties, final boolean includeDirectoryDocuments, final boolean cascade,
            final Set<Path> pathsProcessedSinceFlush,
            final Map<NodeRef, List<ChildAssociationRef>> childAssociationsSinceFlush, final IndexReader deltaReader,
            final IndexReader mainReader)
    {
        return doInReadthroughTransaction(new RetryingTransactionCallback<List<Document>>()
        {
            @Override
            public List<Document> execute() throws Throwable
            {
                return createDocuments(stringNodeRef, ftsStatus, indexAllProperties, includeDirectoryDocuments,
                        cascade, pathsProcessedSinceFlush, childAssociationsSinceFlush, deltaReader, mainReader);
            }
        });
    }
    
    public void registerCallBack(FTSIndexerAware callBack)
    {
        this.callBack = callBack;
    }

    private static class Helper
    {
        String id;

        String tx;

        Helper(String id, String tx)
        {
            this.id = id;
            this.tx = tx;
        }
    }

    FullTextSearchIndexer fullTextSearchIndexer;

    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    protected void doPrepare() throws IOException
    {
        flushPending();
        // prepareToMergeIntoMain();
    }

    protected void doCommit() throws IOException
    {
        if (indexUpdateStatus == IndexUpdateStatus.ASYNCHRONOUS)
        {
            doFTSIndexCommit();
            // FTS does not trigger indexing request
        }
        else
        {
            // Deletions delete nodes only. Containers handled separately
            setInfo(docs, getDeletions(), getContainerDeletions(), getDeleteOnlyNodes());
            fullTextSearchIndexer.requiresIndex(store);
        }
        if (callBack != null)
        {
            callBack.indexCompleted(store, remainingCount, null);
        }
    }

    protected void doRollBack() throws IOException
    {
        if (callBack != null)
        {
            callBack.indexCompleted(store, 0, null);
        }
    }

    protected void doSetRollbackOnly() throws IOException
    {

    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.Indexer#deleteIndex(org.alfresco.service.cmr.repository.StoreRef)
     */
    public void deleteIndex(StoreRef storeRef)
    {
        deleteIndex();
    }

}
