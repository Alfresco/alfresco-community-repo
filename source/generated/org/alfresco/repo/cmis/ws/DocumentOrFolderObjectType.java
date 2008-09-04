
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentOrFolderObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentOrFolderObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/ns/1.0}documentObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}parent" minOccurs="0"/>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}children" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentOrFolderObjectType", propOrder = {
    "parent",
    "children"
})
public class DocumentOrFolderObjectType
    extends DocumentObjectType
{

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String parent;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected Children children;

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParent(String value) {
        this.parent = value;
    }

    /**
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link Children }
     *     
     */
    public Children getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link Children }
     *     
     */
    public void setChildren(Children value) {
        this.children = value;
    }

}
