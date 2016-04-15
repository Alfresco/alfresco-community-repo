
package org.alfresco.repo.virtual;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Implementation of the {@link AlfrescoAPIFacet} interface that provides access
 * to "core" Alfresco services, i.e. services in a lower abstraction layer than
 * the ones provided in the Java API, by the {@link ServiceRegistry}.
 *
 * @author Bogdan Horje
 */
public class CoreAPIFacet implements AlfrescoAPIFacet
{

    private ScriptService scriptService;

    private NodeService nodeService;

    private ContentService contentService;

    private SearchService searchService;

    private DictionaryService dictionaryService;

    private FileFolderService fileFolderService;

    private PermissionService permissionService;

    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    @Override
    public ScriptService getScriptService()
    {
        return scriptService;
    }

    @Override
    public NodeService getNodeService()
    {
        return nodeService;
    }

    @Override
    public ContentService getContentService()
    {
        return contentService;
    }

    @Override
    public SearchService getSearchService()
    {
        return searchService;
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    @Override
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

}
