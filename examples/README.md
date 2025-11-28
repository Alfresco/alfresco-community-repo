# Alfresco to Nuxeo Data Model Mapping Examples

This directory contains example files demonstrating the mapping between Alfresco and Nuxeo data models.

## Files

### 1. alfresco-content-model.xml
A sample Alfresco content model XML file that defines:
- Custom types (`invoice:invoice`, `invoice:purchaseOrder`)
- Custom aspects (`invoice:paymentInfo`, `invoice:approvable`)
- Properties with various data types
- Constraints (LIST constraints for enumerated values)
- Associations (peer associations for document relationships)
- Mandatory aspects

This represents a typical invoice management content model in Alfresco.

### 2. nuxeo-document-type.json
The corresponding Nuxeo data model in JSON format, showing how the Alfresco model is mapped to:
- Nuxeo document types
- Schemas with fields
- Facets
- Relations
- Data type conversions
- Warnings about potential issues

### 3. mapping-report.md
A detailed mapping report documenting:
- Entity mappings (types, aspects, properties)
- Data type conversions
- Constraint mappings
- Association mappings
- Warnings and recommendations
- Implementation notes

## Usage

### Running the Mapper

To use the prototype mapper with the example Alfresco model:

```java
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.nuxeo.NuxeoDataModelMapper;
import org.alfresco.repo.nuxeo.config.MappingContext;

import java.io.FileInputStream;

// Load Alfresco model
FileInputStream xmlInput = new FileInputStream("examples/alfresco-content-model.xml");
M2Model alfrescoModel = M2Model.createModel(xmlInput);

// Create mapper
NuxeoDataModelMapper mapper = new NuxeoDataModelMapper();

// Perform mapping
MappingContext context = mapper.mapModel(alfrescoModel);

// Generate output
String jsonOutput = mapper.toJSON(context);
String report = mapper.generateSummaryReport(context);

// Print results
System.out.println("=== JSON Output ===");
System.out.println(jsonOutput);

System.out.println("\n=== Mapping Report ===");
System.out.println(report);

// Check warnings
for (String warning : context.getWarnings()) {
    System.out.println("WARNING: " + warning);
}
```

### Expected Output

The mapper will produce:
1. **JSON representation** of Nuxeo document types, schemas, and facets
2. **Summary report** with statistics and entity listings
3. **Warnings** about potential data loss or compatibility issues

### Key Mappings in Examples

| Alfresco | Nuxeo | Notes |
|----------|-------|-------|
| `invoice:invoice` type | `invoice:invoice` document type | Parent: File |
| `invoice:paymentInfo` aspect | `invoice:paymentInfo` facet + `invoice_paymentInfo` schema | Facet includes schema |
| `d:text` properties | `string` fields | Direct mapping |
| `d:date` properties | `date` fields | Direct mapping |
| `d:double` properties | `double` fields | Direct mapping |
| LIST constraints | Validation rules | Converted to allowed values |
| Peer associations | Relations | Separate relation graph |

## Testing

Run the test suite to verify the mapping logic:

```bash
cd data-model
mvn test -Dtest=Nuxeo*Test
```

## Next Steps

1. **Review Mappings**: Review the generated output with domain experts
2. **Customize**: Modify `mapping-config.properties` for custom mappings
3. **Extend**: Add custom mapper logic for organization-specific requirements
4. **Test**: Test with your actual Alfresco models
5. **Deploy**: Use the mapping information to plan Nuxeo schema deployment

## Documentation

For complete documentation, see:
- **Investigation Report**: `docs/investigation/ALFRESCO_NUXEO_DATA_MODEL_MAPPING.md`
- **Mapper JavaDocs**: `data-model/src/main/java/org/alfresco/repo/nuxeo/`
- **Test Cases**: `data-model/src/test/java/org/alfresco/repo/nuxeo/`

## Support

For questions or issues:
1. Review the investigation document for known limitations
2. Check the warnings in the mapping report
3. Consult Nuxeo documentation for specific features
4. Test thoroughly in a development environment before production use
