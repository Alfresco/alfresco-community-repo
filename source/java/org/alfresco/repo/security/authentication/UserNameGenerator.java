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
