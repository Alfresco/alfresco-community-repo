<jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>

<jsp:directive.page import="java.util.*"/>
<jsp:directive.page import="org.alfresco.web.pr.*"/>

<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	   elementFormDefault="qualified">
  <xs:simpleType name="company-footer">
    <xs:restriction base="xs:string">
<%
List<CompanyFooterBean> companyFooters = Util.getCompanyFooters(request, application);
for (CompanyFooterBean companyFooter : companyFooters)
{
%>
      <xs:enumeration value="<%= companyFooter.getHref() %>">
	<xs:annotation>
	  <xs:documentation><%= companyFooter.getName() %></xs:documentation>
	</xs:annotation>
      </xs:enumeration>
<%
}
%>
    </xs:restriction>
  </xs:simpleType>
</xs:schema>
