
package org.alfresco.repo.rating;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class provides the basic implementation of a rating property rollup.
 * By providing an implementation of this class (or reusing an existing one),
 * injecting the object into the {@link org.alfresco.service.cmr.rating.RatingScheme} and following the content
 * model naming conventions, it
 * should be possible to have new rating property rollups automatically calculated
 * and persisted into the Alfresco content model, thereby enabling indexing, searching
 * and sorting of rating-related properties.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
@AlfrescoPublicApi
public abstract class AbstractRatingRollupAlgorithm implements InitializingBean
{
    protected String ratingSchemeName;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected RatingServiceImpl ratingServiceImpl;
    
    protected final String rollupName;
    
    public AbstractRatingRollupAlgorithm(String rollupName)
    {
        this.rollupName = rollupName;
    }
    
    public void setRatingSchemeName(String ratingScheme)
    {
        this.ratingSchemeName = ratingScheme;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setRatingService(RatingService ratingService)
    {
        this.ratingServiceImpl = (RatingServiceImpl)ratingService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (ratingSchemeName == null)
        {
            throw new RatingServiceException("Illegal null ratingSchemeName in " + this.getClass().getSimpleName());
        }
    }
    
    public abstract Serializable recalculate(NodeRef ratedNode);
    
    /**
     * This method returns the rollup name, for example "Total" or "Count".
     * This rollup name is used as part of the convention-based naming of content model
     * property names.
     * @return String
     */
    public String getRollupName()
    {
        return this.rollupName;
    }
}
