package org.alfresco.repo.publishing.linkedin.springsocial.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ShareVisibilityCode
{

    @XmlEnumValue("anyone")
    ANYONE("anyone"), 
    
    @XmlEnumValue("connections-only")
    CONNECTIONS_ONLY("connections-only");
    
    private final String value;

    ShareVisibilityCode(String value)
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    public static ShareVisibilityCode fromValue(String value)
    {
        for (ShareVisibilityCode validCode : ShareVisibilityCode.values())
        {
            if (validCode.value.equals(value))
            {
                return validCode;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
