/**
 * ActionServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public interface ActionServiceSoapPort extends java.rmi.Remote {

    /**
     * Gets the available condition definitions.
     */
    public org.alfresco.repo.webservice.action.ActionItemType[] getConditionDefinitions() throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Gets the available action definitions.
     */
    public org.alfresco.repo.webservice.action.ActionItemType[] getActionDefinitions() throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Gets the availble action types.
     */
    public org.alfresco.repo.webservice.action.RuleType[] getRuleTypes() throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Gets the actions saved against a reference.
     */
    public org.alfresco.repo.webservice.action.Action[] getActions(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.ActionFilter filter) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Save actions against a given reference.
     */
    public org.alfresco.repo.webservice.action.Action[] saveActions(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Removes saved actions.
     */
    public void removeActions(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Executes actions.
     */
    public org.alfresco.repo.webservice.action.ActionExecutionResult[] executeActions(org.alfresco.repo.webservice.types.Predicate predicate, org.alfresco.repo.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Gets the rules for a reference.
     */
    public org.alfresco.repo.webservice.action.Rule[] getRules(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.RuleFilter ruleFilter) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Save rules.
     */
    public org.alfresco.repo.webservice.action.Rule[] saveRules(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.Rule[] rules) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;

    /**
     * Remove saved rules.
     */
    public void removeRules(org.alfresco.repo.webservice.types.Reference reference, org.alfresco.repo.webservice.action.Rule[] rules) throws java.rmi.RemoteException, org.alfresco.repo.webservice.action.ActionFault;
}
