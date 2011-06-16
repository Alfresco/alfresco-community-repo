/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
