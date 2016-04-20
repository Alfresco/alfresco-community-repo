
package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.rating.traitextender.RatingServiceExtension;
import org.alfresco.repo.rating.traitextender.RatingServiceTrait;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualRatingServiceExtension extends SpringBeanExtension<RatingServiceExtension, RatingServiceTrait>
            implements RatingServiceExtension
{

    private VirtualStore smartStore;

    public VirtualRatingServiceExtension()
    {
        super(RatingServiceTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

  

    public void applyRating(NodeRef targetNode, float rating, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        getTrait().applyRating(materialNode,
                               rating,
                               ratingSchemeName);
    }

    public int getRatingsCount(NodeRef targetNode, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getRatingsCount(materialNode,
                                          ratingSchemeName);
    }

    public float getTotalRating(NodeRef targetNode, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getTotalRating(materialNode,
                                         ratingSchemeName);
    }

    public float getAverageRating(NodeRef targetNode, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getAverageRating(materialNode,
                                           ratingSchemeName);
    }

    public Rating getRatingByCurrentUser(NodeRef targetNode, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getRatingByCurrentUser(materialNode,
                                                 ratingSchemeName);
    }

    public List<Rating> getRatingsByCurrentUser(NodeRef targetNode)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getRatingsByCurrentUser(materialNode);
    }

    public Rating removeRatingByCurrentUser(NodeRef targetNode, String ratingSchemeName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().removeRatingByCurrentUser(materialNode,
                                                    ratingSchemeName);
    }

    public Serializable getRatingRollup(NodeRef targetNode, String ratingSchemeName, String ratingRollupName)
    {
        NodeRef materialNode = smartStore.materializeIfPossible(targetNode);
        return getTrait().getRatingRollup(materialNode,
                                          ratingSchemeName,
                                          ratingRollupName);
    }

    @Override
    public Map<String, RatingScheme> getRatingSchemes()
    {
        return getTrait().getRatingSchemes();
    }

    @Override
    public RatingScheme getRatingScheme(String ratingSchemeName)
    {
        return getTrait().getRatingScheme(ratingSchemeName);
    }

}
