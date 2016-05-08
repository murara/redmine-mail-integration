FROM java:8-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
		pandoc \
	&& rm -rf /var/lib/apt/lists/*

COPY mail-integration-1.1.3.jar /mail-integration-1.1.3.jar

CMD ["java","-jar","/mail-integration-1.1.3.jar"]