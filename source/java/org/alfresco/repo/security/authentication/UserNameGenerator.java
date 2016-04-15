package org.alfresco.repo.security.authentication;

/**
 * Implementations of this interface generate a user name
 * 
 * @author glen johnson at Alfresco dot com
 */
public interface UserNameGenerator
{
    /**
     * Returns a generated user name.
     * 
     * A seed value of 0 means first attempt.   A non zero seed value indicates that the obvious user name is already taken 
     * and that some random element needs to be added to make a unique user id. 
     * 
     * @param firstName the given name of the new user
     * @param lastName the family name of the new user
     * @param emailAddress the email address of the new user
     * @param seed a seed for user name generation, the value 0 means "no seed"
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed);
}
