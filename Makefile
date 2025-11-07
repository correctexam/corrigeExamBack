.PHONY: check build deploy
check:
	@echo "Checking JDK"
	@java -version 2>&1 | grep 'GraalVM' >/dev/null || \
	( echo "Error: JGRaalVM is required."; \
	echo "GraalVM for Linux and macOS is available through Homebrew or SDKMAN"; \
	echo "* SDKMAN: https://sdkman.io/"; \
	echo "* Homebrew: https://brew.sh"; \
	exit 1 )
	@echo "GrallVM found."

build: check
	cp web.xml.prod src/main/resources/META-INF/web.xml
	./mvnw clean generate-sources
	./mvnw compile
	./mvnw package -Pnative
	
deploy: build
	cp web.xml.dev src/main/resources/META-INF/web.xml
	sudo docker image rm barais/grade-scope-istic
	sudo docker build -f src/main/docker/Dockerfile.native -t barais/grade-scope-istic