<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:alfresco="http://www.alfresco.org/alfresco"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
	  xmlns:pr="http://www.alfresco.org/pr">
  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>
  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  	   elementFormDefault="qualified">
    <xs:simpleType name="company-footer">
      <xs:restriction base="xs:string">
        <c:forEach items="${pr:getCompanyFooters(pageContext)}" var="companyFooter">
          <jsp:element name="xs:enumeration">
	    <jsp:attribute name="value"><c:out value="${companyFooter.href}"/></jsp:attribute>
            <jsp:body>
  	      <xs:annotation>
	        <xs:appinfo>
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