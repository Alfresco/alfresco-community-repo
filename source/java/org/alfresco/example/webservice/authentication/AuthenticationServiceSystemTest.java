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
package org.alfresco.example.webservice.authentication;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.alfresco.util.BaseTest;

/**
 * Tests the AuthenticationService by trying to login as admin/admin and  
 * attempting to login with incorrect credentials.
 * 
 * @author gavinc
 */
public class AuthenticationServiceSystemTest extends BaseTest 
{
   private AuthenticationServiceSoapBindingStub binding;
   
   /**
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      try 
      {
         this.binding = (AuthenticationServiceSoapBindingStub)new AuthenticationServiceLocator().getAuthenticationService();
      }
      catch (ServiceException jre) 
      {
         if (jre.getLinkedCause() != null)
         {
            jre.getLinkedCause().printStackTrace();
         }
         
         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
      }
      
      assertNotNull("binding is null", this.binding);
      
      // Time out after a minute
      binding.setTimeout(60000);
   }

   /**
    * Tests whether the authentication service is working correctly
    * 
    * @throws Exception
    */
   public void testSuccessfulLogin() throws Exception 
   {
      try 
      {
         AuthenticationResult value = this.binding.startSession("admin", "admin");
         assertNotNull("result must not be null", value);
         System.out.println("ticket = " + value.getTicket());
      }
      catch (AuthenticationFault error) 
      {
         throw new AssertionFailedError("AuthenticationFault Exception caught: " + error);
      }
   }
   
   /**
    * Tests that a failed authentication attempt fails as expected
    * 
    * @throws Exception
    */
   public void testFailedLogin() throws Exception
   {
      try
      {
         AuthenticationResult result = this.binding.startSession("wrong", "credentials");
         fail("The credentials are incorrect so an AuthenticationFault should have been thrown");
      }
      catch (AuthenticationFault error) 
      {
         // we expected this
      }
   }
   
   public void testEndSession() throws Exception
   {
       this.binding.startSession("admin", "admin");
       this.binding.endSession();
   }
}
