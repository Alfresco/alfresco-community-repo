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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:outputText id="txt" value="#{msg.reassign_select_user}<br/><br/>" escape="false" />

<a:genericPicker id="user-picker" showAddButton="false" filters="#{DialogManager.bean.filters}" 
                 queryCallback="#{DialogManager.bean.pickerCallback}" multiSelect="false" />

<script type="text/javascript">
   document.getElementById("dialog:dialog-body:user-picker_results").onchange = checkButtonState;
   
   function checkButtonState()
   {
      var button = document.getElementById("dialog:finish-button");
      var list = document.getElementById("dialog:dialog-body:user-picker_results");
      button.disabled = (list.selectedIndex == -1);
   }
</script>