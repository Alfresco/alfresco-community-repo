
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyXmlDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyXmlDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="defaultValue" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisChoiceXmlType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="schemaURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyXmlDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "defaultValue",
    "schemaURI"
})
public class CmisPropertyXmlDefinitionType
    extends CmisPropertyDefinitionType
{

    protected List<CmisChoiceXmlType> defaultValue;
    @XmlSchemaType(name = "anyURI")
    protected String schemaURI;

    /**
     * Gets the value of the defaultValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisChoiceXmlType }
     * 
     * 
     */
    public List<CmisChoiceXmlType> getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = new ArrayList<CmisChoiceXmlType>();
        }
        return this.defaultValue;
    }

    /**
     * Gets the value of the schemaURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaURI() {
        return schemaURI;
    }

    /**
     * Sets the value of the schemaURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaURI(String value) {
        this.schemaURI = value;
    }

}
