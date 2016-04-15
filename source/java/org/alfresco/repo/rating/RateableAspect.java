package org.alfresco.repo.rating;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rateable aspect behaviour bean. When any node with the rateable aspect is
 * copied, then ensure the ratings and roll ups are not copied.
 * 
 * @author Alex Miller
 */
public class RateableAspect implements CopyServicePolicies.OnCopyNodePolicy
{
    /** logger */
    private static final Log logger = LogFactory.getLog(RateableAspect.class);

    /** Services */
    private PolicyComponent policyComponent;

    private RatingNamingConventionsUtil ratingNamingConventions;

    private RatingSchemeRegistry ratingSchemeRegistry;

    /**
     * Set the policy component
     * 
     * @param policyComponent policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the rating scheme registry
     * 
     * @param ratingSchemeRegistry The rating scheme registry
     */
    public void setRatingSchemeRegistry(RatingSchemeRegistry ratingSchemeRegistry)
    {
        this.ratingSchemeRegistry = ratingSchemeRegistry;
    }

    /**
     * Set the rating naming conventions service.
     * 
     * @param ratingNamingConventions RatingNamingConventionsUtil
     */
    public void setRatingNamingConventions(RatingNamingConventionsUtil ratingNamingConventions)
    {
        this.ratingNamingConventions = ratingNamingConventions;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // Prevent the ratebale aspect from being copied
        bindNoCopyBehaviour(ContentModel.ASPECT_RATEABLE);

        // Prevent the roll up aspects from being copied
        for (RatingScheme ratingScheme : ratingSchemeRegistry.getRatingSchemes().values())
        {
            if (ratingScheme.getPropertyRollups() != null && ratingScheme.getPropertyRollups().size() > 0) 
            {
                QName rollupAspectName = ratingNamingConventions.getRollupAspectNameFor(ratingScheme);
                bindNoCopyBehaviour(rollupAspectName);
            }
        }
    }

    private void bindNoCopyBehaviour(QName rollupAspectName)
    {
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                rollupAspectName, new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * @return Returns CopyBehaviourCallback
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }
}
