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
import org.alfresco.config.source.FileConfigSource;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JUnit tests to exercise the capabilities added to the web client config
 * service
 * 
 * @author gavinc
 */
public class WebClientConfigTest extends BaseTest
{
   private static Log logger = LogFactory.getLog(WebClientConfigTest.class);

   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();

      logger.info("******************************************************");
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
      logger.info("propNames = " + propNames);
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
      logger.info("propNames = " + propNames);
      assertTrue("There should be 5 properties in the list", propNames.size() == 5);

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

      logger.info("propNames = " + propNames);
      assertTrue("There should be 5 properties", propNames.size() == 5);
      assertFalse("The id attribute should not be present", propsToDisplay.hasAttribute("id"));
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
      
      logger.info("propNames = " + propNames);
      assertEquals("There should be 5 properties", propNames.size() == 5, true);
      assertFalse("The id attribute should not be present", propsToDisplay.hasAttribute("id"));
   }
   
   /**
    * Tests the custom server configuration objects
    */
   public void testServerConfig()
   {
      // setup the config service
      String configFiles = getResourcesDir() + "test-config.xml";
      XMLConfigService svc = new XMLConfigService(new FileConfigSource(configFiles));
      svc.init();
      
      // get the global config and from that the server config
      ServerConfigElement serverConfig = (ServerConfigElement)svc.getGlobalConfig().
         getConfigElement(ServerConfigElement.CONFIG_ELEMENT_ID);
      assertNotNull("server config should not be null", serverConfig);
      
      String errorPage = serverConfig.getErrorPage();
      assertTrue("error page should be '/jsp/error.jsp'", errorPage.equals("/jsp/error.jsp"));
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
      assertNotNull("client config should not be null", clientConfig);
      
      List<String> views = clientConfig.getViews();
      assertEquals("There should be 2 configured views", 2, views.size());
      String renderer = views.get(1);
      assertEquals("Renderer for the icons view should be 'org.alfresco.web.ui.common.renderer.data.RichListRenderer.IconViewRenderer'", 
            "org.alfresco.web.ui.common.renderer.data.RichListRenderer.IconViewRenderer", renderer);
      
      String defaultView = clientConfig.getDefaultView("topic");
      assertEquals("Default view for topic should be 'bubble'", "bubble", defaultView);
      
      // get the defualt view for something that doesn't exist
      defaultView = clientConfig.getDefaultView("not-there");
      assertEquals("Default view for missing view should be 'details'", "details", defaultView);
      
      // get the default page size for the forum details view
      int pageSize = clientConfig.getDefaultPageSize("forum", "details");
      assertEquals("Page size for forum details should be 20", 20, pageSize);
      
      // get the defualt page size for a non existent view
      pageSize = clientConfig.getDefaultPageSize("not", "there");
      assertEquals("Page size for forum details should be 10", 10, pageSize);
      
      // get the default page size for a non existent screen and valid view
      pageSize = clientConfig.getDefaultPageSize("not-there", "icons");
      assertEquals("Page size for icons view should be 9", 9, pageSize);
      
      // test the sort column
      String column = clientConfig.getDefaultSortColumn("browse");
      assertEquals("Sort column for browse should be 'name'", "name", column);
      
      column = clientConfig.getDefaultSortColumn("topic");
      assertEquals("Sort column for topic should be 'created'", "created", column);
      
      // test the sorting direction
      boolean sortDescending = clientConfig.hasDescendingSort("browse");
      assertFalse("browse screen should use an ascending sort", sortDescending);
      
      sortDescending = clientConfig.hasDescendingSort("topic");
      assertTrue("topic screen should use a descending sort", sortDescending);
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
      
      logger.info("fromViewId = " + fromViewId);
      logger.info("fromOutcome = " + fromOutcome);
      logger.info("toViewId = " + toViewId);
      logger.info("toOutcome = " + toOutcome);
      
      assertNull(fromOutcome);
      assertNull(toOutcome);
      
      assertEquals("/jsp/browse/browse.jsp", fromViewId);
      assertEquals("/jsp/forums/forums.jsp", toViewId);
      
      // get the second child and make sure the attributes are correct,
      // from-outcome should be 'browse' and to-outcome should be 'newOutcome'
      child = children.get(1);
      fromViewId = child.getAttribute("from-view-id");
      fromOutcome = child.getAttribute("from-outcome");
      toViewId = child.getAttribute("to-view-id");
      toOutcome = child.getAttribute("to-outcome");
      
      logger.info("fromViewId = " + fromViewId);
      logger.info("fromOutcome = " + fromOutcome);
      logger.info("toViewId = " + toViewId);
      logger.info("toOutcome = " + toOutcome);
      
      assertNull(fromViewId);
      assertNull(toViewId);
      
      assertEquals("browse", fromOutcome);
      assertEquals("newOutcome", toOutcome);
   }
}
