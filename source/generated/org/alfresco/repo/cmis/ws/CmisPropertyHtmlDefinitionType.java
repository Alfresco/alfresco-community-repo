
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyHtmlDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyHtmlDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="defaultValue" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisChoiceHtmlType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyHtmlDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "defaultValue"
})
public class CmisPropertyHtmlDefinitionType
    extends CmisPropertyDefinitionType
{

    protected List<CmisChoiceHtmlType> defaultValue;

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
     * {@link CmisChoiceHtmlType }
     * 
     * 
     */
    public List<CmisChoiceHtmlType> getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = new ArrayList<CmisChoiceHtmlType>();
        }
        return this.defaultValue;
    }

}
