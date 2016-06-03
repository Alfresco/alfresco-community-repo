package org.alfresco.repo.security.authentication;

import java.text.Normalizer;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Generates a user name based upon firstName and lastName. 
 * 
 * The firstNamePattern is used when seed = 0.   
 * Then a random element is added and randomNamePattern is used. 
 *
 */
public class NameBasedUserNameGenerator implements UserNameGenerator
{
    // user name length property
    private int userNameLength = 10;

    /**
     * name generator pattern
     */
    private String namePattern = "%lastName%_%firstName%";
    
    /**
     * The pattern of the user name to generate 
     * e.g. %lastName%_%firstName% would generate Fred_Bloggs
     * 
     * Patterns available:
     *  	%lastName%,  lower case last name
     *  	%firstName%, lower case first name
     *  	%emailAddress% email address
     *      %i% lower case first name inital
     * 
     * @param userNamePattern String
     */
	public void setNamePattern(String userNamePattern) 
	{
		this.namePattern = userNamePattern;
	}

    /**
     * Set the user name length
     * 
     * @param userNameLength the user name length
     */
    public void setUserNameLength(int userNameLength)
    {
        this.userNameLength = userNameLength;
    }
    
    /**
     * Returns a generated user name
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed)
    {
    	String userName;
    	
     	String pattern = namePattern;
     	
     	String initial = firstName.toLowerCase().substring(0,1);
    		
    	userName = pattern
    		.replace("%i%", initial)
    		.replace("%firstName%", cleanseName(firstName))
    		.replace("%lastName%", cleanseName(lastName))
    		.replace("%emailAddress%", emailAddress.toLowerCase());
    	
    	if(seed > 0)
    	{
    		if (userName.length() < userNameLength + 3)
    		{
    		     userName = userName + RandomStringUtils.randomNumeric(3);	
    		}
    		else
    		{
    			// truncate the user name and slap on 3 random characters
    			userName = userName.substring(0, userNameLength -3) + RandomStringUtils.randomNumeric(3);
    		}
    	}
    	
        return userName;
    }
    
    private String cleanseName(String name)
    {
        // Replace whitespace with _
        String result= name.trim().toLowerCase().replaceAll("\\s+", "_");
        
        // Remove accents from characters and strips out non-alphanumeric chars.
        return Normalizer.normalize(result, Normalizer.Form.NFD).replaceAll("[^a-zA-z0-9_]+", "");
    }
}
