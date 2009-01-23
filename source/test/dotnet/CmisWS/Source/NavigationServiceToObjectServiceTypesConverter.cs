namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class NavigationServiceToObjectServiceTypesConverter: CmisTypesConverter
                         <WcfTestClient.ObjectService.cmisProperty[], WcfTestClient.NavigationService.cmisProperty[]> {
        WcfTestClient.ObjectService.cmisProperty[] CmisTypesConverter<WcfTestClient.ObjectService.cmisProperty[],
                                                                        WcfTestClient.NavigationService.cmisProperty[]>
                                        .convertProperties(WcfTestClient.NavigationService.cmisProperty[] sourceData) {

            WcfTestClient.ObjectService.cmisProperty[] result =
                                                 new WcfTestClient.ObjectService.cmisProperty[sourceData.Length];

            int index = 0;

            foreach(WcfTestClient.NavigationService.cmisProperty property in sourceData) {
                index = determineObjectServiceProperty(result, index, property);
            }

            return result;
        }

        private static int determineObjectServiceProperty(WcfTestClient.ObjectService.cmisProperty[] result,
                                                    int index, WcfTestClient.NavigationService.cmisProperty property) {

            if (property is WcfTestClient.NavigationService.cmisPropertyString) {
                result[index++] = convertToStringObjectServiceProperty(
                                                     (WcfTestClient.NavigationService.cmisPropertyString)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyBoolean) {
                result[index++] = convertToBooleanObjectServiceProperty(
                                                    (WcfTestClient.NavigationService.cmisPropertyBoolean)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyDateTime) {
                result[index++] = convertToDateTimeObjectServiceProperty(
                                                   (WcfTestClient.NavigationService.cmisPropertyDateTime)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyDecimal) {
                result[index++] = convertToDecimalObjectServiceProperty(
                                                    (WcfTestClient.NavigationService.cmisPropertyDecimal)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyId) {
                result[index++] = convertToIdObjectServiceProperty(
                                                         (WcfTestClient.NavigationService.cmisPropertyId)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyInteger) {
                result[index++] = convertToIntegerObjectServiceProperty(
                    (WcfTestClient.NavigationService.cmisPropertyInteger)property);
            }

            if (property is WcfTestClient.NavigationService.cmisPropertyUri) {
                result[index++] = convertToUriObjectServiceProperty(
                    (WcfTestClient.NavigationService.cmisPropertyUri)property);
            }

            return index;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToStringObjectServiceProperty(
                                                           WcfTestClient.NavigationService.cmisPropertyString source) {

            WcfTestClient.ObjectService.cmisPropertyString result =
                                                                  new WcfTestClient.ObjectService.cmisPropertyString();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToBooleanObjectServiceProperty(
                                                          WcfTestClient.NavigationService.cmisPropertyBoolean source) {

            WcfTestClient.ObjectService.cmisPropertyBoolean result =
                                                                  new WcfTestClient.ObjectService.cmisPropertyBoolean();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToDateTimeObjectServiceProperty(
                                                         WcfTestClient.NavigationService.cmisPropertyDateTime source) {

            WcfTestClient.ObjectService.cmisPropertyDateTime result =
                                                                  new WcfTestClient.ObjectService.cmisPropertyDateTime();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToDecimalObjectServiceProperty(
                                                          WcfTestClient.NavigationService.cmisPropertyDecimal source) {

            WcfTestClient.ObjectService.cmisPropertyDecimal result =
                                                                  new WcfTestClient.ObjectService.cmisPropertyDecimal();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToIdObjectServiceProperty(
                                                               WcfTestClient.NavigationService.cmisPropertyId source) {

            WcfTestClient.ObjectService.cmisPropertyId result = new WcfTestClient.ObjectService.cmisPropertyId();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToIntegerObjectServiceProperty(
                                                          WcfTestClient.NavigationService.cmisPropertyInteger source) {

            WcfTestClient.ObjectService.cmisPropertyInteger result =
                                                                 new WcfTestClient.ObjectService.cmisPropertyInteger();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToUriObjectServiceProperty(
                                                              WcfTestClient.NavigationService.cmisPropertyUri source) {

            WcfTestClient.ObjectService.cmisPropertyUri result = new WcfTestClient.ObjectService.cmisPropertyUri();

            result.AnyAttr = source.AnyAttr;
            result.index = source.index;
            result.name = source.name;
            result.propertyType = convertToObjectServicePropertyTypeEnum(source.propertyType);
            result.propertyTypeSpecified = source.propertyTypeSpecified;
            result.value = source.value;

            return result;
        }

        private static WcfTestClient.ObjectService.enumPropertyType convertToObjectServicePropertyTypeEnum(
                                                             WcfTestClient.NavigationService.enumPropertyType source) {

            switch (source) {
                case WcfTestClient.NavigationService.enumPropertyType.boolean: {
                    return WcfTestClient.ObjectService.enumPropertyType.boolean;
                }

                case WcfTestClient.NavigationService.enumPropertyType.datetime: {
                    return WcfTestClient.ObjectService.enumPropertyType.datetime;
                }

                case WcfTestClient.NavigationService.enumPropertyType.html: {
                    return WcfTestClient.ObjectService.enumPropertyType.html;
                }

                case WcfTestClient.NavigationService.enumPropertyType.id: {
                    return WcfTestClient.ObjectService.enumPropertyType.id;
                }

                case WcfTestClient.NavigationService.enumPropertyType.integer: {
                    return WcfTestClient.ObjectService.enumPropertyType.integer;
                }

                case WcfTestClient.NavigationService.enumPropertyType.uri: {
                    return WcfTestClient.ObjectService.enumPropertyType.uri;
                }

                case WcfTestClient.NavigationService.enumPropertyType.xml: {
                    return WcfTestClient.ObjectService.enumPropertyType.xml;
                }
            }

            return WcfTestClient.ObjectService.enumPropertyType.@decimal;
        }
    }
}
