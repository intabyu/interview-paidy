SHELL := /bin/bash
.PHONY : all

ONE_FRAME_PORT := 8081

help:
	cat Makefile

setup:
	docker pull paidyinc/one-frame

run_one_frame:
	docker run -p $(ONE_FRAME_PORT):8080 paidyinc/one-frame

run:
	sbt run

test_curl_one_frame:
	curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:$(ONE_FRAME_PORT)/rates?pair=USDJPY&pair=USDEUR'

test_curl_proxy:
	curl -X GET 'http://localhost:8080/rates?from=USD&to=JPY'
