package org.alfresco.module.org_alfresco_module_rm.vital;

import java.util.Date;

import org.alfresco.service.cmr.repository.Period;

/**
 * Vital record definition interface
 * 
 * @author Roy Wetherall
 */
public interface VitalRecordDefinition
{   
    /**
     * Indicates whether the vital record definition is enabled or not.
     * <p>
     * Note:  a result of false indicates that the vital record definition is inactive
     * therefore does not impose the rules associated with vital record review on 
     * associated nodes.
     * 
     * @return  boolean true if enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Review period for vital records
     * 
     * @return Period   review period
     */
    Period getReviewPeriod();
    
    /**
     * Gets the next review date based on the review period
     * 
     * @return Date date of the next review
     */
    Date getNextReviewDate();
}
