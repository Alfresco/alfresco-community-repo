/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.tools;


/**
 * Tool Context
 * 
 * @author David Caruana
 */
/*package*/ class ToolContext
{
    /** Help required? */
    private boolean help = false;
    /** Login required? */
    private boolean login = false;
    /** Username */
    private String username = null;
    /** Password */
    private String password = "";
    /** Log messages whilst importing? */
    private boolean quiet = false;
    /** Verbose logging */
    private boolean verbose = false;
    
    
    /**
     * Is help required?
     * 
     * @return  true => help is required
     */
    /*package*/ final boolean isHelp()
    {
        return help;
    }
    
    /**
     * Sets whether help is required
     * 
     * @param help
     */
    /*package*/ final void setHelp(boolean help)
    {
        this.help = help;
    }
    
    /**
     * Is login required?
     * 
     * @return true => login is required
     */
    /*package*/ final boolean isLogin()
    {
        return login;
    }
    
    /**
     * Sets whether login is required
     * 
     * @param login
     */
    /*package*/ final void setLogin(boolean login)
    {
        this.login = login;
    }
    
    /**
     * Get the password
     * 
     * @return the password
     */
    /*package*/ final String getPassword()
    {
        return password;
    }
    
    /**
     * Set the password 
     * 
     * @param password
     */
    /*package*/ final void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
     * Is output is required?
     * 
     * @return true => output is required
     */
    /*package*/ final boolean isQuiet()
    {
        return quiet;
    }
    
    /**
     * Sets whether output is required
     * 
     * @param quiet
     */
    /*package*/ final void setQuiet(boolean quiet)
    {
        this.quiet = quiet;
    }
    
    /**
     * Get the username
     * 
     * @return the username
     */
    /*package*/ final String getUsername()
    {
        return username;
    }
    
    /**
     * Set the username
     * 
     * @param username
     */
    /*package*/ final void setUsername(String username)
    {
        this.username = username;
    }
    
    /**
     * Is verbose logging required?
     * 
     * @return  true => verbose logging is required
     */
    /*package*/ final boolean isVerbose()
    {
        return verbose;
    }
    
    /**
     * Sets whether verbose logging is required
     * 
     * @param verbose
     */
    /*package*/ final void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    /**
     * Validate Tool Context
     */
    /*package*/ void validate()
        throws ToolArgumentException
    {
        if (login)
        {
            if (username == null || username.length() == 0)
            {
                throw new ToolException("Username for login has not been specified.");
            }
        }
    }
    
}
