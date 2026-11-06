# Builds the whole game into one image: web client, master and account servers
# (Node.js) and the game server (Java). server.js serves everything on $PORT.

# 1. Build the Java game server.
FROM maven:3.9-eclipse-temurin-17 AS game-build
WORKDIR /build
COPY backup_server/server/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backup_server/server/src ./src
RUN mvn -q clean package -DskipTests

# 2. Runtime: Node.js plus a Java runtime.
FROM node:22-bookworm-slim
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY backup_master/masterserver/package*.json backup_master/masterserver/
RUN cd backup_master/masterserver && npm ci --omit=dev --no-audit --no-fund
COPY backup_account/accountserver/package*.json backup_account/accountserver/
RUN cd backup_account/accountserver && npm ci --omit=dev --no-audit --no-fund

COPY . .
COPY --from=game-build /build/target/MopeSBoxServer-1.0-SNAPSHOT.jar backup_server/server/target/

ENV NODE_ENV=production
# Public port. Hosts like Sevalla and Render set PORT themselves; 8080 is the
# usual default when they don't.
ENV PORT=8080
EXPOSE 8080
CMD ["node", "server.js"]
