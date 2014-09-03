package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.springframework.extensions.surf.util.I18NUtil;

public class AddTagScopeAspect  extends AbstractPatch
{
    /** The title we give to the batch process in progress messages / JMX. */
    private static final String SUCCESS_MSG = "patch.addTagScopeAspect.result";
    
    /** Services */
    private TaggingService taggingService;
    
    /** Repository object */
    private Repository repository;

    
    /**
     * Sets the tagging service.
     * 
     * @param taggingService
     *            the tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    /**
     * @param repository    repository object
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        taggingService.addTagScope(repository.getCompanyHome());
        return I18NUtil.getMessage(SUCCESS_MSG);
    }
}