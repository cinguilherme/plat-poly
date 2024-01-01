build-rest-api:
	@echo "Building REST API"
	clojure -T:build uberjar :project rest-api

build-image-rest-api:
	make build-rest-api
	@echo "Building image REST API"
	cd projects/rest-api && docker build -t rest-api -f Dockerfile .

run-rest-api:
	@echo "Running REST API"
	java -jar projects/rest-api/target/rest-api.jar

run-image-rest-api:
	@echo "Running image REST API"
	docker run -e ENV=dev -p 8890:8890 rest-api