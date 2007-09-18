/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.forms;

import java.io.*;
import java.util.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.model.WCMAppModel;
import junit.framework.AssertionFailedError;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.events.DOMEventNames;
import org.w3c.dom.*;
import org.w3c.dom.events.*;
import org.xml.sax.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.model.*;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.util.TestWithUserUtils;
import org.apache.shale.test.mock.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * JUnit tests to exercise parts of the forms codebase
 * 
 * @author ariel backenroth
 */
public class FormsTest 
   extends BaseSpringTest
{

   /////////////////////////////////////////////////////////////////////////////
   
   private static class MockForm
      extends FormImpl
   {

      MockForm(final NodeRef folderNodeRef,
               final FormsService formsService)
      {
         super(folderNodeRef, formsService);
      }


      public void setOutputPathPattern(final String opp)
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         nodeService.setProperty(this.getNodeRef(), WCMAppModel.PROP_OUTPUT_PATH_PATTERN, opp);
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private final static Log LOGGER = LogFactory.getLog(FormsTest.class);
   private final static String WEB_CLIENT_APPLICATION_CONTEXT =
      "classpath:alfresco/web-client-application-context.xml";

   private NodeService nodeService;
   private FormsService formsService;
   private MockForm mockForm;

   protected void onSetUpInTransaction()
      throws Exception
   {
      System.err.println("onSetUpInTransaction");
      super.onSetUpInTransaction();
      this.nodeService = (NodeService)super.applicationContext.getBean("dbNodeService");
      assertNotNull(this.nodeService);
      final FileFolderService fileFolderService = (FileFolderService)
         super.applicationContext.getBean("fileFolderService");
      assertNotNull(fileFolderService);
      this.formsService = (FormsService)super.applicationContext.getBean("FormsService");
      assertNotNull(this.formsService);
      final AuthenticationService authenticationService = (AuthenticationService)
         applicationContext.getBean("authenticationService");
      authenticationService.clearCurrentSecurityContext();
      final MutableAuthenticationDao authenticationDAO = (MutableAuthenticationDao) 
         applicationContext.getBean("authenticationDao");

        // Create a workspace that contains the 'live' nodes
      final StoreRef testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, 
                                                                 "Test_" + System.currentTimeMillis());
        
      // Get a reference to the root node
      final NodeRef rootNodeRef = this.nodeService.getRootNode(testStoreRef);

      // Create an authenticate the user
      if(!authenticationDAO.userExists("admin"))
      {
         authenticationService.createAuthentication("admin", "admin".toCharArray());
      }
        
      TestWithUserUtils.authenticateUser("admin", 
                                         "admin", 
                                         rootNodeRef, 
                                         authenticationService);

      // set up a faces context
      final MockExternalContext ec = new MockExternalContext(new MockServletContext(),
                                                             new MockHttpServletRequest(),
                                                             new MockHttpServletResponse());
      final StaticWebApplicationContext ac = new StaticWebApplicationContext();
      ac.setParent(this.applicationContext);
      this.applicationContext = ac;
      ec.getApplicationMap().put(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                                 this.applicationContext);
      new MockFacesContext(ec);


      final FileInfo folderInfo =
         fileFolderService.create(rootNodeRef,
                                  "test_form",
                                  WCMAppModel.TYPE_FORMFOLDER);
      final HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
      this.nodeService.addAspect(folderInfo.getNodeRef(), 
                                 WCMAppModel.ASPECT_FORM, 
                                 props);
      this.mockForm = new MockForm(folderInfo.getNodeRef(), this.formsService);
   }

   @Override
   protected String[] getConfigLocations()
   {
      return (String[])ArrayUtils.add(super.getConfigLocations(), 
                                      WEB_CLIENT_APPLICATION_CONTEXT);
   }

   @Override 
   protected ConfigurableApplicationContext loadContext(Object key)
      throws Exception
   {
      return new ClassPathXmlApplicationContext((String[])key);
   }

   public void testOutputPathPatternForFormInstanceData()
      throws Exception
   {
      class OutputPathPatternTest
      {
         public final String expected;
         public final String pattern;
         public final Document xml;
         public final String name;
         public final String parentAVMPath;
         public final String webapp;
         
         public OutputPathPatternTest(final String expected,
                                      final String pattern,
                                      final Document xml,
                                      final String name,
                                      final String parentAVMPath,
                                      final String webapp)
         {
            this.expected = expected;
            this.pattern = pattern;
            this.xml = xml;
            this.name = name;
            this.parentAVMPath = parentAVMPath;
            this.webapp = webapp;
         }
      }

      final OutputPathPatternTest[] opps = new OutputPathPatternTest[] {
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/foo.xml",
                                   "${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/foo.xml", 
                                   "/${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/foo.xml", 
                                   "/${webapp}/${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/foo.xml", 
                                   "/${webapp}/${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/another_webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir1/dir2/foo.xml", 
                                   "/${webapp}/${cwd}/${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/another_webapp/dir1/dir2",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/" + Calendar.getInstance().get(Calendar.YEAR) + "_foo.xml", 
                                   "${date?string('yyyy')}_${name}.xml",
                                   XMLUtil.parse("<foo/>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/foo.xml", 
                                   "${xml.root_tag.name}.xml",
                                   XMLUtil.parse("<root_tag><name>foo</name></root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/07.xml", 
                                   "${xml.root_tag.date?date('yyyy-MM-dd')?string('MM')}.xml",
                                   XMLUtil.parse("<root_tag><date>1776-07-04</date></root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/foo.xml",
                                   "${xml['foons:root_tag/foons:name']}.xml",
                                   XMLUtil.parse("<foons:root_tag xmlns:foons='bar'><foons:name>foo</foons:name></foons:root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/foo.xml",
                                   "${xml[\"/*[name()='foons:root_tag']/*[name()='foons:name']\"]}.xml",
                                   XMLUtil.parse("<foons:root_tag xmlns:foons='bar'><foons:name>foo</foons:name></foons:root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest("avmstore:/www/avm_webapps/webapp/dir/foo.xml",
                                   "${xml['/foons:root_tag/foons:name']}.xml",
                                   XMLUtil.parse("<foons:root_tag xmlns:foons='bar'><foons:name>foo</foons:name></foons:root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp"),
         new OutputPathPatternTest(null,
                                   "${xml.root_tag.name}",
                                   XMLUtil.parse("<foons:root_tag xmlns:foons='bar'><foons:name>foo</foons:name></foons:root_tag>"),
                                   "foo",
                                   "avmstore:/www/avm_webapps/webapp/dir",
                                   "webapp")
      };
      for (final OutputPathPatternTest oppt : opps)
      {
         this.mockForm.setOutputPathPattern(oppt.pattern);
         if (oppt.expected == null)
         {
            try
            {
               this.mockForm.getOutputPathForFormInstanceData(oppt.xml,
                                                              oppt.name,
                                                              oppt.parentAVMPath,
                                                              oppt.webapp);
               fail("expected pattern " + oppt.pattern + " to fail");
            }
            catch (Exception e)
            {
               // expected failure
            }
         }
         else
         {
            assertEquals(oppt.pattern + " failed", 
                         oppt.expected,
                         this.mockForm.getOutputPathForFormInstanceData(oppt.xml,
                                                                        oppt.name,
                                                                        oppt.parentAVMPath,
                                                                        oppt.webapp));
         }
      }
   }
}