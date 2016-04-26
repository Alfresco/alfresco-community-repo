package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.service.namespace.QName;


/**
 * Definition of a Policy
 * 
 * @author David Caruana
 *
 * @param <P>  the policy interface
 */
@AlfrescoPublicApi
public interface PolicyDefinition<P extends Policy>
{
    /**
     * Gets the name of the Policy
     * 
     * @return  policy name
     */
    public QName getName();
    
    
    /**
     * Gets the Policy interface class
     * 
     * @return  the class
     */
    public Class<P> getPolicyInterface();

    
    /**
     * Gets the Policy type
     * 
     * @return  the policy type
     */
    public PolicyType getType();
    
    /**
     * Gets Policy Argument definition for the specified argument index
     * 
     * @param index  argument index
     * @return  ARG.KEY or ARG.START_VALUE or ARG.END_VALUE
     */
    public Arg getArgument(int index);

    /**
     * Gets Policy Argument definitions for all arguments in order of arguments
     * @return Arg[]
     */
    public Arg[] getArguments();
    
}
