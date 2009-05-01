
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for cmisAllowableActionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisAllowableActionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="canDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canUpdateProperties" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetProperties" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetRelationships" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetParents" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetFolderParent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetDescendants" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canMove" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canDeleteVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canDeleteContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCheckout" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCancelCheckout" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCheckin" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canSetContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetAllVersions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canAddToFolder" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canRemoveFromFolder" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canViewContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canAddPolicy" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetAppliedPolicies" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canRemovePolicy" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canGetChildren" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCreateDocument" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCreateFolder" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCreateRelationship" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canCreatePolicy" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="canDeleteTree" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/ns/cmis/core/200901}cmisUndefinedAttribute"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisAllowableActionsType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", propOrder = {
    "parentId",
    "parentUrl",
    "canDelete",
    "canUpdateProperties",
    "canGetProperties",
    "canGetRelationships",
    "canGetParents",
    "canGetFolderParent",
    "canGetDescendants",
    "canMove",
    "canDeleteVersion",
    "canDeleteContent",
    "canCheckout",
    "canCancelCheckout",
    "canCheckin",
    "canSetContent",
    "canGetAllVersions",
    "canAddToFolder",
    "canRemoveFromFolder",
    "canViewContent",
    "canAddPolicy",
    "canGetAppliedPolicies",
    "canRemovePolicy",
    "canGetChildren",
    "canCreateDocument",
    "canCreateFolder",
    "canCreateRelationship",
    "canCreatePolicy",
    "canDeleteTree",
    "any"
})
public class CmisAllowableActionsType {

    protected String parentId;
    protected String parentUrl;
    protected Boolean canDelete;
    protected Boolean canUpdateProperties;
    protected Boolean canGetProperties;
    protected Boolean canGetRelationships;
    protected Boolean canGetParents;
    protected Boolean canGetFolderParent;
    protected Boolean canGetDescendants;
    protected Boolean canMove;
    protected Boolean canDeleteVersion;
    protected Boolean canDeleteContent;
    protected Boolean canCheckout;
    protected Boolean canCancelCheckout;
    protected Boolean canCheckin;
    protected Boolean canSetContent;
    protected Boolean canGetAllVersions;
    protected Boolean canAddToFolder;
    protected Boolean canRemoveFromFolder;
    protected Boolean canViewContent;
    protected Boolean canAddPolicy;
    protected Boolean canGetAppliedPolicies;
    protected Boolean canRemovePolicy;
    protected Boolean canGetChildren;
    protected Boolean canCreateDocument;
    protected Boolean canCreateFolder;
    protected Boolean canCreateRelationship;
    protected Boolean canCreatePolicy;
    protected Boolean canDeleteTree;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the parentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the value of the parentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentId(String value) {
        this.parentId = value;
    }

