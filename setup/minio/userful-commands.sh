#!/bin/bash

###########################################################
## NOTE - this file is just a list of useful commands    ##
##         It is not meant to be executed                ##
###########################################################

# start minio
cd ./setup/minio
export MINIO_ROOT_USER=admin
export MINIO_ROOT_PASSWORD=passw0rd
export MINIO_KMS_KES_ENDPOINT=https://play.min.io:7373
export MINIO_KMS_KES_KEY_FILE=root.key
export MINIO_KMS_KES_CERT_FILE=root.cert
export MINIO_KMS_KES_KEY_NAME=my-minio-key
minio server ./data --address "127.0.0.1:9000" -console-address "127.0.0.1:9001"

## save config from generated messages
# API: http://127.0.0.1:9000
# RootUser: admin
# RootPass: passw0rd
# Console: http://127.0.0.1:9001
# RootUser: admin
# RootPass: passw0rd
#  $ mc alias set myminio http://127.0.0.1:9000 admin passw0rd

# set alias using values from startup
mc alias set minio-local http://127.0.0.1:9000 admin passw0rd --api s3v4
