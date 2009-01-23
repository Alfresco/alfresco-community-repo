namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class VersioningServiceToObjectServiceTypesConverter: CmisTypesConverter
                         <WcfTestClient.ObjectService.cmisProperty[], WcfTestClient.VersioningService.cmisProperty[]> {
        WcfTestClient.ObjectService.cmisProperty[] CmisTypesConverter<WcfTestClient.ObjectService.cmisProperty[],
                                                                        WcfTestClient.VersioningService.cmisProperty[]>
                                        .convertProperties(WcfTestClient.VersioningService.cmisProperty[] sourceData) {

            WcfTestClient.ObjectService.cmisProperty[] result =
                                                       new WcfTestClient.ObjectService.cmisProperty[sourceData.Length];

            int index = 0;

            foreach (WcfTestClient.VersioningService.cmisProperty property in sourceData) {
                index = determineObjectServiceProperty(result, index, property);
            }

            return result;
        }

        private static int determineObjectServiceProperty(WcfTestClient.ObjectService.cmisProperty[] result,
                                                    int index, WcfTestClient.VersioningService.cmisProperty property) {

            if (property is WcfTestClient.VersioningService.cmisPropertyString) {
                result[index++] = convertToStringObjectServiceProperty(
                                                         (WcfTestClient.VersioningService.cmisPropertyString)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyBoolean) {
                result[index++] = convertToBooleanObjectServiceProperty(
                                                        (WcfTestClient.VersioningService.cmisPropertyBoolean)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyDateTime) {
                result[index++] = convertToDateTimeObjectServiceProperty(
                                                       (WcfTestClient.VersioningService.cmisPropertyDateTime)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyDecimal) {
                result[index++] = convertToDecimalObjectServiceProperty(
                                                        (WcfTestClient.VersioningService.cmisPropertyDecimal)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyId) {
                result[index++] = convertToIdObjectServiceProperty(
                                                             (WcfTestClient.VersioningService.cmisPropertyId)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyInteger) {
                result[index++] = convertToIntegerObjectServiceProperty(
                                                        (WcfTestClient.VersioningService.cmisPropertyInteger)property);
            }

            if (property is WcfTestClient.VersioningService.cmisPropertyUri) {
                result[index++] = convertToUriObjectServiceProperty(
                                                            (WcfTestClient.VersioningService.cmisPropertyUri)property);
            }

            return index;
        }

        private static WcfTestClient.ObjectService.cmisProperty convertToStringObjectServiceProperty(
                                                           WcfTestClient.VersioningService.cmisPropertyString source) {

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
                                                          WcfTestClient.VersioningService.cmisPropertyBoolean source) {

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
                                                         WcfTestClient.VersioningService.cmisPropertyDateTime source) {

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
                                                          WcfTestClient.VersioningService.cmisPropertyDecimal source) {

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
                                                               WcfTestClient.VersioningService.cmisPropertyId source) {

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
                                                          WcfTestClient.VersioningService.cmisPropertyInteger source) {

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
                                                              WcfTestClient.VersioningService.cmisPropertyUri source) {

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
                                                             WcfTestClient.VersioningService.enumPropertyType source) {

            switch (source) {
                case WcfTestClient.VersioningService.enumPropertyType.boolean: {
                    return WcfTestClient.ObjectService.enumPropertyType.boolean;
                }

                case WcfTestClient.VersioningService.enumPropertyType.datetime: {
                    return WcfTestClient.ObjectService.enumPropertyType.datetime;
                }

                case WcfTestClient.VersioningService.enumPropertyType.html: {
                    return WcfTestClient.ObjectService.enumPropertyType.html;
                }

                case WcfTestClient.VersioningService.enumPropertyType.id: {
                    return WcfTestClient.ObjectService.enumPropertyType.id;
                }

                case WcfTestClient.VersioningService.enumPropertyType.integer: {
                    return WcfTestClient.ObjectService.enumPropertyType.integer;
                }

                case WcfTestClient.VersioningService.enumPropertyType.uri: {
                    return WcfTestClient.ObjectService.enumPropertyType.uri;
                }

                case WcfTestClient.VersioningService.enumPropertyType.xml: {
                    return WcfTestClient.ObjectService.enumPropertyType.xml;
                }
            }

            return WcfTestClient.ObjectService.enumPropertyType.@decimal;
        }
    }
}
