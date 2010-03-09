<?xml version="1.0"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:alf="http://www.alfresco.org"
           xmlns:wsimp="http://www.alfresco.org/alfresco/webscriptimported"
           targetNamespace="http://www.alfresco.org/alfresco/webscriptimported"
           elementFormDefault="qualified">

  <xs:simpleType name="NonEmptyNormalizedStringType">
    <xs:restriction base="xs:normalizedString">
      <xs:minLength value="1"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:complexType name="WebScriptImportExampleType">
    <xs:sequence>
      <xs:element name="Id"      type="xs:nonNegativeInteger"              nillable="false" minOccurs="1" maxOccurs="1" fixed="${idValue}" />
      <xs:element name="StoreId" type="wsimp:NonEmptyNormalizedStringType" nillable="false" minOccurs="1" maxOccurs="1" fixed="${storeid}" />
      <xs:element name="Title"   type="wsimp:NonEmptyNormalizedStringType" nillable="false" minOccurs="1" maxOccurs="1" />
      <xs:element name="Summary" type="wsimp:NonEmptyNormalizedStringType" nillable="false" minOccurs="0" maxOccurs="1" />
      <xs:element name="Keyword" type="wsimp:NonEmptyNormalizedStringType" nillable="false" minOccurs="0" maxOccurs="unbounded" />
      <xs:element name="Body"    type="xs:string"                          nillable="false" minOccurs="1" maxOccurs="1" />
    </xs:sequence>
  </xs:complexType>

</xs:schema>
