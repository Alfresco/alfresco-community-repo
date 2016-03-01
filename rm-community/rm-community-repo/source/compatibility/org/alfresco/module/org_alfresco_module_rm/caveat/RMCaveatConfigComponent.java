 
package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface RMCaveatConfigComponent
{
    void init();

    /**
     * Get allowed values for given caveat list (for current user)
     *
     * @param constraintName
     * @return
     */
    List<String> getRMAllowedValues(String constraintName);

    /**
     * Get custom caveat models
     *
     * @return
     */
    List<QName> getRMCaveatModels();

    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     *
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    boolean hasAccess(NodeRef nodeRef);

    /**
     * Get RM constraint list
     *
     * @param listName the name of the RMConstraintList
     */
    RMConstraintInfo getRMConstraint(String listName);

    /**
     * Add RM constraint
     */
    void addRMConstraint(String listName);

    /**
     * Add RM constraint value for given authority
     */
    void addRMConstraintListValue(String listName, String authorityName, String value);

    /**
     * Update RM constraint values for given authority
     */
    void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values);

    /**
     * Update RM constraint authorities for given value
     */
    void updateRMConstraintListValue(String listName, String valueName, List<String>authorities);

    /**
     * Remove RM constraint value (all authorities)
     */
    void removeRMConstraintListValue(String listName, String valueName);

    /**
     * Remove RM constraint authority (all values)
     */
    void removeRMConstraintListAuthority(String listName, String authorityName);

    /**
     * Delete RM Constraint
     *
     * @param listName the name of the RMConstraintList
     */
    void deleteRMConstraint(String listName);

    /**
     * Get the details of a caveat list
     * @param listName
     * @return
     */
    Map<String, List<String>> getListDetails(String listName);

    NodeRef updateOrCreateCaveatConfig(File jsonFile);

    NodeRef updateOrCreateCaveatConfig(String jsonString);

    NodeRef updateOrCreateCaveatConfig(InputStream is);
}
