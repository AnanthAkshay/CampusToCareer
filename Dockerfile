# ============================================================================
# Dockerfile for RIT Placement Portal
# ============================================================================
# Multi-stage build: Build WAR with Maven, then deploy to Tomcat
# ============================================================================

# Stage 1: Build the WAR file
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to Tomcat
FROM tomcat:10-jdk17

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR as ROOT.war (deploys to root context)
COPY --from=builder /app/target/rit-placement-portal.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat port
EXPOSE 8080

# Set environment variables for database connection
ENV DB_HOST=db
ENV DB_PORT=3306
ENV DB_NAME=placement_system
ENV DB_USER=root
ENV DB_PASSWORD=placement_root_2024

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]
