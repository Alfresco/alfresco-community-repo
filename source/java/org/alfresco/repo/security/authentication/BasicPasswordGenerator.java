package org.alfresco.repo.security.authentication;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Generates a password of specified length consisting of printable ASCII characters  
 * 
 * @author glen johnson at Alfresco dot com
 */
public class BasicPasswordGenerator implements PasswordGenerator
{
    // password length property
    private int passwordLength;

    /**
     * Set the password length
     * 
     * @param passwordLength the password length
     */
    public void setPasswordLength(int passwordLength)
    {
        this.passwordLength = passwordLength; 
    }
    
    /**
     * Returns a generated password
     * 
     * @return generated password
     */
    public String generatePassword()
    {
        return RandomStringUtils.randomAlphanumeric(passwordLength);
    }
    
    public static void main(String ... args)
    {
        BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
        pwdGen.setPasswordLength(10);
        
        System.out.println("A password: " + pwdGen.generatePassword());
    }
}
