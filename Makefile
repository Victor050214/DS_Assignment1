# Makefile — Java RMI Calculator (root-level, sources in ./src)
SHELL := /bin/bash
JAVAC := javac
JAVA := java

SRC_DIR := src
OUT := out
SRC := $(wildcard $(SRC_DIR)/*.java)

.DEFAULT_GOAL := all

all: $(OUT)/.compiled
	@echo "Build OK -> $(OUT)"

$(OUT)/.compiled: $(SRC)
	@mkdir -p $(OUT)
	$(JAVAC) -d $(OUT) $(SRC)
	@touch $@

server: all
	$(JAVA) -cp $(OUT) CalculatorServer

client: all
	$(JAVA) -cp $(OUT) CalculatorClient

shared-test: all
	$(JAVA) -cp $(OUT) shared_test

private-test: all
	$(JAVA) -cp $(OUT) per_client_test

clean:
	rm -rf $(OUT)

rebuild: clean all

.PHONY: all server client shared-test private-test clean rebuild