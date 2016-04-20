package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.List;

/**
 * Represents a cloud network (account).
 * 
 * @author steveglover
 *
 */
public interface Network
{
    public String getId();
    
    /**
     * Gets the date the account was created
     *
     * @return  The account creation date
     */
    public Date getCreatedAt();
    public List<Quota> getQuotas();

	/**
     * Gets whether an account is enabled or not. 
     *
     * @return true = account is enabled, false = account is disabled
     */
    public Boolean getIsEnabled();
    
    /**
     * Gets the subscription level.
     * @return String
     */
    public String getSubscriptionLevel();
    
	public Boolean getPaidNetwork();
}