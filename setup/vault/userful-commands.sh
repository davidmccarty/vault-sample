#!/bin/bash

###########################################################
## NOTE - this file is just a list of useful commands    ##
##         It is not meant to be executed                ##
###########################################################

# start server
cd ./setup/vault
vault server -config=vault.hcl

# init vault with single seal key - only needed first time you start vault
export VAULT_ADDR='http://127.0.0.1:8200'
vault operator init -key-shares=1 -key-threshold=1

# example output
#  Unseal Key 1: vMWCdO8YiTYPEQdNQljjGvJR4DhkT/9d3GAF2W8jgOg=
#  Initial Root Token: s.Q106iASbvn11NXJNKjj10YCZ

# unseal vault using the token and key from previous command
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN=s.Q106iASbvn11NXJNKjj10YCZ
vault operator unseal vMWCdO8YiTYPEQdNQljjGvJR4DhkT/9d3GAF2W8jgOg=
