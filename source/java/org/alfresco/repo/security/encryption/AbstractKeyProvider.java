package org.alfresco.repo.security.encryption;

import java.security.Key;

import org.alfresco.util.ParameterCheck;

/**
 * Basic support for key providers
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public abstract class AbstractKeyProvider implements KeyProvider
{
    @Override
    public Key getKey(AlfrescoKeyAlias keyAlias)
    {
        ParameterCheck.mandatory("keyAlias", keyAlias);
        return getKey(keyAlias.name());
    }
}
