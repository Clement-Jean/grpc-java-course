::!/bin/bash
:: Output files
:: ca.key: Certificate Authority private key file (this shouldn't be shared in real-life)
:: ca.crt: Certificate Authority trust certificate (this should be shared with users in real-life)
:: server.key: Server private key, password protected (this shouldn't be shared)
:: server.csr: Server certificate signing request (this should be shared with the CA owner)
:: server.crt: Server certificate signed by the CA (this would be sent back by the CA owner) - keep on server
:: server.pem: Conversion of server.key into a format gRPC likes (this shouldn't be shared)

:: Summary
:: Private files: ca.key, server.key, server.pem, server.crt
:: "Share" files: ca.crt (needed by the client), server.csr (needed by the CA)

:: Set the path where openssl.cfg is installed
set OPENSSL_CONF=D:\List\Git\grpc-replica\ssl\openssl.cfg

echo "Step 1: Generate Certificate Authority + Trust Certificate (ca.crt)"
:: Step 1: Generate Certificate Authority + Trust Certificate (ca.crt)
openssl genrsa -passout pass:1111 -des3 -out ca.key 4096
openssl req -passin pass:1111 -new -x509 -days 365 -key ca.key -out ca.crt -subj "/CN=ca"

echo "Step 2: Generate the Server Private Key (server.key)"
:: Step 2: Generate the Server Private Key (server.key)
openssl genrsa -passout pass:1111 -des3 -out server.key 4096

:: Changes these CN's = localhost to match your hosts in your environment if needed.
echo "Step 3: Get a certificate signing request from the CA (server.csr)"
:: Step 3: Get a certificate signing request from the CA (server.csr)
openssl req -passin pass:1111 -new -key server.key -out server.csr -subj "/CN=localhost"

echo "Step 4: Sign the certificate with the CA we created (it's called self signing) - server.crt"
:: Step 4: Sign the certificate with the CA we created (it's called self signing) - server.crt
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -passin pass:1111 -set_serial 01 -out server.crt

echo "Step 5"
:: Step 5: Convert the server certificate to .pem format (server.pem) - usable by gRPC
openssl pkcs8 -topk8 -nocrypt -passin pass:1111 -in server.key -out server.pem
