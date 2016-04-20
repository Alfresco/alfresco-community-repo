
package org.alfresco.repo.rating;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/*
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingSchemeImpl implements RatingScheme, BeanNameAware, InitializingBean
{
    private final RatingSchemeRegistry ratingSchemeRegistry;
    
    private String name;
    private float minRating, maxRating;
    
    private List<AbstractRatingRollupAlgorithm> propertyRollups = Collections.emptyList();
    
    /**
     * Is the cm:creator of the content node allowed to apply ratings to it?
     * <code>true</code> if yes, else <code>false</code>.
     */
    private boolean selfRatingAllowed;
    
    /**
     * This property is used to determine where in the Alfresco content model the ratings rollup aspect should go.
     * If it is not injected, a default value of "cm" for the Alfresco content model is used.
     * Individual rating schemes can provide their own namespace prefixes.
     * @since 4.1.5
     * @see RatingNamingConventionsUtil
     * @see RatingsRelatedAspectBehaviours#getAspectsNotToCopy() to prevent aspect copying.
     */
    private String modelPrefix = "cm";
    
    public RatingSchemeImpl(RatingSchemeRegistry registry)
    {
        this.ratingSchemeRegistry = registry;
    }
    
    public void setPropertyRollups(List<AbstractRatingRollupAlgorithm> rollupAlgorithms)
    {
        this.propertyRollups = rollupAlgorithms;
    }
    
    public void setModelPrefix(String prefix)
    {
        this.modelPrefix = prefix;
    }
    
    public void init()
    {
        ratingSchemeRegistry.register(this.name, this);
    }
    
    public List<AbstractRatingRollupAlgorithm> getPropertyRollups()
    {
        return Collections.unmodifiableList(this.propertyRollups);
    }
    
    public String getModelPrefix()
    {
        return this.modelPrefix;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    public void setMinRating(float minRating)
    {
        this.minRating = minRating;
    }

    public void setMaxRating(float maxRating)
    {
        this.maxRating = maxRating;
    }
    
    public void setSelfRatingAllowed(boolean selfRatingAllowed)
    {
        this.selfRatingAllowed = selfRatingAllowed;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (this.minRating > this.maxRating)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Illegal rating limits for ").append(name)
               .append(". Min > Max. ")
               .append(minRating).append(" > ").append(maxRating);
            throw new RatingServiceException(msg.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getMaxRating()
     */
    public float getMaxRating()
    {
        return this.maxRating;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getMinRating()
     */
    public float getMinRating()
    {
        return this.minRating;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#isSelfRatingAllowed()
     */
    public boolean isSelfRatingAllowed()
    {
        return this.selfRatingAllowed;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getName()
     */
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
           .append(" ").append(this.name)
           .append(" [").append(this.minRating)
           .append("..").append(this.maxRating)
           .append("]");
        
        // Injected rollups.
        msg.append(" <");
        for (Iterator<AbstractRatingRollupAlgorithm> iter = propertyRollups.iterator(); iter.hasNext(); )
        {
            AbstractRatingRollupAlgorithm nextRollup = iter.next();
            msg.append(nextRollup.getRollupName());
            if (iter.hasNext())
            {
                msg.append(", ");
            }
        }
        msg.append(">");
        
        return msg.toString();
    }
    
    /**
     * This method can be used to sort RatingSchemes by name.
     * @since 4.1.5
     */
    @Override public int compareTo(RatingScheme otherScheme) { return this.name.compareTo(otherScheme.getName()); }
}
