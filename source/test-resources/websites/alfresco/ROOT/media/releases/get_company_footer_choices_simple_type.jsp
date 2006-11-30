<!--
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

Produces an xml schema simpleType definition which lists all company footers in /media/releases/content
as an enumerated type.  This is intended to be included within a schema (such as press-release.xsd)
which wants to update the list of available company footers dynamically.
-->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
	  xmlns:pr="http://www.alfresco.org/alfresco/pr">   
  <!-- xmlns:pr is mapped to /WEB-INF/pr.tld by web.xml -->
  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <!--
  The expected output is in the form:
  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
             xmlns:alfresco="http://www.alfresco.org/alfresco"
  	   elementFormDefault="qualified">
    <xs:simpleType name="company_footer_choices">
      <xs:restriction base="xs:string">
        <xs:enumeration value="company_footer_1.xml">
  	<xs:annotation>
  	  <xs:appinfo>
  	    <alfresco:label>Company Footer 1 Name</alfresco:label>
  	  </xs:appinfo>
  	</xs:annotation>
        </xs:enumeration>
        <xs:enumeration value="company_footer_2.xml">
  	<xs:annotation>
  	  <xs:appinfo>
  	    <alfresco:label>Company Footer 2 Name</alfresco:label>
  	  </xs:appinfo>
  	</xs:annotation>
        </xs:enumeration>
      </xs:restriction>
    </xs:simpleType>
  </xs:schema>
  -->
  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
             xmlns:alfresco="http://www.alfresco.org/alfresco"
  	     elementFormDefault="qualified">
    <xs:simpleType name="company_footer_choices">
      <xs:restriction base="xs:string">
	<!-- call into CompanyFooterBean to retrieve all company footers -->
        <c:forEach items="${pr:getCompanyFooterChoices(pageContext)}" var="companyFooter">
          <jsp:element name="xs:enumeration">
	    <!-- this is the file name of the company footer -->
	    <jsp:attribute name="value"><c:out value="${companyFooter.fileName}"/></jsp:attribute>
            <jsp:body>
  	      <xs:annotation>
	        <xs:appinfo>
		  <!-- this produces the label displayed in the combobox within the press release form -->
                  <alfresco:label><c:out value="${companyFooter.name}"/></alfresco:label>
		</xs:appinfo>
              </xs:annotation>
            </jsp:body>
	  </jsp:element>
        </c:forEach>
      </xs:restriction>
    </xs:simpleType>
  </xs:schema>
</jsp:root>
