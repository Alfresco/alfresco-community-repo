 
package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 * Recordable version config constraint
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class VersionParameterConstraint extends BaseParameterConstraint
{
    /**
     * @see org.alfresco.repo.action.constraint.BaseParameterConstraint#getAllowableValuesImpl()
     */
    @Override
    protected Map<String, String> getAllowableValuesImpl()
    {
        RecordableVersionPolicy[] recordableVersionPolicies = RecordableVersionPolicy.values();
        Map<String, String> allowableValues = new HashMap<String, String>(recordableVersionPolicies.length);
        for (RecordableVersionPolicy recordableVersionPolicy : recordableVersionPolicies)
        {
            String policy = recordableVersionPolicy.toString();
            allowableValues.put(policy, getI18NLabel(policy));
        }
        return allowableValues;
    }
}
