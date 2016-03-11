package org.alfresco.module.org_alfresco_module_rm.vital;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;

/**
 * Vital record definition implementation class
 * 
 * @author Roy Wetherall
 */
public class VitalRecordDefinitionImpl implements VitalRecordDefinition, RecordsManagementModel
{
    /** Indicates whether the vital record definition is enabled or not */
    private boolean enabled = false;
    
    /** Vital record review period */
    private Period reviewPeriod = new Period("none|0");
    
    /**
     * Constructor.
     * 
     * @param enabled     
     * @param reviewPeriod
     */
    /* package */ VitalRecordDefinitionImpl(boolean enabled, Period reviewPeriod)
    {
        this.enabled = enabled;
        if (reviewPeriod != null)
        {
            this.reviewPeriod = reviewPeriod;
        }
    }
    
    /**
     * Helper method to create vital record definition from node reference.
     * 
     * @param nodeService
     * @param nodeRef
     * @return
     */
    /* package */ static VitalRecordDefinition create(NodeService nodeService, NodeRef nodeRef)
    {
        Boolean enabled = (Boolean)nodeService.getProperty(nodeRef, PROP_VITAL_RECORD_INDICATOR);
        if (enabled == null)
        {
            enabled = Boolean.FALSE;
        }
        Period reviewPeriod = (Period)nodeService.getProperty(nodeRef, PROP_REVIEW_PERIOD);
        return new VitalRecordDefinitionImpl(enabled, reviewPeriod);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#getNextReviewDate()
     */
    public Date getNextReviewDate()
    {
        return getReviewPeriod().getNextDate(new Date());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#getReviewPeriod()
     */
    public Period getReviewPeriod()
    {
        return reviewPeriod;
    }
}
