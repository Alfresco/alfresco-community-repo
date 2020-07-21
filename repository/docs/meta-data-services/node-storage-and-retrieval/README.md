# Node Storage and Retrieval

## Properties

### Encrypted properties (```d:encrypted```)
Encrypted properties are stored as BLOBs in the database, but there is no additional handling for
them. In particular, the ```NodeService``` does not encrypt or decrypt them. It only guarantees
that properties of this type contain objects of type ```javax.crypto.SealedObject```. It is up to
the implementor of a custom extension to handle encryption.
The ACS provides the helper class ```MetadataEncryptor``` which provides key handling and a one-stop-shop
for encryption. But custom implementations do not need to use it.
