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
#  Unseal Key 1: 1UZjZw//ziUtafWl4D0vO3ymvx5Tl+lPTUllc1mjxSk=
#  Initial Root Token: s.0fAt8ADVNewUCukQdPbDl5qa

# unseal vault using the token and key from previous command
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN=s.0fAt8ADVNewUCukQdPbDl5qa
vault operator unseal 1UZjZw//ziUtafWl4D0vO3ymvx5Tl+lPTUllc1mjxSk=

# config
vault secrets enable -path=secret/ kv
vault secrets enable transit
vault write -f transit/keys/vault-sample
