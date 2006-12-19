/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Component to perform a bootstrap check of the alignment of the
 * database, Lucene indexes and content store.
 * <p>
 * The algorithm is:
 * <ul>
 *   <li>Get all stores from the NodeService</li>
 *   <li>Get each root node</li>
 *   <li>Perform a Lucene query for each root node</li>
 *   <li>Query Lucene for a small set of content nodes</li>
 *   <li>Get content readers for each node</li>
 * </ul>
 * If any of the steps fail then the bootstrap bean will fail, except if
 * the indexes are marked for full recovery.  In this case, the Lucene
 * checks are not required as the indexes will be due for a rebuild.  
 * 
 * @author Derek Hulley
 */
public class ConfigurationChecker extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(ConfigurationChecker.class);
    
    private static final String WARN_RELATIVE_DIR_ROOT = "system.config_check.warn.dir_root";
    private static final String MSG_DIR_ROOT = "system.config_check.msg.dir_root";
    private static final String ERR_DUPLICATE_ROOT_NODE = "system.config_check.err.indexes.duplicate_root_node";
    private static final String ERR_MISSING_INDEXES = "system.config_check.err.missing_index";
    private static final String ERR_MISSING_CONTENT = "system.config_check.err.missing_content";
    private static final String ERR_FIX_DIR_ROOT = "system.config_check.err.fix_dir_root";
    private static final String MSG_HOWTO_INDEX_RECOVER = "system.config_check.msg.howto_index_recover";
    private static final String WARN_STARTING_WITH_ERRORS = "system.config_check.warn.starting_with_errors";

    private boolean strict;
    private RecoveryMode indexRecoveryMode;
    private String dirRoot;
    private boolean checkAllContent;

    private AuthenticationComponent authenticationComponent;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    
    public ConfigurationChecker()
    {
        this.checkAllContent = false;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("ConfigurationChecker")
          .append("[indexRecoveryMode=").append(indexRecoveryMode)
          .append(", checkAllContent=").append(checkAllContent)
          .append("]");
        return sb.toString();
    }

    /**
     * This flag controls the behaviour of the component in the event of problems being found.
     * Generally, the system should be <b>strict</b>, but this can be changed if indexes are
     * going to be recovered, or if missing content is acceptable.
     * 
     * @param strict <code>true</code> to prevent system startup if problems are found, otherwise
     *      <code>false</code> to allow the system to startup regardless.
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

    /**
     * @param checkAllContent <code>true</code> to get all content URLs when checking for
     *      missing content, or <code>false</code> to just do a quick sanity check against
     *      the content store.
     */
    public void setCheckAllContent(boolean checkAllContent)
    {
        this.checkAllContent = checkAllContent;
    }

    /**
     * Set the index recovery mode.  If this is
     * {@link org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode#VALIDATE FULL}
     * then the index checks are ignored as the indexes will be scheduled for a rebuild
     * anyway.
     * 
     * @see org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode
     */
    public void setIndexRecoveryMode(String indexRecoveryMode)
    {
        this.indexRecoveryMode = RecoveryMode.valueOf(indexRecoveryMode);
    }

    public void setDirRoot(String dirRoot)
    {
        this.dirRoot = dirRoot;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // authenticate
        try
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            check();
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    
    /**
     * Performs the check work.
     */
    private void check()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting bootstrap configuration check: " + this);
        }
        
        // check the dir.root
        boolean isRelativeRoot = dirRoot.startsWith(".");
        if (isRelativeRoot)
        {
            String msg = I18NUtil.getMessage(WARN_RELATIVE_DIR_ROOT, dirRoot);
            logger.warn(msg);
        }
        File dirRootFile = new File(dirRoot);
        String msgDirRoot = I18NUtil.getMessage(MSG_DIR_ROOT, dirRootFile);
        logger.info(msgDirRoot);

        // get all root nodes from the NodeService, i.e. database
        List<StoreRef> storeRefs = nodeService.getStores();
        List<StoreRef> missingIndexStoreRefs = new ArrayList<StoreRef>(0);
        List<StoreRef> missingContentStoreRefs = new ArrayList<StoreRef>(0);
        for (StoreRef storeRef : storeRefs)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            if (indexRecoveryMode != RecoveryMode.FULL)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Checking index for store: " + storeRef);
                }
                
                // perform a Lucene query for the root node
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery("ID:" + LuceneQueryParser.escape(rootNodeRef.toString()));
                
                ResultSet results = null;
                int size = 0;
                try
                {
                    results = searchService.query(sp);
                    size = results.length();
                }
                finally
                {
                    try { results.close(); } catch (Throwable e) {}
                }
                
                if (size == 0)
                {
                    // indexes missing for root node
                    missingIndexStoreRefs.add(storeRef);
                    // debug
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Index missing for store: \n" +
                                "   store: " + storeRef);
                    }
                }
                else if (size > 1)
                {
                    // there are duplicates
                    String msg = I18NUtil.getMessage(ERR_DUPLICATE_ROOT_NODE, storeRef);
                    throw new AlfrescoRuntimeException(msg);
                }
            }
            // select a content property
            QName contentPropertyQName = null;
            Collection<QName> typeQNames = dictionaryService.getAllTypes();
            /* BREAK POINT */ contentPropertyFound:
            for (QName typeQName : typeQNames)
            {
                TypeDefinition classDef = dictionaryService.getType(typeQName);
                Map<QName, PropertyDefinition> propertyDefs = classDef.getProperties();
                for (PropertyDefinition propertyDef : propertyDefs.values())
                {
                    if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                    {
                        continue;
                    }
                    contentPropertyQName = propertyDef.getName();
                    break contentPropertyFound;
                }
            }
            // do a search for nodes with content
            if (contentPropertyQName != null)
            {
                String attributeName = "\\@" + LuceneQueryParser.escape(contentPropertyQName.toString());

                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery(attributeName + ":*");
                if (!checkAllContent)
                {
                    sp.setLimit(1);
                    sp.setLimitBy(LimitBy.FINAL_SIZE);
                }
                ResultSet results = null;
                try
                {
                    results = searchService.query(sp);
                    // iterate and attempt to get the content
                    for (ResultSetRow row : results)
                    {
                        NodeRef nodeRef = row.getNodeRef();
                        ContentReader reader = contentService.getReader(nodeRef, contentPropertyQName);
                        if (reader == null)
                        {
                            // content not written
                            continue;
                        }
                        else if (reader.exists())
                        {
                            // the data is present in the content store
                        }
                        else
                        {
                            // URL is missing
                            missingContentStoreRefs.add(storeRef);
                            // debug
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Content missing from store: \n" +
                                        "   store: " + storeRef + "\n" +
                                        "   content: " + reader);
                            }
                        }
                        // break out if necessary
                        if (!checkAllContent)
                        {
                            break;
                        }
                    }
                }
                finally
                {
                    try { results.close(); } catch (Throwable e) {}
                }
            }
        }
        // check for missing indexes
        int missingStoreIndexes = missingIndexStoreRefs.size();
        if (missingStoreIndexes > 0)
        {
            String msg = I18NUtil.getMessage(ERR_MISSING_INDEXES, missingStoreIndexes);
            logger.error(msg);
            String msgRecover = I18NUtil.getMessage(MSG_HOWTO_INDEX_RECOVER);
            logger.info(msgRecover);
        }
        // check for missing content
        int missingStoreContent = missingContentStoreRefs.size();
        if (missingStoreContent > 0)
        {
            String msg = I18NUtil.getMessage(ERR_MISSING_CONTENT, missingStoreContent);
            logger.error(msg);
        }
        // handle either content or indexes missing
        if (missingStoreIndexes > 0 || missingStoreContent > 0)
        {
            String msg = I18NUtil.getMessage(ERR_FIX_DIR_ROOT, dirRootFile);
            logger.error(msg);
            
            // Now determine the failure behaviour
            if (strict)
            {
                throw new AlfrescoRuntimeException(msg);
            }
            else
            {
                String warn = I18NUtil.getMessage(WARN_STARTING_WITH_ERRORS);
                logger.warn(warn);
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing here
    }
}
