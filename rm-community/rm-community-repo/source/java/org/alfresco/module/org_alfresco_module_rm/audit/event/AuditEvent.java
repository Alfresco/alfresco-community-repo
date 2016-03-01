 
package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Class to represent an audit event
 *
 * @author Gavin Cornwell
 * @author Roy Wetherall
 * @since 1.0
 */
public class AuditEvent implements RecordsManagementModel, Comparable<AuditEvent>
{
	/** Name */
    private String name;

    /** Label */
    private String label;

    /** Records management audit service */
    protected RecordsManagementAuditService recordsManagementAuditService;

    /**
     * @param recordsManagementAuditService     records management audit service
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * Init method
     */
    public void init()
    {
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("label", label);

        recordsManagementAuditService.registerAuditEvent(this);
    }

    /**
     * Default constructor.
     */
    public AuditEvent()
    {
        // do nothing
    }

    /**
     * Default constructor.
     *
     * @param name  audit event name
     * @param label audit event label (can be actual label or I18N lookup key)
     */
    public AuditEvent(String name, String label)
    {
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("label", label);

        setName(name);
        setLabel(label);
    }

    /**
     * @return  audit event name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name  audit event name
     */
    public void setName(String name)
    {
		this.name = name;
	}

    /**
     * @return   audit event label
     */
    public String getLabel()
    {
    	String lookup = I18NUtil.getMessage(label);
    	if (StringUtils.isBlank(lookup))
    	{
    		lookup = label;
    	}
    	return lookup;
    }

    /**
     * @param label audit event label
     */
    public void setLabel(String label)
    {
		this.label = label;
	}

    /**
     * Compare by label.
     *
     * @param compare   compare to audit event
     * @return int
     */
    @Override
    public int compareTo(AuditEvent compare)
    {
        return getLabel().compareTo(compare.getLabel());
    }
}
