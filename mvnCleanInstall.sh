#!/bin/bash
mvn clean install
cd EFTEMj-lib
mvn clean install
cd ..
cd EFTEMj-SR-EELS
mvn clean install
cd ..
