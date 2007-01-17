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
<%-- Breadcrumb area --%>
<%-- Designed to support a variable height breadcrumb --%>
<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_1.gif" width=4 height=7></td>
   <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_2.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_3.gif" width=4 height=7></td>
</tr>

<tr>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_4.gif)"></td>
   <td bgcolor="#dfe6ed">
      <%-- Breadcrumb component --%>
      <div style="padding-left:8px" class="headbarTitle">
         <a:breadcrumb value="#{NavigationBean.location}" styleClass="headbarLink" />
      </div>
   </td>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_6.gif)"></td>
</tr>

<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_7.gif" width=4 height=10></td>
   <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_8.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_9.gif" width=4 height=10></td>
</tr>
