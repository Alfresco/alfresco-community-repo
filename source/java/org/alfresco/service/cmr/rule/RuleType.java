package org.alfresco.service.cmr.rule;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Rule type interface.
 * 
 * @author Roy Wetherall
 */
public interface RuleType
{
	/**
	 * Some rule type constants
	 */
	public static final String INBOUND = "inbound";
    public static final String UPDATE = "update";
	public static final String OUTBOUND = "outbound";
	
	/**
	 * Get the name of the rule type.
	 * <p>
	 * The name is unique and is used to identify the rule type.
	 * 
	 * @return	the name of the rule type
	 */
	public String getName();
	
	/**
	 * Get the display label of the rule type.
	 * 
	 * @return	the display label
	 */
	public String getDisplayLabel();

	/**
	 * Trigger the rules of the rule type for the node on the actioned upon node.
	 * 
	 * @param nodeRef			   	    the node ref whos rule of rule type are to be triggered
	 * @param actionedUponNodeRef	    the node ref that the triggered rule will action upon
     * @param executeRuleImmediately    indicates whether the rule should be executed immediately or not
	 */
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately);
}