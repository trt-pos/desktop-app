FROM ubuntu:latest
LABEL authors="borja"

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
    libgtk-3-0 \
    && apt-get clean