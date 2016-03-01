 
package org.alfresco.module.org_alfresco_module_rm.patch.v20;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.compatibility.ModulePatchComponent;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 Saved Search Patch
 *
 * @author Roy Wetherall
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class RMv2SavedSearchPatch extends ModulePatchComponent
                                  implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** RM site id */
    private static final String RM_SITE_ID = "rm";

    /** Records management search service */
    private RecordsManagementSearchService recordsManagementSearchService;

    /** Site service */
    private SiteService siteService;

    /** Content service */
    private ContentService contentService;

    /**
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }

    /**
     * @param siteService   site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch()
    {
        if (siteService.getSite(RM_SITE_ID) != null)
        {
            // get the saved searches
            List<SavedSearchDetails> savedSearches = recordsManagementSearchService.getSavedSearches(RM_SITE_ID);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("  ... updating " + savedSearches.size() + " saved searches");
            }

            for (SavedSearchDetails savedSearchDetails : savedSearches)
            {
                // refresh the query
                String refreshedJSON = savedSearchDetails.toJSONString();
                NodeRef nodeRef = savedSearchDetails.getNodeRef();

                if (nodeRef != null)
                {
                    ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                    writer.putContent(refreshedJSON);


                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("    ... updated saved search " + savedSearchDetails.getName() + " (nodeRef=" + nodeRef.toString() + ")");
                    }
                }
            }
        }
    }
}
