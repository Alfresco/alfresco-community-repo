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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.source.FileConfigSource;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.ActionsConfigElement.ActionDefinition;
import org.alfresco.web.config.ActionsConfigElement.ActionGroup;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.alfresco.web.config.WizardsConfigElement.ConditionalPageConfig;
import org.alfresco.web.config.WizardsConfigElement.PageConfig;
import org.alfresco.web.config.WizardsConfigElement.StepConfig;
import org.alfresco.web.config.WizardsConfigElement.WizardConfig;

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
      Map<String, ItemConfig> props = spacePropConfig.getItemsToShow();
      ItemConfig descProp = props.get("description");
      assertNotNull("description property config should not be null", descProp);
      assertEquals("display label for description should be 'Description'", descProp.getDisplayLabel(), 
            "Description");
      assertFalse("read only for description should be 'false'", descProp.isReadOnly());

      ItemConfig createdDataProp = props.get("createddate");
      assertNotNull("createddate property config should not be null", createdDataProp);
      assertEquals("display label for createddate should be null", null, createdDataProp.getDisplayLabel());
      assertTrue("read only for createddate should be 'true'", createdDataProp.isReadOnly());
      assertTrue("ignoreIfMissing for createddate should be 'true'", createdDataProp.getIgnoreIfMissing());

      ItemConfig iconProp = props.get("icon");
      assertNotNull("icon property config should not be null", iconProp);
      assertEquals("display label for icon should be null", null, iconProp.getDisplayLabel());
      assertEquals("component-generator", "SpaceIconPickerGenerator", iconProp.getComponentGenerator());
      assertFalse("read only for icon should be 'false'", iconProp.isReadOnly());
      assertFalse("ignoreIfMissing for icon should be 'false'", iconProp.getIgnoreIfMissing());
      
      // test that a call to the generic getChildren call throws an error
      try
      {
         spacePropConfig.getChildren();
         fail("getChildren() did not throw an excpetion");
      }
      catch (ConfigException ce)
      {
         // expected
      } 
   }
   
   public void testPropertyViewing()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      Config propViewConfig = svc.getConfig("Property Viewing");
      assertNotNull("Property Viewing section should not be null", propViewConfig);
      
      PropertySheetConfigElement propSheet = (PropertySheetConfigElement)propViewConfig.
            getConfigElement("property-sheet");
      assertNotNull("property-sheet config should not be null", propSheet);
      
      // make sure the all items map works correctly
      Map<String, ItemConfig> allItems = propSheet.getItems();
      assertNotNull("allItems should not be null", allItems);
      assertEquals("Total number of properties", 5, allItems.size());
      assertNotNull("name property is missing", allItems.get("name"));
      assertNotNull("description property is missing", allItems.get("description"));
      assertNotNull("icon property is missing", allItems.get("icon"));
      assertNotNull("size property is missing", allItems.get("size"));
      assertNotNull("uuid property is missing", allItems.get("uuid"));
      
      // make sure the viewable properties list is correct
      List<String> itemsToShow = propSheet.getItemNamesToShow();
      assertNotNull("itemsToShow should not be null", itemsToShow);
      assertEquals("Number of viewable properties", 3, itemsToShow.size());
      assertEquals("first viewable property name", "name", itemsToShow.get(0));
      assertEquals("second viewable property name", "description", itemsToShow.get(1));
      assertEquals("third viewable property name", "size", itemsToShow.get(2));
      
      // make sure the editable properties list is correct
      List<String> editItems = propSheet.getEditableItemNamesToShow();
      assertNotNull("editItems should not be null", editItems);
      assertEquals("Number of editable properties", 3, editItems.size());
      assertEquals("first viewable property name", "name", editItems.get(0));
      assertEquals("second viewable property name", "description", editItems.get(1));
      assertEquals("third viewable property name", "icon", editItems.get(2));
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
      Map<String, ItemConfig> itemsToEditMap = propSheet.getEditableItemsToShow();
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
      Collection<ItemConfig> itemsToEdit = propSheet.getEditableItemsToShow().values();
      assertNotNull("itemsToEdit should not be null", itemsToEdit);
      assertEquals("Number of properties", 3, itemsToEdit.size());
   }
   
   public void testPropertyOverride()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the config for the size property in the space-aspect property sheet
      PropertySheetConfigElement propSheet = ((PropertySheetConfigElement)svc.getConfig("space-aspect").
            getConfigElement(PropertySheetConfigElement.CONFIG_ELEMENT_ID));
      assertNotNull("propSheet should not be null", propSheet);
      
      // make sure the list and map numbers are correct
      assertEquals("prop names to show size", 6, propSheet.getItemNamesToShow().size());
      assertEquals("props to show size", 6, propSheet.getItemsToShow().size());
      assertEquals("edit prop names to show size", 5, propSheet.getEditableItemNamesToShow().size());
      assertEquals("edit props to show size", 5, propSheet.getEditableItemsToShow().size());
      
      PropertySheetConfigElement.PropertyConfig propConfig = 
         (PropertySheetConfigElement.PropertyConfig)propSheet.getItemsToShow().get("size");
      assertNotNull("propConfig should not be null", propConfig);
      
      // make sure config has been overridden
      assertTrue("size should be shown in edit mode", propConfig.isShownInEditMode());
      assertTrue("size property should be read only", propConfig.isReadOnly());
      assertNotNull("size property should be in edit map", propSheet.getEditableItemsToShow().get("size"));
      
      // get the icon 
      propConfig = (PropertySheetConfigElement.PropertyConfig)propSheet.getItemsToShow().get("icon");
      assertNotNull("propConfig should not be null", propConfig);
      
      //
      // test the overridding of viewable/editable items
      //
      
      propSheet = ((PropertySheetConfigElement)svc.getConfig("Property Viewing").
            getConfigElement(PropertySheetConfigElement.CONFIG_ELEMENT_ID));
      assertNotNull("property-sheet config should not be null", propSheet);
      
      // make sure the all items map works correctly
      Map<String, ItemConfig> allItems = propSheet.getItems();
      assertNotNull("allItems should not be null", allItems);
      assertEquals("Total number of properties", 5, allItems.size());
      assertNotNull("name property is missing", allItems.get("name"));
      assertNotNull("description property is missing", allItems.get("description"));
      assertNotNull("icon property is missing", allItems.get("icon"));
      assertNotNull("size property is missing", allItems.get("size"));
      assertNotNull("uuid property is missing", allItems.get("uuid"));
      
      // make sure the viewable properties list is correct
      List<String> itemsToShow = propSheet.getItemNamesToShow();
      assertNotNull("itemsToShow should not be null", itemsToShow);
      assertEquals("Number of viewable properties", 4, itemsToShow.size());
      assertEquals("first viewable property name", "name", itemsToShow.get(0));
      assertEquals("second viewable property name", "size", itemsToShow.get(1));
      assertEquals("third viewable property name", "icon", itemsToShow.get(2));
      assertEquals("third viewable property name", "uuid", itemsToShow.get(3));
      
      // make sure the editable properties list is correct
      List<String> editItems = propSheet.getEditableItemNamesToShow();
      assertNotNull("editItems should not be null", editItems);
      assertEquals("Number of editable properties", 3, editItems.size());
      assertEquals("first viewable property name", "name", editItems.get(0));
      assertEquals("second viewable property name", "icon", editItems.get(1));
      assertEquals("third viewable property name", "uuid", editItems.get(2));
    
      // make sure icon is no longer read-only
      ItemConfig iconCfg = propSheet.getItemsToShow().get("icon");
      assertNotNull("iconCfg not should not be null", iconCfg);
      assertFalse("icon should not be read-only", iconCfg.isReadOnly());
      assertTrue("icon should be shown in view mode", iconCfg.isShownInViewMode());
      assertTrue("icon should be shown in edit mode", iconCfg.isShownInEditMode());
      assertFalse("ignoreIfMissing for icon should be 'false'", iconCfg.getIgnoreIfMissing());
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
   
   public void testDialogs()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config-dialogs-wizards.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get Dialogs config section
      Config dialogsConfig = svc.getConfig("Dialogs");
      assertNotNull("dialogsConfig should not be null", dialogsConfig);
      
      // make sure the dialog-container is correct
      assertEquals("dialog container", "/jsp/dialog/container.jsp", 
            dialogsConfig.getConfigElement("dialog-container").getValue());
      
      // make sure a non existent dialog returns null
      assertNull("non existent dialog test should return null", 
            dialogsConfig.getConfigElement("Non Existent Dialog"));
      
      // get the dialogs element
      DialogsConfigElement dialogsElement = (DialogsConfigElement)dialogsConfig.
            getConfigElement(DialogsConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("dialogsElement should not be null", dialogsElement);
      
      // make sure there are 2 items in the list and map
      assertEquals("map size", 2, dialogsElement.getDialogs().size());
      
      // get the 'createSpace' dialog
      DialogConfig dialog = dialogsElement.getDialog("createSpace");
      assertNotNull("createSpace dialog config should not be null", dialog);
      
      // make sure the info on the dialog is correct
      assertEquals("name", "createSpace", dialog.getName());
      assertEquals("page", "/jsp/dialog/create-space.jsp", dialog.getPage());
      assertEquals("managed-bean", "NewSpaceDialog", dialog.getManagedBean());
      assertEquals("actions-config-id", "space-actions", dialog.getActionsConfigId());
      assertEquals("icon", "/images/icons/create_space_large.gif", dialog.getIcon());
      assertEquals("title-id", "create_space_title", dialog.getTitleId());
      assertEquals("description-id", "create_space_description", dialog.getDescriptionId());
      assertEquals("error-message-id", "error_create_space_dialog", dialog.getErrorMessageId());
      assertNull("title should be null", dialog.getTitle());
      assertNull("description should be null", dialog.getDescription());
      
      // get the 'spaceDetails' dialog
      dialog = dialogsElement.getDialog("spaceDetails");
      assertNotNull("spaceDetails dialog config should not be null", dialog);
      
      // make sure the info on the dialog is correct
      assertEquals("name", "spaceDetails", dialog.getName());
      assertEquals("page", "/jsp/dialog/space-details.jsp", dialog.getPage());
      assertEquals("managed-bean", "SpaceDetailsDialog", dialog.getManagedBean());
      assertEquals("actions-config-id", "space-actions", dialog.getActionsConfigId());
      assertEquals("icon", "/images/icons/create_space_large.gif", dialog.getIcon());
      assertEquals("title", "Space Details Dialog", dialog.getTitle());
      assertEquals("description", "Space Details Dialog Decsription", dialog.getDescription());
      assertEquals("error-message-id", "error_dialog", dialog.getErrorMessageId());
      assertNull("title-id should be null", dialog.getTitleId());
      assertNull("description-id should be null", dialog.getDescriptionId());
   }
   
   public void testDialogOverride()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config-dialogs-wizards.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the 'dialogs' element
      DialogsConfigElement dialogsElement = ((DialogsConfigElement)svc.getConfig("Dialogs").
            getConfigElement(DialogsConfigElement.CONFIG_ELEMENT_ID));
      
      // make sure there are 2 items in the list and map
      assertEquals("map size", 2, dialogsElement.getDialogs().size());
      
      // get the 'createSpace' dialog
      DialogConfig dialog = dialogsElement.getDialog("createSpace");
      assertNotNull("createSpace dialog should not be null", dialog);
      
      // make sure the page and managed bean attributes have been overridden
      assertEquals("page", "/custom/my-create-space.jsp", dialog.getPage());
      assertEquals("managed-bean", "MyNewSpaceDialog", dialog.getManagedBean());
      assertEquals("title-id", "create_space_title", dialog.getTitleId());
      assertEquals("description-id", "create_space_description", dialog.getDescriptionId());
   }
   
   public void testWizards()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config-dialogs-wizards.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get Dialogs config section
      Config wizardsConfig = svc.getConfig("Wizards");
      assertNotNull("wizardsConfig should not be null", wizardsConfig);
      
      // make sure the wizard-container is correct
      assertEquals("wizard container", "/jsp/wizard/container.jsp", 
            wizardsConfig.getConfigElement("wizard-container").getValue());
      
      // make sure a non existent wizard returns null
      assertNull("non existent wizard should not be null", 
            wizardsConfig.getConfigElement("Non Existent Wizard"));
      
      // get the wizards element
      WizardsConfigElement wizardsElement = (WizardsConfigElement)wizardsConfig.
            getConfigElement(WizardsConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("wizardsElement should not be null", wizardsElement);
      
      // make sure there are 2 items in the map
      assertEquals("map size", 2, wizardsElement.getWizards().size());
      
      // get the 'exampleWizard' wizard
      WizardConfig wizard = wizardsElement.getWizard("exampleWizard");
      assertNotNull("exampleWizard wizard should not be null", wizard);
            
      // make sure data is correct
      assertEquals("name", "exampleWizard", wizard.getName());
      assertEquals("exampleWizard steps", 2, wizard.getNumberSteps());
      assertEquals("managed-bean", "ExampleWizard", wizard.getManagedBean());
      assertEquals("actions-config-id", "example-wizard-actions", wizard.getActionsConfigId());
      assertEquals("icon", "/images/icons/example-logo.gif", wizard.getIcon());
      assertEquals("title", "Example Wizard Title", wizard.getTitle());
      assertEquals("description", "Example Wizard Description", wizard.getDescription());
      assertEquals("error-message-id", "error_wizard", wizard.getErrorMessageId());
      assertNull("title-id should be null", wizard.getTitleId());
      assertNull("description-id should be null", wizard.getDescriptionId());
      
      // retrive step 1 config and check it is correct
      Map<String, StepConfig> stepsMap = wizard.getSteps();
      StepConfig step1 = stepsMap.get("details");
      assertNotNull("step 1 of example wizard should not be null", step1);
      assertEquals("step title", "Details", step1.getTitle());
      assertNull("step 1 title-id should be null", step1.getTitleId());
      
      // get the 'createSpace' wizard and ensure all the data is correct
      wizard = wizardsElement.getWizard("createSpace");
      assertEquals("name", "createSpace", wizard.getName());
      assertEquals("createSpace steps", 3, wizard.getNumberSteps());
      assertEquals("managed-bean", "AdvancedSpaceWizard", wizard.getManagedBean());
      assertEquals("actions-config-id", "create-space-actions", wizard.getActionsConfigId());
      assertEquals("icon", "/images/icons/create_space_large.gif", wizard.getIcon());
      assertEquals("title-id", "advanced_space_details_title", wizard.getTitleId());
      assertEquals("description-id", "advanced_space_details_description", wizard.getDescriptionId());
      assertEquals("error-message-id", "error_create_space_wizard", wizard.getErrorMessageId());
      assertNull("title should be null", wizard.getTitle());
      assertNull("description should be null", wizard.getDescription());
      List<StepConfig> steps = wizard.getStepsAsList();
      assertNotNull("steps should not be null", steps);
      
      // retrieve step1 information and check it is correct
      step1 = steps.get(0);
      assertEquals("step 1 name", "details", step1.getName());
      assertEquals("step 1 title-id", "starting_space", step1.getTitleId());
      assertFalse("step 1 should not have conditional pages", step1.hasConditionalPages());
      PageConfig step1Page = step1.getDefaultPage();
      assertNotNull("step1Page should not be null", step1Page);
      assertEquals("step 1 page", "/jsp/wizard/new-space/create-from.jsp", step1Page.getPath());
      assertEquals("step 1 title-id", "create_space_details_title", step1Page.getTitleId());
      assertEquals("step 1 description-id", "create_space_details_desc", step1Page.getDescriptionId());
      assertEquals("step 1 instruction-id", "create_space_details_instruction", step1Page.getInstructionId());
      assertNull("step 1 title should be null", step1Page.getTitle());
      assertNull("step 1 description should be null", step1Page.getDescription());
      
      // check the conditional step2 data
      StepConfig step2 = steps.get(1);
      assertEquals("step 2 name", "properties", step2.getName());
      assertEquals("step 2 title-id", "space_options", step2.getTitleId());
      assertTrue("step 2 should have conditional pages", step2.hasConditionalPages());
      PageConfig step2DefaultPage = step2.getDefaultPage();
      assertNotNull("step 2 default page should not be null", step2DefaultPage);
      assertEquals("step 2 default page", "/jsp/wizard/new-space/from-scratch.jsp", step2DefaultPage.getPath());
      assertEquals("step 2 default title-id", "create_space_scratch_title", step2DefaultPage.getTitleId());
      assertEquals("step 2 default description-id", "create_space_scratch_desc", step2DefaultPage.getDescriptionId());
      assertEquals("step 2 default instruction-id", "create_space_scratch_instruction", step2DefaultPage.getInstructionId());
      assertNull("step 2 default title should be null", step2DefaultPage.getTitle());
      assertNull("step 2 default description should be null", step2DefaultPage.getDescription());
      List<ConditionalPageConfig> conditionalPages = step2.getConditionalPages();
      assertEquals("number of conditional pages for step 2", 1, conditionalPages.size());
      ConditionalPageConfig step2CondPage = conditionalPages.get(0);
      assertNotNull("step 2 cond page should not be null", step2CondPage);
      assertEquals("step 2 conditon", "#{AdvancedSpaceWizard.createFrom == 'template'}", step2CondPage.getCondition());
      assertEquals("step 2 cond page", "/jsp/wizard/new-space/from-template.jsp", step2CondPage.getPath());
      assertEquals("step 2 cond title-id", "create_space_template_title", step2CondPage.getTitleId());
      assertEquals("step 2 cond description-id", "create_space_template_desc", step2CondPage.getDescriptionId());
      assertEquals("step 2 cond instruction-id", "create_space_template_instruction", step2CondPage.getInstructionId());
      assertNull("step 2 cond title should be null", step2CondPage.getTitle());
      assertNull("step 2 cond description should be null", step2CondPage.getDescription());
      
      // check step 3 data
      StepConfig step3 = steps.get(2);
      assertEquals("step 3 name", "summary", step3.getName());
      assertEquals("step 3 title-id", "summary", step3.getTitleId());
      assertFalse("step 3 should not have conditional pages", step3.hasConditionalPages());
      PageConfig step3Page = step3.getDefaultPage();
      assertNotNull("step3Page should not be null", step3Page);
      assertEquals("step 3 page", "/jsp/wizard/new-space/summary.jsp", step3Page.getPath());
      assertEquals("step 3 title-id", "create_space_summary_title", step3Page.getTitleId());
      assertEquals("step 3 description-id", "create_space_summary_desc", step3Page.getDescriptionId());
      assertEquals("step 3 instruction-id", "create_space_summary_instruction", step3Page.getInstructionId());
      assertNull("step 3 title should be null", step3Page.getTitle());
      assertNull("step 3 description should be null", step3Page.getDescription());
   }
   
   public void testActions()
   {
      // setup the config service
      String configFile = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFile));
      svc.init();
      
      // get the "Actions" config
      Config cfg = svc.getGlobalConfig();
      assertNotNull("cfg should not be null", cfg);   
      
      // get the <actions> config element
      ActionsConfigElement actionsConfig = (ActionsConfigElement)cfg.
            getConfigElement(ActionsConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("actions config element should not be null", actionsConfig);
      
      // get the individual actions
      ActionDefinition docDetails = actionsConfig.getActionDefinition("details_doc");
      assertNotNull("details_doc action definition should not be null", docDetails);
      assertEquals("details_doc action", "dialog:showDocDetails", docDetails.Action);
      
      ActionDefinition spaceDetails = actionsConfig.getActionDefinition("details_space");
      assertNotNull("details_space action definition should not be null", spaceDetails);
      assertEquals("details_space action", "dialog:showSpaceDetails", spaceDetails.Action);
      
      // get the action group
      ActionGroup group = actionsConfig.getActionGroup("document_browse");
      assertNotNull("group definition should not be null", group);
      assertFalse("showLink for document_browse group should be false", group.ShowLink);
      for (String actionId : group)
      {
         if (actionId.equals("details_doc") == false &&
             actionId.equals("details_space") == false)
         {
            fail("Unrecognised action-id '" + actionId + "' in action group '" + group.getId() + "'");
         }
      }
   }
   
   public void testActionsOverriding()
   {
      // setup the config service
      List<String> configFiles = new ArrayList<String>(2);
      configFiles.add(getResourcesDir() + "test-config.xml");
      configFiles.add(getResourcesDir() + "test-config-override.xml");
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the "Actions" config
      Config cfg = svc.getConfig("Actions Override");
      assertNotNull("cfg should not be null", cfg);
      
      // get the <actions> config element
      ActionsConfigElement actionsConfig = (ActionsConfigElement)cfg.
            getConfigElement(ActionsConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("actions config element should not be null", actionsConfig);
      
      // get the individual actions
      ActionDefinition docDetails = actionsConfig.getActionDefinition("details_doc");
      assertNotNull("details_doc action definition should not be null", docDetails);
      assertEquals("details_doc action", "dialog:showCustomDocDetails", docDetails.Action);
      
      ActionDefinition spaceDetails = actionsConfig.getActionDefinition("details_space");
      assertNotNull("details_space action definition should not be null", spaceDetails);
      assertEquals("details_space action", "dialog:showSpaceDetails", spaceDetails.Action);
      
      ActionDefinition customAction = actionsConfig.getActionDefinition("custom_action");
      assertNotNull("custom_action action definition should not be null", customAction);
      assertEquals("custom_action action", "customAction", customAction.Action);
      
      // get the document_browse action group
      ActionGroup group = actionsConfig.getActionGroup("document_browse");
      assertNotNull("group definition should not be null", group);
      assertTrue("showLink for document_browse group should be true", group.ShowLink);
      assertEquals("document_browse group style class", "inlineAction", group.StyleClass);
      assertNull("Style for document_browse group should be null", group.Style);
      
      // make sure there are 2 items (as one was hidden in the override)
      ArrayList<String> actions = new ArrayList<String>(3);
      for (String actionId : group)
      {
         actions.add(actionId);
      }
      
      assertEquals("number of items in document_browse group", 2, actions.size());
      
      // make sure they are in the correct order
      assertEquals("first action", "details_doc", actions.get(0));
      assertEquals("second action", "custom_action", actions.get(1));
      
      // get the new_group action group
      ActionGroup newGroup = actionsConfig.getActionGroup("new_group");
      assertNotNull("new_group definition should not be null", newGroup);
      
      // make sure there is only 1 item and it's id is correct
      actions = new ArrayList<String>(1);
      for (String actionId : newGroup)
      {
         actions.add(actionId);
      }
      
      assertEquals("number of items in new_group group", 1, actions.size());
      assertEquals("action", "custom_action", actions.get(0));
   }
}