    /**
     * Gets the value of the parentUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentUrl() {
        return parentUrl;
    }

    /**
     * Sets the value of the parentUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentUrl(String value) {
        this.parentUrl = value;
    }

    /**
     * Gets the value of the canDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanDelete() {
        return canDelete;
    }

    /**
     * Sets the value of the canDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanDelete(Boolean value) {
        this.canDelete = value;
    }

    /**
     * Gets the value of the canUpdateProperties property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanUpdateProperties() {
        return canUpdateProperties;
    }

    /**
     * Sets the value of the canUpdateProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanUpdateProperties(Boolean value) {
        this.canUpdateProperties = value;
    }

    /**
     * Gets the value of the canGetProperties property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetProperties() {
        return canGetProperties;
    }

    /**
     * Sets the value of the canGetProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetProperties(Boolean value) {
        this.canGetProperties = value;
    }

    /**
     * Gets the value of the canGetRelationships property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetRelationships() {
        return canGetRelationships;
    }

    /**
     * Sets the value of the canGetRelationships property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetRelationships(Boolean value) {
        this.canGetRelationships = value;
    }

    /**
     * Gets the value of the canGetParents property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetParents() {
        return canGetParents;
    }

    /**
     * Sets the value of the canGetParents property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetParents(Boolean value) {
        this.canGetParents = value;
    }

    /**
     * Gets the value of the canGetFolderParent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetFolderParent() {
        return canGetFolderParent;
    }

    /**
     * Sets the value of the canGetFolderParent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetFolderParent(Boolean value) {
        this.canGetFolderParent = value;
    }

    /**
     * Gets the value of the canGetDescendants property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetDescendants() {
        return canGetDescendants;
    }

    /**
     * Sets the value of the canGetDescendants property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetDescendants(Boolean value) {
        this.canGetDescendants = value;
    }

    /**
     * Gets the value of the canMove property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanMove() {
        return canMove;
    }

    /**
     * Sets the value of the canMove property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanMove(Boolean value) {
        this.canMove = value;
    }

    /**
     * Gets the value of the canDeleteVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanDeleteVersion() {
        return canDeleteVersion;
    }

    /**
     * Sets the value of the canDeleteVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanDeleteVersion(Boolean value) {
        this.canDeleteVersion = value;
    }

    /**
     * Gets the value of the canDeleteContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanDeleteContent() {
        return canDeleteContent;
    }

    /**
     * Sets the value of the canDeleteContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanDeleteContent(Boolean value) {
        this.canDeleteContent = value;
    }

    /**
     * Gets the value of the canCheckout property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCheckout() {
        return canCheckout;
    }

    /**
     * Sets the value of the canCheckout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCheckout(Boolean value) {
        this.canCheckout = value;
    }

    /**
     * Gets the value of the canCancelCheckout property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCancelCheckout() {
        return canCancelCheckout;
    }

    /**
     * Sets the value of the canCancelCheckout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCancelCheckout(Boolean value) {
        this.canCancelCheckout = value;
    }

    /**
     * Gets the value of the canCheckin property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCheckin() {
        return canCheckin;
    }

    /**
     * Sets the value of the canCheckin property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCheckin(Boolean value) {
        this.canCheckin = value;
    }

    /**
     * Gets the value of the canSetContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanSetContent() {
        return canSetContent;
    }

    /**
     * Sets the value of the canSetContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanSetContent(Boolean value) {
        this.canSetContent = value;
    }

    /**
     * Gets the value of the canGetAllVersions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetAllVersions() {
        return canGetAllVersions;
    }

    /**
     * Sets the value of the canGetAllVersions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetAllVersions(Boolean value) {
        this.canGetAllVersions = value;
    }

    /**
     * Gets the value of the canAddToFolder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanAddToFolder() {
        return canAddToFolder;
    }

    /**
     * Sets the value of the canAddToFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanAddToFolder(Boolean value) {
        this.canAddToFolder = value;
    }

    /**
     * Gets the value of the canRemoveFromFolder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanRemoveFromFolder() {
        return canRemoveFromFolder;
    }

    /**
     * Sets the value of the canRemoveFromFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanRemoveFromFolder(Boolean value) {
        this.canRemoveFromFolder = value;
    }

    /**
     * Gets the value of the canViewContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanViewContent() {
        return canViewContent;
    }

    /**
     * Sets the value of the canViewContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanViewContent(Boolean value) {
        this.canViewContent = value;
    }

    /**
     * Gets the value of the canAddPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanAddPolicy() {
        return canAddPolicy;
    }

    /**
     * Sets the value of the canAddPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanAddPolicy(Boolean value) {
        this.canAddPolicy = value;
    }

    /**
     * Gets the value of the canGetAppliedPolicies property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetAppliedPolicies() {
        return canGetAppliedPolicies;
    }

    /**
     * Sets the value of the canGetAppliedPolicies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetAppliedPolicies(Boolean value) {
        this.canGetAppliedPolicies = value;
    }

    /**
     * Gets the value of the canRemovePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanRemovePolicy() {
        return canRemovePolicy;
    }

    /**
     * Sets the value of the canRemovePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanRemovePolicy(Boolean value) {
        this.canRemovePolicy = value;
    }

    /**
     * Gets the value of the canGetChildren property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanGetChildren() {
        return canGetChildren;
    }

    /**
     * Sets the value of the canGetChildren property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanGetChildren(Boolean value) {
        this.canGetChildren = value;
    }

    /**
     * Gets the value of the canCreateDocument property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreateDocument() {
        return canCreateDocument;
    }

    /**
     * Sets the value of the canCreateDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreateDocument(Boolean value) {
        this.canCreateDocument = value;
    }

    /**
     * Gets the value of the canCreateFolder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreateFolder() {
        return canCreateFolder;
    }

    /**
     * Sets the value of the canCreateFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreateFolder(Boolean value) {
        this.canCreateFolder = value;
    }

    /**
     * Gets the value of the canCreateRelationship property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreateRelationship() {
        return canCreateRelationship;
    }

    /**
     * Sets the value of the canCreateRelationship property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreateRelationship(Boolean value) {
        this.canCreateRelationship = value;
    }

    /**
     * Gets the value of the canCreatePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreatePolicy() {
        return canCreatePolicy;
    }

    /**
     * Sets the value of the canCreatePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreatePolicy(Boolean value) {
        this.canCreatePolicy = value;
    }

    /**
     * Gets the value of the canDeleteTree property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanDeleteTree() {
        return canDeleteTree;
    }

    /**
     * Sets the value of the canDeleteTree property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanDeleteTree(Boolean value) {
        this.canDeleteTree = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
