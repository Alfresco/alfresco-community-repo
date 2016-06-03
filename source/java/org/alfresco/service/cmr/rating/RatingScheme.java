
package org.alfresco.service.cmr.rating;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.rating.AbstractRatingRollupAlgorithm;
import org.alfresco.repo.rating.RatingNamingConventionsUtil;
import org.alfresco.repo.rating.RatingSchemeRegistry;

/**
 * This interface defines a Rating Scheme, which is a named scheme for user-supplied
 * ratings with a defined minimum value and a defined maximum value. The minimum must
 * not be greater than the maximum but the two values can be equal.
 * These schemes are defined within spring context files and injected into the
 * {@link RatingSchemeRegistry} at startup.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
@AlfrescoPublicApi
public interface RatingScheme extends Comparable<RatingScheme>
{
    /**
     * This method returns the name which uniquely identifies the rating scheme.
     * 
     * @return the name.
     */
    public String getName();

    /**
     * This method returns the minimum rating defined for this scheme.
     * 
     * @return the minimum rating.
     */
    public float getMinRating();

    /**
     * This method returns the maximum rating defined for this scheme.
     * 
     * @return the maximum rating.
     */
    public float getMaxRating();
    
    /**
     * This method returns the namespace (prefix e.g. "cm") of the Alfresco content model
     * containing the definitions of the rollup aspect and properties.
     * @since 4.1.5
     * @see RatingNamingConventionsUtil
     */
    public String getModelPrefix();
    
    /**
     * This method returns <code>true</code> if the cm:creator of the node is allowed
     * to apply a rating to it, else <code>false</code>.
     * 
     * @return whether or not the cm:creator of the node can apply a rating in this scheme.
     */
    public boolean isSelfRatingAllowed();
    
    /**
     * This method returns a List of {@link AbstractRatingRollupAlgorithm property rollup algorithms}
     * which are used in order to calculate rating totals, counts etc for a rated node.
     * 
     * @return an unmodifiable list of property rollup algorithms.
     * @since 3.5
     */
    public List<AbstractRatingRollupAlgorithm> getPropertyRollups();

}
