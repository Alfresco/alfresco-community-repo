package org.alfresco.repo.security.authentication;

import java.util.Date;

import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import net.sf.acegisecurity.providers.dao.SaltSource;

/**
 * A service provider interface to provide both acegi integration via AuthenticationDao and SaltSource
 * and mutability support for user definitions. 
 * 
 * @author Andy Hind
 */
public interface MutableAuthenticationDao extends AuthenticationDao, SaltSource
{
    /**
     * Create a user with the given userName and password
     */
    public void createUser(String userName, char[] rawPassword) throws AuthenticationException;

    /**
     * Create a user with the given userName and password hash
     * If hashedPassword is passed in then this is used, otherwise it falls back to using the rawPassword.
     * It is assumed the hashed password has been encoded using system.preferred.password.encoding and doesn't use its
     * own salt.
     */
    public void createUser(String caseSensitiveUserName, String hashedPassword, char[] rawPassword) throws AuthenticationException;

    /**
     * Update a user's password.
     */
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException;
    
    /**
     * Delete a user.
     */
    public void deleteUser(String userName) throws AuthenticationException;
    
    /**
     * Check is a user exists.
     */
    public boolean userExists(String userName);

    /**
     * Enable/disable a user.
     */
    public void setEnabled(String userName, boolean enabled);
    
    /**
     * Getter for user enabled
     */
    public boolean getEnabled(String userName);
    
    /**
     * Set if the account should expire
     */
    public void setAccountExpires(String userName, boolean expires);
    
    /**
     * Does the account expire?
     */
    public boolean getAccountExpires(String userName);
    
    /**
     * Has the account expired?
     */
    public boolean getAccountHasExpired(String userName);
  
    /**
     * Set if the password expires.
     */
    public void setCredentialsExpire(String userName, boolean expires);
  
    /**
     * Do the credentials for the user expire?
     */
    public boolean getCredentialsExpire(String userName);
    
    /**
     * Have the credentials for the user expired?
     */
    public boolean getCredentialsHaveExpired(String userName);
    
    /**
     * Set if the account is locked.
     */
    public void setLocked(String userName, boolean locked);
    
    /**
     * Check if the account is locked
     * 
     * @param userName          the username
     * 
     * @since 4.0
     */
    public boolean getLocked(String userName);
    
    /**
     * Is the account locked?
     * 
     * @deprecated Use {@link #getLocked(String)}
     */
    public boolean getAccountlocked(String userName);
    
    /**
     * Set the date on which the account expires
     */
    public void setAccountExpiryDate(String userName, Date exipryDate);
    
    /** 
     * Get the date when this account expires.
     */
    public Date getAccountExpiryDate(String userName);
    
    /**
     * Set the date when credentials expire.
     */
    public void setCredentialsExpiryDate(String userName, Date exipryDate);
    
    /**
     * Get the date when the credentials/password expire.
     */
    public Date getCredentialsExpiryDate(String userName);
    
    /**
     * Get the MD4 password hash
     */
    public String getMD4HashedPassword(String userName);
}
