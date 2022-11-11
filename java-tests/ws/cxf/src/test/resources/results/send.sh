#!/bin/bash

HOST="http://pgu-dev-fednlb.test.gosuslugi.ru/config/ws/ConfigService"
#HOST="http://localhost:8080/"

curl -v -X POST "$HOST" --data "@request-jws.xml" \
  --header "Content-type: text/xml; charset=utf-8" \
  --header "Accept: text/xml, multipart/related" \
  --header 'Soapaction: "http://idecs.atc.ru/config/ws/listParameters"'
