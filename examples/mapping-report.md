# Alfresco to Nuxeo Mapping Report

## Model Information
- **Alfresco Model**: custom:invoiceModel
- **Model Version**: 1.0
- **Mapping Date**: 2025-11-26
- **Mapper Version**: 1.0

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Document Types | 2 |
| Aspects | 2 |
| Schemas | 4 |
| Facets | 3 |
| Namespaces | 1 |
| Warnings | 3 |

---

## Document Type Mappings

### 1. invoice:invoice → invoice:invoice (Nuxeo)

**Alfresco Type**: `invoice:invoice`  
**Nuxeo Document Type**: `invoice:invoice`  
**Parent Type**: `cm:content` → `File`

#### Properties Mapped (8 fields):

| Alfresco Property | Nuxeo Field | Type Mapping | Required | Multi-valued |
|-------------------|-------------|--------------|----------|--------------|
| `invoice:invoiceNumber` | `invoiceNumber` | d:text → string | Yes | No |
| `invoice:invoiceDate` | `invoiceDate` | d:date → date | Yes | No |
| `invoice:dueDate` | `dueDate` | d:date → date | No | No |
| `invoice:amount` | `amount` | d:double → double | Yes | No |
| `invoice:currency` | `currency` | d:text → string | Yes | No |
| `invoice:customerName` | `customerName` | d:text → string | Yes | No |
| `invoice:status` | `status` | d:text → string | Yes | No |
| `invoice:notes` | `notes` | d:text → string[] | No | Yes |

#### Constraints Mapped:

- **currency**: LIST constraint → Allowed values: USD, EUR, GBP, JPY
- **status**: LIST constraint → Allowed values: Draft, Sent, Paid, Overdue, Cancelled

#### Mandatory Aspects:

| Alfresco Aspect | Nuxeo Facet | Schema Included |
|-----------------|-------------|-----------------|
| `cm:versionable` | `cm:versionable` | - |
| `invoice:paymentInfo` | `invoice:paymentInfo` | `invoice_paymentInfo` |

#### Associations:

| Alfresco Association | Nuxeo Relation | Type | Cardinality |
|---------------------|----------------|------|-------------|
| `invoice:relatedDocuments` | `invoice_relatedDocuments_relation` | Peer | Many-to-Many |

---

### 2. invoice:purchaseOrder → invoice:purchaseOrder (Nuxeo)

**Alfresco Type**: `invoice:purchaseOrder`  
**Nuxeo Document Type**: `invoice:purchaseOrder`  
**Parent Type**: `cm:content` → `File`

#### Properties Mapped (2 fields):

| Alfresco Property | Nuxeo Field | Type Mapping | Required | Multi-valued |
|-------------------|-------------|--------------|----------|--------------|
| `invoice:poNumber` | `poNumber` | d:text → string | Yes | No |
| `invoice:poDate` | `poDate` | d:date → date | Yes | No |

---

## Aspect Mappings

### 1. invoice:paymentInfo → Facet + Schema

**Alfresco Aspect**: `invoice:paymentInfo`  
**Nuxeo Facet**: `invoice:paymentInfo`  
**Nuxeo Schema**: `invoice_paymentInfo`

#### Properties Mapped (3 fields):

| Alfresco Property | Nuxeo Field | Type Mapping | Required |
|-------------------|-------------|--------------|----------|
| `invoice:paymentMethod` | `paymentMethod` | d:text → string | No |
| `invoice:paymentDate` | `paymentDate` | d:datetime → date | No |
| `invoice:paymentReference` | `paymentReference` | d:text → string | No |

#### Constraints:
- **paymentMethod**: LIST constraint → Allowed values: Credit Card, Bank Transfer, Cash, Check

---

### 2. invoice:approvable → Facet + Schema

**Alfresco Aspect**: `invoice:approvable`  
**Nuxeo Facet**: `invoice:approvable`  
**Nuxeo Schema**: `invoice_approvable`

#### Properties Mapped (3 fields):

| Alfresco Property | Nuxeo Field | Type Mapping | Required | Default |
|-------------------|-------------|--------------|----------|---------|
| `invoice:approver` | `approver` | d:text → string | No | - |
| `invoice:approvalDate` | `approvalDate` | d:datetime → date | No | - |
| `invoice:approved` | `approved` | d:boolean → boolean | No | false |

---

## Namespace Mappings

| Alfresco URI | Prefix | Usage |
|--------------|--------|-------|
| `http://example.com/model/invoice/1.0` | `invoice` | Custom invoice model |
| `http://www.alfresco.org/model/content/1.0` | `cm` | Core Alfresco content model |
| `http://www.alfresco.org/model/dictionary/1.0` | `d` | Alfresco data types |

---

## Data Type Conversion Summary

| Alfresco Type | Nuxeo Type | Occurrences | Data Loss? |
|---------------|------------|-------------|------------|
| d:text | string | 9 | No |
| d:date | date | 5 | No |
| d:datetime | date | 2 | No |
| d:double | double | 1 | No |
| d:boolean | boolean | 1 | No |

---

## Warnings and Recommendations

### Warnings (3):

1. **Aspect 'invoice:paymentInfo'**: Nuxeo facets are less dynamic than Alfresco aspects. Consider pre-defining facets on document types.

2. **Aspect 'invoice:approvable'**: Nuxeo facets are less dynamic than Alfresco aspects. Consider pre-defining facets on document types.

3. **Peer association 'invoice:relatedDocuments'**: Nuxeo relations are implemented separately from document hierarchy. Querying may differ from Alfresco.

### Recommendations:

1. **Facet Application**: Pre-define all facets on document types during migration to avoid runtime application issues.

2. **Association Queries**: Update queries that traverse peer associations to use Nuxeo's relation query syntax.

3. **Testing**: Test all LIST constraints and validation rules in Nuxeo to ensure they behave as expected.

4. **Indexing**: Review and configure indexing for the `invoiceNumber` field to match Alfresco's indexed configuration.

5. **Multi-valued Fields**: Verify that multi-valued field behavior (e.g., `notes`) matches Alfresco's behavior in Nuxeo.

---

## Implementation Notes

### Schema Naming Convention
- Schemas follow the pattern: `{prefix}_{localName}`
- Example: `invoice:paymentInfo` → `invoice_paymentInfo`

### Facet-Schema Relationship
- Each aspect with properties maps to both a facet and a schema
- The facet references the schema
- Document types include both the facet and schema

### Parent Type Mapping
- `cm:content` → `File`
- `cm:folder` → `Folder`
- `cm:cmobject` → `Document`

### Constraint Conversion
- LIST constraints → Nuxeo vocabulary or enum validation
- REGEX constraints → Pattern validators
- MINMAX constraints → Range validators

---

## Next Steps

1. **Review Mapping**: Review this mapping with domain experts
2. **Test Schema**: Deploy schemas to Nuxeo test environment
3. **Data Migration**: Plan data migration strategy based on this mapping
4. **Query Update**: Update application queries for Nuxeo syntax
5. **Validation**: Implement and test validation rules in Nuxeo

---

**Generated by**: Alfresco to Nuxeo Data Model Mapper v1.0  
**Report Date**: 2025-11-26
