
package org.alfresco.repo.virtual;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Dependency inversion facade of the Alfresco services. It provides access to
 * the implementations of Alfresco services.
 * 
 * @author Bogdan Horje
 */
public interface AlfrescoAPIFacet
{

    @NotAuditable
    ScriptService getScriptService();

    @NotAuditable
    NodeService getNodeService();

    @NotAuditable
    ContentService getContentService();

    @NotAuditable
    SearchService getSearchService();

    @NotAuditable
    DictionaryService getDictionaryService();

    @NotAuditable
    FileFolderService getFileFolderService();

    @NotAuditable
    PermissionService getPermissionService();

}
