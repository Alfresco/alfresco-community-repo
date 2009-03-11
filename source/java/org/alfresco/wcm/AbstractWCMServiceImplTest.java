/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.wcm;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Abstract WCM Service implementation unit test
 * 
 * @author janv
 */
public class AbstractWCMServiceImplTest extends TestCase
{
    // override jbpm.job.executor idleInterval to 10s (was 1.5m) for WCM unit tests
    protected static ApplicationContext ctx =new ClassPathXmlApplicationContext(
            new String[] {ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:wcm/wcm-jbpm-context.xml"}
            );
    
    protected static final long SUBMIT_DELAY = 20000L; // 20s - to allow async submit direct workflow to complete (as per 10s idleInterval above)
    
    //
    // test data
    //
    
    protected static final String TEST_RUN = ""+System.currentTimeMillis();
    protected static final boolean CLEAN = true; // cleanup during teardown
    
    //
    // services
    //

    protected AuthenticationService authenticationService;
    protected PersonService personService;

    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
    }
    
    protected void createUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    protected void deleteUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == true)
        {
            personService.deletePerson(userName);
            authenticationService.deleteAuthentication(userName);
        }
    }
}
