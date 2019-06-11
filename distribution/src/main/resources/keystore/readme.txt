See https://wiki.alfresco.com/wiki/Data_Encryption and https://wiki.alfresco.com/wiki/Alfresco_And_SOLR.

keystore is the secret key keystore, containing the secret key used to encrypt and decrypt node properties.
ssl.keystore is the repository keystore, containing the repository private/public key pair and certificate.
ssl.truststore is the repository truststore, containing certificates that the repository trusts.

browser.p12 is a pkcs12 keystore generated from ssl.keystore that contains the repository private key and certificate for use in browsers such as Firefox.