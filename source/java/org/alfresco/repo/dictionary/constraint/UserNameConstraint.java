package org.alfresco.repo.dictionary.constraint;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Apply constraints for user names.
 * 
 * @author andyh
 *
 */
public class UserNameConstraint extends AbstractConstraint
{
    private static final String ERR_INVALID_USERNAME = "d_dictionary.constraint.user_name.invalid_user_name";
    private static final String ERR_NON_STRING = "d_dictionary.constraint.user_name.non_string";

    @Override
    protected void evaluateSingleValue(Object value)
    {
        // ensure that the value can be converted to a String
        String checkValue = null;
        try
        {
            checkValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(ERR_NON_STRING, value);
        }
        
        AuthorityType type = AuthorityType.getAuthorityType(checkValue);
        if((type != AuthorityType.USER) && (type != AuthorityType.GUEST))
        {
            throw new ConstraintException(ERR_INVALID_USERNAME, value, type);
        }
    }
}
