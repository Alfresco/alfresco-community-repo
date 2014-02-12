/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.preference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule.RunAsUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/**
 * {@link PreferenceService} implementation unit test
 * 
 * @author Roy Wetherall
 * @author Neil Mc Erlean (refactoring to JUnit Rules and enabling disabled tests)
 */
@Category(BaseSpringTestsCategory.class)
public class PreferenceServiceImplTest
{
    private static final Log log = LogFactory.getLog(PreferenceServiceImplTest.class);

    // JUnit rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    private static final String USERNAME2 = "username2";
    
    // Rules to create test users. Note that this class is unusual in that we do *NOT* want to reuse users across test methods.
    public AlfrescoPerson testUser1 = new AlfrescoPerson(APP_CONTEXT_INIT);
    public AlfrescoPerson testUser2 = new AlfrescoPerson(APP_CONTEXT_INIT, USERNAME2);
    
    // A rule to have all test methods be run as "UserOne".
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(testUser1);
    
    // Tie them together in a Rule Chain
    @Rule public RuleChain ruleChain = RuleChain.outerRule(testUser1)
                                                .around(testUser2)
                                                .around(runAsRule);
    

    // Various services
    private static ContentService               CONTENT_SERVICE;
    private static PersonService                PERSON_SERVICE;
    private static PreferenceService            PREFERENCE_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static ScriptService                SCRIPT_SERVICE;
    
    private static NodeRef COMPANY_HOME;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        CONTENT_SERVICE           = APP_CONTEXT_INIT.getApplicationContext().getBean("ContentService", ContentService.class);
        PERSON_SERVICE            = APP_CONTEXT_INIT.getApplicationContext().getBean("PersonService", PersonService.class);
        PREFERENCE_SERVICE        = APP_CONTEXT_INIT.getApplicationContext().getBean("PreferenceService", PreferenceService.class);
        SCRIPT_SERVICE            = APP_CONTEXT_INIT.getApplicationContext().getBean("ScriptService", ScriptService.class);
        TRANSACTION_HELPER        = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        
        Repository repositoryHelper = (Repository) APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        COMPANY_HOME = repositoryHelper.getCompanyHome();
    }
    
    @Test public void testPreferences() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                // Try and get preferences before they have been set
                Map<String, Serializable> prefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername());
                assertNotNull(prefs);
                assertEquals(0, prefs.size());

                // Lets set some preferences for the user
                prefs = new HashMap<String, Serializable>(5);
                prefs.put("alfresco.one.alpha", "string");
                prefs.put("alfresco.one.beta", 100);
                prefs.put("alfresco.two.alpha", 3.142);
                prefs.put("alfresco.two.beta", COMPANY_HOME);
                prefs.put("alfresco.two.gamma", new Date());
                prefs.put("atTheRoot", "thisIsAtTheRoot");
                PREFERENCE_SERVICE.setPreferences(testUser1.getUsername(), prefs);

                NodeRef personNodeRef = PERSON_SERVICE.getPerson(testUser1.getUsername());
                ContentReader reader = CONTENT_SERVICE.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
                log.debug("JSON: \n" + prettyJson(reader.getContentString()));

                // Try and get all the preferences
                prefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername(), null);
                assertNotNull(prefs);
                assertEquals(6, prefs.size());

                // Try and get some of the preferences
                prefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername(), "alfresco.two");
                assertNotNull(prefs);
                assertEquals(3, prefs.size());

                // Clear some of the preferences
                PREFERENCE_SERVICE.clearPreferences(testUser1.getUsername(), "alfresco.two");
                prefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername(), null);
                assertNotNull(prefs);
                assertEquals(3, prefs.size());

                // Clear all the preferences
                PREFERENCE_SERVICE.clearPreferences(testUser1.getUsername());
                prefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername());
                assertNotNull(prefs);
                assertEquals(0, prefs.size());
                return null;
            }
        });
    }

    @Test(expected=AccessDeniedException.class)
    @RunAsUser(userName=USERNAME2)
    public void testBadUser()
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                Map<String, Serializable> prefs = new HashMap<String, Serializable>(5);
                prefs.put("alfresco.one.alpha", "string");
                PREFERENCE_SERVICE.setPreferences(testUser1.getUsername(), prefs);
                
                return null;
            }
        });
    }

    @Test public void testGetOtherUserPreferences()
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                // Lets set some preferences for the user one
                Map<String, Serializable> prefs = new HashMap<String, Serializable>(5);
                prefs.put("alfresco.one.alpha", "string");
                prefs.put("alfresco.one.beta", 100);
                PREFERENCE_SERVICE.setPreferences(testUser1.getUsername(), prefs);
        
                Map<String, Serializable> userOnePrefs = PREFERENCE_SERVICE.getPreferences(testUser1.getUsername());
                assertNotNull(userOnePrefs);
                assertEquals(2, prefs.size());
                return null;
            }
        });
                
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(USERNAME2);
                // This should not be possible
                try
                {
                    PREFERENCE_SERVICE.getPreferences(testUser1.getUsername());
                }
                catch (AccessDeniedException expected) { return null; }
                fail("Expected exception when trying to access another user's prefs");
                
                return null;
            }
        });
    }

    // == Test the JavaScript API ==
    @Test public void testJSAPI() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                // This test is running as user1 and the JavaScript needs to know that.
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("username1", testUser1.getUsername());
                model.put("username2", testUser2.getUsername());
                
                ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/preference/script/test_preferenceService.js");
                SCRIPT_SERVICE.executeScript(location, model);
                
                return null;
            }
        });
    }
    
    private String prettyJson(String jsonString)
    {
        String result = jsonString;
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(jsonString));
            result = json.toString(2);
        } catch (JSONException ignored)
        {
            // Intentionally empty
        }
        return result;
    }
}
