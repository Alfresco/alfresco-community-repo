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
package org.alfresco.repo.admin.patch.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A patch to update the value of a Mimetype.
 * <p>
 * This patch will only work fully if the content URL data has been fully normalized.
 * It supports renaming to existing, currently-used mimetypes as well as to
 * mimetypes that have not been used before.
 * 
 * @author Derek Hulley
 * @since 3.3 SP1
 */
public class GenericMimetypeRenamePatch extends AbstractPatch
{
    private static final String MSG_START = "patch.genericMimetypeUpdate.start";
    private static final String MSG_UPDATED = "patch.genericMimetypeUpdate.updated";
    private static final String MSG_INDEXED = "patch.genericMimetypeUpdate.indexed";
    private static final String MSG_DONE = "patch.genericMimetypeUpdate.done";
    private static final String MSG_DONE_REINDEX = "patch.genericMimetypeUpdate.doneReindex";

    /* Helper DAOs */
    private IndexerAndSearcher indexerAndSearcher;
    private MimetypeDAO mimetypeDAO;
    private PatchDAO patchDAO;
    
    /** Mimetype mappings */
    private Map<String, String> mimetypeMappings;
    private boolean reindex;
    
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    public void setMimetypeDAO(MimetypeDAO mimetypeDAO)
    {
        this.mimetypeDAO = mimetypeDAO;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setMimetypeMappings(Map<String, String> mimetypeMappings)
    {
        this.mimetypeMappings = mimetypeMappings;
    }

    public void setReindex(boolean reindex)
    {
        this.reindex = reindex;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(indexerAndSearcher, "indexerAndSearcher");
        checkPropertyNotNull(mimetypeDAO, "mimetypeDAO");
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(mimetypeMappings, "mimetypeMappings");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // First get all the available stores that we might want to reindex
        List<StoreRef> storeRefsList = nodeService.getStores();
        Set<StoreRef> storeRefs = new HashSet<StoreRef>();
        for (StoreRef storeRef : storeRefsList)
        {
            // We want workspace://SpacesStore or related MT stores
            if (storeRef.getIdentifier().endsWith("SpacesStore"))
            {
                storeRefs.add(storeRef);
            }
        }
        
        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));
        for (Map.Entry<String, String> element : mimetypeMappings.entrySet())
        {
            String oldMimetype = element.getKey();
            String newMimetype = element.getValue();
            
            // First check if the mimetype is used at all
            Pair<Long, String> oldMimetypePair = mimetypeDAO.getMimetype(oldMimetype);
            if (oldMimetypePair == null)
            {
                // Not used
                continue;
            }
            
            // Check if the new mimetype exists
            Pair<Long, String> newMimetypePair = mimetypeDAO.getMimetype(newMimetype);
            int updateCount = 0;
            if (newMimetypePair == null)
            {
                // Easy, just rename the old one
                updateCount = mimetypeDAO.updateMimetype(oldMimetype, newMimetype);
            }
            else
            {
                // We need to move all the old references to the new ones
                Long oldMimetypeId = oldMimetypePair.getFirst();
                Long newMimetypeId = mimetypeDAO.getOrCreateMimetype(newMimetype).getFirst();
                updateCount = patchDAO.updateContentMimetypeIds(oldMimetypeId, newMimetypeId);
            }
            result.append(I18NUtil.getMessage(MSG_UPDATED, updateCount, oldMimetype, newMimetype));
            if (reindex)
            {
                // Update Lucene
                int reindexCount = 0;
                for (StoreRef storeRef : storeRefs)
                {
                    reindexCount += reindex(oldMimetype, storeRef);
                    result.append(I18NUtil.getMessage(MSG_INDEXED, reindexCount, storeRef));
                }
            }
        }
        // Done
        if (reindex)
        {
            result.append(I18NUtil.getMessage(MSG_DONE));
        }
        else
        {
            result.append(I18NUtil.getMessage(MSG_DONE_REINDEX));
        }

        return result.toString();
    }
    
    private int reindex(String oldMimetype, StoreRef store)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("@" + AbstractLuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) +
                ".mimetype:\"" + oldMimetype + "\"");
        sp.addStore(store);
        Indexer indexer = indexerAndSearcher.getIndexer(store);
        ResultSet rs = null;
        int count = 0;
        try
        {
            rs = searchService.query(sp);
            count = rs.length();
            for (ResultSetRow row : rs)
            {
                indexer.updateNode(row.getNodeRef());
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        return count;
    }
}
