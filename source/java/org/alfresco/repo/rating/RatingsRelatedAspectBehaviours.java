package org.alfresco.repo.rating;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Aspect behaviour bean for {@link RatingService}-related aspects.
 * 
 * @author Neil Mc Erlean
 * @since 4.1.5
 */
public class RatingsRelatedAspectBehaviours implements CopyServicePolicies.OnCopyNodePolicy
{
    private PolicyComponent  policyComponent;
    
    public void setPolicyComponent(PolicyComponent policyComponent) { this.policyComponent = policyComponent; }
    
    public void init()
    {
        for (QName aspect : getAspectsNotToCopy())
        {
            this.policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), 
                    aspect,
                    new JavaBehaviour(this, "getCopyCallback"));
        }
    }
    
    /**
     * This method returns the default list of ratings-related aspects which should not be
     * copied when a rated node is copied.
     * 
     * @return a List of QNames of ratings-related aspects which should not be copied.
     */
    protected List<QName> getAspectsNotToCopy()
    {
        return Arrays.asList(new QName[] {ContentModel.ASPECT_RATEABLE, 
                                          ContentModel.ASPECT_LIKES_RATING_SCHEME_ROLLUPS,
                                          ContentModel.ASPECT_FIVESTAR_RATING_SCHEME_ROLLUPS});
    }
    
    /**
     * @return Returns CopyBehaviourCallback
     */
    @Override public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return RatingRelatedAspectsCopyBehaviourCallback.INSTANCE;
    }
    
    /**
     * This class defines a 'do not copy' behaviour for all relevant aspects.
     */
    private static class RatingRelatedAspectsCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new RatingRelatedAspectsCopyBehaviourCallback();
        
        /** Do not copy any of these aspects. */
        @Override public boolean getMustCopy(QName classQName, CopyDetails copyDetails) { return false; }
        
        @Override public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyAssociationDetails assocCopyDetails)
        {
            return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                    AssocCopySourceAction.IGNORE,
                    AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
        }
        
        @Override public ChildAssocCopyAction getChildAssociationCopyAction(QName classQName,
                                                                            CopyDetails copyDetails,
                                                                            CopyChildAssociationDetails childAssocCopyDetails)
        { return ChildAssocCopyAction.IGNORE; }
    }
}
