/**
 * ActionServiceSoapPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.action;

public interface ActionServiceSoapPort extends java.rmi.Remote {

    /**
     * Gets the available condition definitions.
     */
    public org.alfresco.example.webservice.action.ActionItemType[] getConditionDefinitions() throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Gets the available action definitions.
     */
    public org.alfresco.example.webservice.action.ActionItemType[] getActionDefinitions() throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Gets the availble action types.
     */
    public org.alfresco.example.webservice.action.RuleType[] getRuleTypes() throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Gets the actions saved against a reference.
     */
    public org.alfresco.example.webservice.action.Action[] getActions(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.ActionFilter filter) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Save actions against a given reference.
     */
    public org.alfresco.example.webservice.action.Action[] saveActions(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Removes saved actions.
     */
    public void removeActions(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Executes actions.
     */
    public org.alfresco.example.webservice.action.ActionExecutionResult[] executeActions(org.alfresco.example.webservice.types.Predicate predicate, org.alfresco.example.webservice.action.Action[] actions) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Gets the rules for a reference.
     */
    public org.alfresco.example.webservice.action.Rule[] getRules(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.RuleFilter ruleFilter) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Save rules.
     */
    public org.alfresco.example.webservice.action.Rule[] saveRules(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.Rule[] rules) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;

    /**
     * Remove saved rules.
     */
    public void removeRules(org.alfresco.example.webservice.types.Reference reference, org.alfresco.example.webservice.action.Rule[] rules) throws java.rmi.RemoteException, org.alfresco.example.webservice.action.ActionFault;
}
