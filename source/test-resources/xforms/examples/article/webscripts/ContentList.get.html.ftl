<?xml version="1.0"?>
<#if results??>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:article="http://www.alfresco.org/alfresco/article"
	targetNamespace="http://www.alfresco.org/alfresco/article"
	xmlns:alf="http://www.alfresco.org"	
	elementFormDefault="qualified">
	<xs:simpleType name="content-list">
		  <xs:restriction base="xs:string">
			<#list results as result>
				<#if ! result.hasAspect("{http://www.alfresco.org/model/wcmappmodel/1.0}rendition")>
					<xs:enumeration value="${result.displayPath?substring(result.displayPath?index_of("/www/avm_webapps/ROOT")+21)}/${result.name}">
						<xs:annotation>
						  <xs:appinfo>
							<alf:label>${result.name?substring(0,result.name?index_of(".xml"))}</alf:label>
						  </xs:appinfo>
						</xs:annotation>
					</xs:enumeration>
				</#if>	
			</#list>
		  </xs:restriction>
		</xs:simpleType>
	 </xs:schema>
 </#if>

