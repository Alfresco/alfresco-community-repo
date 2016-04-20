
package org.alfresco.repo.virtual;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Implementation of the {@link AlfrescoAPIFacet} interface that provides access
 * to Alfresco services at the level of the Java API, through its
 * {@link ServiceRegistry} instance.
 *
 * @author Bogdan Horje
 */
public class PublicAPIFacet implements AlfrescoAPIFacet
{
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ScriptService getScriptService()
    {
        return serviceRegistry.getScriptService();
    }

    @Override
    public NodeService getNodeService()
    {
        return serviceRegistry.getNodeService();
    }

    @Override
    public ContentService getContentService()
    {
        return serviceRegistry.getContentService();
    }

    @Override
    public SearchService getSearchService()
    {
        return serviceRegistry.getSearchService();
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        return serviceRegistry.getDictionaryService();
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        return serviceRegistry.getFileFolderService();
    }

    @Override
    public PermissionService getPermissionService()
    {
        return serviceRegistry.getPermissionService();
    }
}
