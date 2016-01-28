/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

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
