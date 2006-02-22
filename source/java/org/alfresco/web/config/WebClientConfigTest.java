/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.source.FileConfigSource;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;

/**
 * JUnit tests to exercise the capabilities added to the web client config
 * service
 * 
 * @author gavinc
 */
public class WebClientConfigTest extends BaseTest
{
   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   /**
    * Tests the property sheet configuration classes
    */
   public void testPropertySheetConfig()
   {
      // setup the config service
      String configFile = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFile));
      svc.init();

      // get hold of the property sheet config from the global section
      Config global = svc.getGlobalConfig();
      ConfigElement globalPropSheet = global.getConfigElement("property-sheet");
      assertNotNull("global property sheet element should not be null", globalPropSheet);
      assertTrue("config element should be an instance of PropertySheetConfigElement",
            (globalPropSheet instanceof PropertySheetConfigElement));

      // get the property names from the global section and make sure it is the
      // name property
      List<String> propNames = ((PropertySheetConfigElement) globalPropSheet).getItemNamesToShow();
      assertTrue("There should only be one property in the list", propNames.size() == 1);
      assertTrue("The property name should be 'name'", propNames.get(0).equals("name"));

      // get the config section representing a space aspect and make sure we get
      // 5 properties
      Config spaceAspectConfig = svc.getConfig("space-aspect");
      assertNotNull("Space aspect config should not be null", spaceAspectConfig);
      PropertySheetConfigElement spacePropConfig = (PropertySheetConfigElement) spaceAspectConfig
            .getConfigElement("property-sheet");
      assertNotNull("Space aspect property config should not be null", spacePropConfig);
      propNames = spacePropConfig.getItemNamesToShow();
      assertTrue("There should be 6 properties in the list", propNames.size() == 6);

      // make sure the property sheet config has come back with the correct data
      Map<String, ItemConfig> props = spacePropConfig.getItemsMapToShow();
      ItemConfig descProp = props.get("description");
      assertNotNull("description property config should not be null", descProp);
      assertEquals("display label for description should be 'Description'", descProp.getDisplayLabel(), 
            "Description");
      assertFalse("read only for description should be 'false'", descProp.isReadOnly());

      ItemConfig createdDataProp = props.get("createddate");
      assertNotNull("createddate property config should not be null", createdDataProp);
      assertEquals("display label for createddate should be null", null, createdDataProp.getDisplayLabel());
      assertTrue("read only for createddate should be 'true'", createdDataProp.isReadOnly());

      ItemConfig iconProp = props.get("icon");
      assertNotNull("icon property config should not be null", iconProp);
      assertEquals("display label for icon should be null", null, iconProp.getDisplayLabel());
      assertFalse("read only for icon should be 'false'", iconProp.isReadOnly());
   }
   
   /**
    * Tests the config service by retrieving property sheet configuration using
    * the generic interfaces
    */
   public void testGenericConfigElement()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();

      // get the space aspect configuration
      Config configProps = svc.getConfig("space-aspect");
      ConfigElement propsToDisplay = configProps.getConfigElement("property-sheet");
      assertNotNull("property sheet config should not be null", propsToDisplay);

      // get all the property names using the ConfigElement interface methods
      List<ConfigElement> kids = propsToDisplay.getChildren();
      List<String> propNames = new ArrayList<String>();
      for (ConfigElement propElement : propsToDisplay.getChildren())
      {
         String value = propElement.getValue();
         assertNull("property value should be null", value);
         String propName = propElement.getAttribute("name");
         propNames.add(propName);
      }

      assertTrue("There should be 6 properties", propNames.size() == 6);
      assertFalse("The id attribute should not be present", propsToDisplay.hasAttribute("id"));
      
      // make sure the inEditMode and readOnly flags are set correctly on the last property
      assertEquals("showInEditMode", "false", kids.get(5).getAttribute("showInEditMode"));
      assertEquals("readOnly", "true", kids.get(5).getAttribute("readOnly"));
   }

   /**
    * Tests the config service by retrieving property sheet configuration using
    * the custom config objects
    */
   public void testGetProperties()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the space aspect configuration
      Config configProps = svc.getConfig("space-aspect");
      PropertySheetConfigElement propsToDisplay = (PropertySheetConfigElement)configProps.
            getConfigElement("property-sheet");
      assertNotNull("property sheet config should not be null", propsToDisplay);
      
      // get all the property names using the PropertySheetConfigElement implementation
      List<String> propNames = propsToDisplay.getItemNamesToShow();
              
      // make sure the generic interfaces are also returning the correct data
      List<ConfigElement> kids = propsToDisplay.getChildren();
      assertNotNull("kids should not be null", kids);
      assertTrue("There should be more than one child", kids.size() > 1);
      
      assertEquals("There should be 6 properties", propNames.size() == 6, true);
      assertFalse("The id attribute should not be present", propsToDisplay.hasAttribute("id"));
   }
   
   public void testPropertyEditing()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      Config propEditConfig = svc.getConfig("Property Editing");
      assertNotNull("Property Editing section should not be null", propEditConfig);
      
      PropertySheetConfigElement propSheet = (PropertySheetConfigElement)propEditConfig.
            getConfigElement("property-sheet");
      assertNotNull("property-sheet config should not be null", propSheet);
      
      // make sure the list of names method works correctly
      List<String> itemNamesToEdit = propSheet.getEditableItemNamesToShow();
      assertNotNull("itemNamesToEdit should not be null", itemNamesToEdit);
      assertEquals("Number of properties", 3, itemNamesToEdit.size());
      
      // make sure the property names are correct
      assertEquals("first property name", "name", itemNamesToEdit.get(0));
      assertEquals("second property name", "description", itemNamesToEdit.get(1));
      assertEquals("third property name", "icon", itemNamesToEdit.get(2));
      
      // make sure the map has the correct number of items
      Map<String, ItemConfig> itemsToEditMap = propSheet.getEditableItemsMapToShow();
      assertNotNull("itemsToEditMap should not be null", itemsToEditMap);
      assertEquals("Number of properties", 3, itemsToEditMap.size());
      
      // make sure the icon property is set as read only
      ItemConfig item = itemsToEditMap.get("icon");
      assertNotNull("icon should not be null", item);
      assertTrue("icon property readOnly status should be true", item.isReadOnly());
      
      // make the size property is unavailable
      item = itemsToEditMap.get("size");
      assertNull("size should be null", item);
      
      // make sure the list has the correct numbe of items
      List<ItemConfig> itemsToEdit = propSheet.getEditableItemsToShow();
      assertNotNull("itemsToEdit should not be null", itemsToEdit);
      assertEquals("Number of properties", 3, itemsToEdit.size());
   }
   
   /**
    * Tests the custom client configuration objects 
    */
   public void testClientConfig()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the global config and from that the client config
      ClientConfigElement clientConfig = (ClientConfigElement)svc.getGlobalConfig().
         getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("client config", clientConfig);
      
      assertEquals("error page", "/jsp/error.jsp", clientConfig.getErrorPage());
      assertEquals("login page", "/jsp/login.jsp", clientConfig.getLoginPage());
      assertEquals("home space permission", "Consumer", clientConfig.getHomeSpacePermission());
      assertEquals("help url", "http://www.alfresco.org/help/webclient", clientConfig.getHelpUrl());
      assertEquals("edit link type", "http", clientConfig.getEditLinkType());
      assertEquals("from address", "alfresco@alfresco.org", clientConfig.getFromEmailAddress());
      assertEquals("recent spaces", 6, clientConfig.getRecentSpacesItems());
      assertEquals("search minimum", 3, clientConfig.getSearchMinimum());
      assertTrue("shelf visible", clientConfig.isShelfVisible());
   }
   
   public void testClientOverride()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
        
      // try and get the global config section
      Config globalSection = svc.getGlobalConfig();
      assertNotNull("global section", globalSection);
      
      // get the client config
      ClientConfigElement clientConfig = (ClientConfigElement)globalSection.
            getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("client config", clientConfig);
      
      // make sure the error page is still set as the default
      assertEquals("error page", "/jsp/error.jsp", clientConfig.getErrorPage());
      assertEquals("login page", "/jsp/login-override.jsp", clientConfig.getLoginPage());
      assertEquals("home space permission", "Editor", clientConfig.getHomeSpacePermission());
      assertEquals("help url", "http://www.somewhere.com/help", clientConfig.getHelpUrl());
      assertEquals("edit link type", "webdav", clientConfig.getEditLinkType());
      assertEquals("from address", "me@somewhere.com", clientConfig.getFromEmailAddress());
      assertEquals("recent spaces", 1, clientConfig.getRecentSpacesItems());
      assertEquals("search minimum", 10, clientConfig.getSearchMinimum());
      assertFalse("shelf visible", clientConfig.isShelfVisible());
   }
   
   /**
    * Tests the navigation config i.e. the custom element reader and config element
    */
   public void testNavigation()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();

      // *** Test the returning of a view id override
      Config testCfg = svc.getConfig("viewid-navigation-result");
      assertNotNull("viewid-navigation-result config should not be null", testCfg);
      
      NavigationConfigElement navCfg = (NavigationConfigElement)testCfg.getConfigElement("navigation");
      assertNotNull("navigation config should not be null", navCfg);
      
      // get the result for the browse view id
      NavigationResult navResult = navCfg.getOverride("/jsp/browse/browse.jsp", null);
      assertEquals("result should be '/jsp/forums/forums.jsp'", "/jsp/forums/forums.jsp", 
            navResult.getResult());
      assertFalse("isOutcome test should be false", navResult.isOutcome());
      
      // get the result for the browse outcome
      navResult = navCfg.getOverride(null, "browse");
      assertEquals("result should be '/jsp/forums/topics.jsp'", "/jsp/forums/topics.jsp", 
            navResult.getResult());
      assertFalse("isOutcome test should be false", navResult.isOutcome());
      
      // get the result when passing both the browse view id and outcome, make
      // sure we get the result for the outcome as it should take precedence
      navResult = navCfg.getOverride("/jsp/browse/browse.jsp", "browse");
      assertEquals("result should be '/jsp/forums/topics.jsp'", "/jsp/forums/topics.jsp", 
            navResult.getResult());
      assertFalse("isOutcome test should be false", navResult.isOutcome());
      
      // *** Test the returning of an outcome override
      testCfg = svc.getConfig("outcome-navigation-result");
      assertNotNull("outcome-navigation-result config should not be null", testCfg);
      
      navCfg = (NavigationConfigElement)testCfg.getConfigElement("navigation");
      assertNotNull("navigation config should not be null", navCfg);
      
      // get the result for the browse view id
      navResult = navCfg.getOverride("/jsp/browse/browse.jsp", null);
      assertEquals("result should be 'showSomethingElse'", "showSomethingElse", 
            navResult.getResult());
      assertTrue("isOutcome test should be true", navResult.isOutcome());
      
      // get the result for the browse outcome
      navResult = navCfg.getOverride(null, "browse");
      assertEquals("result should be 'showSomethingElse'", "showSomethingElse", 
            navResult.getResult());
      assertTrue("isOutcome test should be true", navResult.isOutcome());
      
      // get the result when passing both the browse view id and outcome, make
      // sure we get the result for the outcome as it should take precedence
      navResult = navCfg.getOverride("/jsp/browse/browse.jsp", "browse");
      assertEquals("result should be 'showSomethingElse'", "showSomethingElse", 
            navResult.getResult());
      assertTrue("isOutcome test should be true", navResult.isOutcome());
      
      // *** Test the duplicate result config 
      testCfg = svc.getConfig("duplicate-navigation-overrides");
      assertNotNull("duplicate-navigation-overrides config should not be null", testCfg);
      
      navCfg = (NavigationConfigElement)testCfg.getConfigElement("navigation");
      assertNotNull("navigation config should not be null", navCfg);
      
      // make sure the outcome result is 'newOutcome'
      navResult = navCfg.getOverride(null, "browse");
      assertEquals("result should be 'newOutcome'", "newOutcome", 
            navResult.getResult());
      assertTrue("isOutcome test should be true", navResult.isOutcome());
      
      // call getOverride passing a valid view id but an invalid outcome
      // and make sure the result is null
      navResult = navCfg.getOverride("/jsp/browse/browse.jsp", "nonExistentOutcome");
      assertNull("result should be null", navResult);
   }
   
   public void testNavigationGenericConfig()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // do a lookup using the generic config elements and make sure the correct
      // info comes out
      Config testCfg = svc.getConfig("duplicate-navigation-overrides");
      assertNotNull("duplicate-navigation-overrides config should not be null", testCfg);
      
      ConfigElement ce = testCfg.getConfigElement("navigation");
      assertNotNull("navigation config should not be null", ce);
      
      List<ConfigElement> children = ce.getChildren();
      assertNotNull(children);
      
      // make sure there are 2 children
      assertEquals("There should be 2 children", 2, children.size());
      
      // get the first child and make sure the attributes are correct,
      // from-view-id should be '/jsp/browse/browse.jsp' and to-view-id
      // should be '/jsp/forums/forums.jsp'
      ConfigElement child = children.get(0);
      String fromViewId = child.getAttribute("from-view-id");
      String fromOutcome = child.getAttribute("from-outcome");
      String toViewId = child.getAttribute("to-view-id");
      String toOutcome = child.getAttribute("to-outcome");
      
      assertNull("fromOutcome", fromOutcome);
      assertNull("toOutcome", toOutcome);
      assertEquals("fromViewId", "/jsp/browse/browse.jsp", fromViewId);
      assertEquals("toViewId", "/jsp/forums/forums.jsp", toViewId);
      
      // get the second child and make sure the attributes are correct,
      // from-outcome should be 'browse' and to-outcome should be 'newOutcome'
      child = children.get(1);
      fromViewId = child.getAttribute("from-view-id");
      fromOutcome = child.getAttribute("from-outcome");
      toViewId = child.getAttribute("to-view-id");
      toOutcome = child.getAttribute("to-outcome");
      
      assertNull("fromViewId", fromViewId);
      assertNull("toViewId", toViewId);
      assertEquals("fromOutcome", "browse", fromOutcome);
      assertEquals("toOutcome", "newOutcome", toOutcome);
   }
   
   public void testLanguages()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      LanguagesConfigElement config = (LanguagesConfigElement)svc.getConfig("Languages").
            getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("languages config", config);
      
      // make sure there are 3 languages returned
      assertEquals("number of languages", 4, config.getLanguages().size());
      
      // make sure they are returned in order
      assertEquals("first language", "en_US", config.getLanguages().get(0));
      assertEquals("second language", "fr_FR", config.getLanguages().get(1));
      assertEquals("third language", "de_DE", config.getLanguages().get(2));
      assertEquals("fourth language", "ja_JP", config.getLanguages().get(3));
      
      // make sure the labels are correct too
      assertEquals("en_US", "English", config.getLabelForLanguage("en_US"));
      assertEquals("fr_FR", "French", config.getLabelForLanguage("fr_FR"));
      assertEquals("de_DE", "German", config.getLabelForLanguage("de_DE"));
      assertEquals("ja_JP", "Japanese", config.getLabelForLanguage("ja_JP"));
      
      // make sure the getChildren method throws an exception
      try
      {
         config.getChildren();
         fail("getChildren() did not throw an excpetion");
      }
      catch (ConfigException ce)
      {
         // expected
      }
   }
   
   public void testAdvancedSearch()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      AdvancedSearchConfigElement config = (AdvancedSearchConfigElement)svc.getConfig("Advanced Search").
            getConfigElement(AdvancedSearchConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("advanced search config", config);
      
      // make sure there are 2 custom types
      assertEquals("number of content types", 2, config.getContentTypes().size());
      
      // make sure they are correct
      assertEquals("first type", "cm:dictionaryModel", config.getContentTypes().get(0));
      assertEquals("second type", "fm:post", config.getContentTypes().get(1));
      
      // make sure there are 3 custom properties
      assertEquals("number of content properties", 3, config.getCustomProperties().size());
      
      CustomProperty property = config.getCustomProperties().get(0);
      assertTrue("first property is type", property.Type != null);
      
      property = config.getCustomProperties().get(1);
      assertTrue("second property is aspect", property.Type == null);
      assertTrue("second property is aspect", property.Aspect != null);
      assertEquals("second property aspect", "app:simpleworkflow", property.Aspect);
      assertEquals("second property name", "app:approveStep", property.Property);
      
      property = config.getCustomProperties().get(2);
      assertEquals("third property name", "app:rejectStep", property.Property);
      assertEquals("third property display id", "reject_step", property.LabelId);
      
      // make sure the getChildren method throws an exception
      try
      {
         config.getChildren();
         fail("getChildren() did not throw an excpetion");
      }
      catch (ConfigException ce)
      {
         // expected
      }
   }
   
   public void testViews()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      ViewsConfigElement config = (ViewsConfigElement)svc.getConfig("Views").
            getConfigElement(ViewsConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("views config", config);
      
      // make sure there are 4 views
      List<String> views = config.getViews();
      assertEquals("configured views", 4, views.size());
      
      // make sure the views are correct
      assertEquals("details view renderer", 
            "org.alfresco.web.ui.common.renderer.data.RichListRenderer$DetailsViewRenderer", 
            views.get(0));
      assertEquals("icons view renderer", 
            "org.alfresco.web.ui.common.renderer.data.RichListRenderer$IconViewRenderer", 
            views.get(1));
      assertEquals("list view renderer", 
            "org.alfresco.web.ui.common.renderer.data.RichListRenderer$ListViewRenderer", 
            views.get(2));
      assertEquals("bubble view renderer", 
            "org.alfresco.web.bean.ForumsBean$TopicBubbleViewRenderer", views.get(3));

      // test default views 
      assertEquals("default view", "details", config.getDefaultView("not-there"));
      assertEquals("default view for topic", "bubble", config.getDefaultView("topic"));
      
      // test page sizes
      assertEquals("default page size", 10, config.getDefaultPageSize("not", "there"));
      assertEquals("forums icons page size", 20, config.getDefaultPageSize("forums", "icons"));
      assertEquals("forum details page size", 50, config.getDefaultPageSize("forum", "details"));
      assertEquals("icons view page size", 9, config.getDefaultPageSize("not-there", "icons"));
      
      // test the sort columns
      assertEquals("default sort column", "name", config.getDefaultSortColumn("not-there"));
      assertEquals("browse page sort column", "name", config.getDefaultSortColumn("browse"));
      assertEquals("forum page sort column", "modified", config.getDefaultSortColumn("forum"));
      assertEquals("topic page sort column", "created", config.getDefaultSortColumn("topic"));
      
      // test the sorting direction
      assertFalse("default sort direction should be ascending", config.hasDescendingSort("not-there"));
      assertFalse("browse screen should use an ascending sort", config.hasDescendingSort("browse"));
      assertTrue("topic screen should use a descending sort", config.hasDescendingSort("forum"));
      assertFalse("topic screen should use an ascending sort", config.hasDescendingSort("topic"));
      
      // make sure the getChildren method throws an exception
      try
      {
         config.getChildren();
         fail("getChildren() did not throw an excpetion");
      }
      catch (ConfigException ce)
      {
         // expected
      }
   }
}
