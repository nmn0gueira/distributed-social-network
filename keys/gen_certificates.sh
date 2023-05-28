#!/bin/bash

# Function to generate keystore and truststore
generate_keystore_and_truststore() {
  local alias="$1"
  local keystore_password="$2"
  local truststore_password="$3"
  local keystore_file="$4"
  local truststore_file="$5"
  local certificate_file="$6"

  echo "Generating keystore and truststore for $alias"

  # Generate keystore with SAN DNS name
  keytool -genkey -alias "$alias" -keyalg RSA -validity 365 -keystore "$keystore_file" -storetype pkcs12 -ext SAN=dns:"$server" << EOF
$keystore_password
$keystore_password
$alias
TP2
SD2223
LX
LX
PT
yes
$keystore_password
$keystore_password
EOF

  # Export certificate
  echo "Exporting certificate for $alias"
  keytool -exportcert -alias "$alias" -keystore "$keystore_file" -file "$certificate_file" << EOF
$keystore_password
EOF

  # Import certificate into truststore
  echo"Creating Client Truststore"
  cp cacerts "$truststore_file"
  echo "Importing certificate into truststore for $alias"
  keytool -importcert -file "$certificate_file" -alias "$alias" -keystore "$truststore_file" << EOF
$truststore_password
yes
EOF

  echo "Generated keystore and truststore for $alias"
  echo
}

# Clean up existing keystores and truststores
rm -f *.jks

# Define an array of server names
servers=("users0-ourorg0" "feeds0-ourorg0" "feeds1-ourorg0" "feeds2-ourorg0"
         "users0-ourorg1" "feeds0-ourorg1" "feeds1-ourorg1" "feeds2-ourorg1"
         "users0-ourorg2" "feeds0-ourorg2" "feeds1-ourorg2" "feeds2-ourorg2")

# Iterate over the array using a for loop
for server in "${servers[@]}"
do
  # Generate keystore and truststore for each server
  generate_keystore_and_truststore "$server" "password" "changeit" "$server.jks" "$server-ts.jks" "$server.cert" "$server"
done

echo "All keystores and truststores generated successfully."
