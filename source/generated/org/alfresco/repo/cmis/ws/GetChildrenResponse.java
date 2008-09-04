
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getChildrenResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getChildrenResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="children" type="{http://www.cmis.org/ns/1.0}childrenType"/>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}hasMoreItems"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "children",
    "hasMoreItems"
})
@XmlRootElement(name = "getChildrenResponse")
public class GetChildrenResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected ChildrenType children;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected boolean hasMoreItems;

    /**
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ChildrenType }
     *     
     */
    public ChildrenType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildrenType }
     *     
     */
    public void setChildren(ChildrenType value) {
        this.children = value;
    }

    /**
     * Gets the value of the hasMoreItems property.
     * 
     */
    public boolean isHasMoreItems() {
        return hasMoreItems;
    }

    /**
     * Sets the value of the hasMoreItems property.
     * 
     */
    public void setHasMoreItems(boolean value) {
        this.hasMoreItems = value;
    }

}
