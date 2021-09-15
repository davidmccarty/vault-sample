#!/bin/bash

###########################################################
## NOTE - this file is just a list of useful commands    ##
##         It is not meant to be executed                ##
###########################################################

# init vault with single seal key
export VAULT_ADDR='http://127.0.0.1:8200'
vault operator init -key-shares=1 -key-threshold=1

# example output
#  Unseal Key 1: 0EQ7g7swMGLMw+qs7dEoO87xc3HJeG1zqQJYKNBZP2A=
#  Initial Root Token: s.GLZzM7RuuUGzvC5vXOPbb452

# unseal vault using the token and key from previous command
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN=s.GLZzM7RuuUGzvC5vXOPbb452
vault operator unseal 0EQ7g7swMGLMw+qs7dEoO87xc3HJeG1zqQJYKNBZP2A=
