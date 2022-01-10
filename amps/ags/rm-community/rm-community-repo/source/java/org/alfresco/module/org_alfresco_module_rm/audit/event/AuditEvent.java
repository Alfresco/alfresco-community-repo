/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Class to represent an audit event
 *
 * @author Gavin Cornwell
 * @author Roy Wetherall
 * @since 1.0
 */
@AlfrescoPublicApi
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
