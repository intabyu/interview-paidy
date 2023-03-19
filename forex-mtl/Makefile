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

test_curl:
	curl http://localhost:8080
