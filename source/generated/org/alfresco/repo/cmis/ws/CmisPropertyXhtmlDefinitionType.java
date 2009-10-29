
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisPropertyXhtmlDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisPropertyXhtmlDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="defaultValue" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisPropertyXhtml" minOccurs="0"/>
 *         &lt;element name="choice" type="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisChoiceXhtml" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisPropertyXhtmlDefinitionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "defaultValue",
    "choice"
})
public class CmisPropertyXhtmlDefinitionType
    extends CmisPropertyDefinitionType
{

    protected CmisPropertyXhtml defaultValue;
    protected List<CmisChoiceXhtml> choice;

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link CmisPropertyXhtml }
     *     
     */
    public CmisPropertyXhtml getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisPropertyXhtml }
     *     
     */
    public void setDefaultValue(CmisPropertyXhtml value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the choice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CmisChoiceXhtml }
     * 
     * 
     */
    public List<CmisChoiceXhtml> getChoice() {
        if (choice == null) {
            choice = new ArrayList<CmisChoiceXhtml>();
        }
        return this.choice;
    }

}
