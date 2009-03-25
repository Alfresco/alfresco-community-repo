<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
		 xmlns:article="http://www.alfresco.org/alfresco/article"
  		 elementFormDefault="qualified">
	<xs:simpleType name="catgory-list">
	  <xs:restriction base="xs:string">
		<xs:enumeration value="Pressrelease">
			<xs:annotation>
			  <xs:appinfo>
				<alfresco:label>Press Release</alfresco:label>
			  </xs:appinfo>
			</xs:annotation>
		</xs:enumeration>
		<xs:enumeration value="Whitepaper">
			<xs:annotation>
			  <xs:appinfo>
				<alfresco:label>Whitepaper</alfresco:label>
			  </xs:appinfo>
			</xs:annotation>
		</xs:enumeration>
	  </xs:restriction>
	</xs:simpleType>
 </xs:schema>

