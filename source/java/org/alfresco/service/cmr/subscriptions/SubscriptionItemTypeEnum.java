package org.alfresco.service.cmr.subscriptions;

/**
 * Subscription types enum.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public enum SubscriptionItemTypeEnum
{
    USER("user");

    private String value;

    SubscriptionItemTypeEnum(String type)
    {
        value = type;
    }

    public String getValue()
    {
        return value;
    }

    public static SubscriptionItemTypeEnum fromValue(String v)
    {
        for (SubscriptionItemTypeEnum ste : SubscriptionItemTypeEnum.values())
        {
            if (ste.value.equals(v))
            {
                return ste;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
