k: clean build-push k-refresh k-logs

clean:
	mvn clean
build: clean
	quarkus build -Dquarkus.container-image.build=true
push:
	docker buildx build --platform linux/amd64,linux/arm64  -f /home/nherbaut/workspace/java-runner-analytics/scrapper-frontend/src/main/docker/Dockerfile.jvm -t nherbaut/scrapper-frontend:1.0.0-SNAPSHOT --push /home/nherbaut/workspace/java-runner-analytics/scrapper-frontend  
run:
	quarkus dev
run-docker:
	docker run -ti \
	--name scrapper \
	--rm=true \
	-e QUARKUS_DATASOURCE_DB_KIND=mysql \
	-e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://hn105529-001.eu.clouddb.ovh.net:35771/projetsl2 \
	-e QUARKUS_DATASOURCE_PASSWORD=AeGh5ag5shaixet6haed6aip8 \
	-e QUARKUS_DATASOURCE_USERNAME=projetsl2 \
	-e QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update \
	-p 8080:8080 \
	nherbaut/scrapper-frontend:1.0.0-SNAPSHOT

k-refresh:
	kubectl delete pods -l app=scrapper && sleep 10

k-logs:
	kubectl logs -l app=scrapper -f
	
