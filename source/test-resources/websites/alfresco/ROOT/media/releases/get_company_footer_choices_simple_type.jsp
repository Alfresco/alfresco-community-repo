<!--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

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
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"

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
             xmlns:alf="http://www.alfresco.org"
  	     elementFormDefault="qualified">
    <xs:simpleType name="company_footer_choices">
      <xs:restriction base="xs:normalizedString">
	<!-- call into CompanyFooterBean to retrieve all company footers -->
        <c:forEach items="${pr:getCompanyFooterChoices(pageContext)}" var="companyFooter">
          <jsp:element name="xs:enumeration">
	    <!-- this is the file name of the company footer -->
	    <jsp:attribute name="value"><c:out value="${companyFooter.fileName}"/></jsp:attribute>
            <jsp:body>
  	      <xs:annotation>
	        <xs:appinfo>
		  <!-- this produces the label displayed in the combobox within the press release form -->
                  <alf:label><c:out value="${companyFooter.name}"/></alf:label>
		</xs:appinfo>
              </xs:annotation>
            </jsp:body>
	  </jsp:element>
        </c:forEach>
      </xs:restriction>
    </xs:simpleType>
  </xs:schema>
</jsp:root>
