<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>

<r:page>

<f:view>
   
   <h2>Image Picker</h2>
   
   <h:form acceptCharset="UTF-8" id="imagePicker">
   
      <a:imagePickerRadio columns="4" spacing="5" value="#{DummyBean.properties.one}">
         <a:listItem value="1" label="Checkin" tooltip="Checkin"
                           image="/images/icons/check_in_large.gif" />
         <a:listItem value="2" label="Checkout" tooltip="Checkout"
                           image="/images/icons/check_out_large.gif" />
         <a:listItem value="3" label="New File" tooltip="New File"
                           image="/images/icons/large_newFile.gif" />
      </a:imagePickerRadio>
      <br/>
      <h:commandButton id="submit" value="Submit" action="#{DummyBean.submit}" />
      
      <p/>
      <a:imagePickerRadio columns="1" spacing="6" value="#{DummyBean.properties.two}" style="border: 1px solid black">
         <a:listItem value="1" tooltip="Checkin" image="/images/icons/check_in_large.gif" />
         <a:listItem value="2" tooltip="Checkout" image="/images/icons/check_out_large.gif" />
         <a:listItem value="3" tooltip="New File" image="/images/icons/large_newFile.gif" />
      </a:imagePickerRadio>
      <br/>
      <h:commandButton id="submit" value="Submit" action="#{DummyBean.submit}" />
      
      
      <p/>
      <h:commandButton id="show-zoo-page" value="Show Zoo" action="showZoo" />

   </h:form>
   
   
      
</f:view>

</r:page>