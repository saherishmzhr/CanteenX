# Deploying Smart Canteen (Free Tiers)

This guide covers deploying **Smart Canteen** on **Railway** and **Render** without external payment gateways, email services, or API keys.

---

## Option 1: Railway (Recommended — Includes Managed MySQL)

Railway offers a starter/trial tier without requiring a credit card up front and can provision a **MySQL** database directly in the project.

1. Push the `smart-canteen` project to a GitHub repository.
2. In **Railway**, create a **New Project** -> **Deploy from GitHub repo** and select your repository.
3. In the same Railway project, click **+ New** -> **Database** -> **MySQL**.
4. Open your Spring Boot web service **Variables** tab and set:
   - `SPRING_PROFILES_ACTIVE` = `mysql`
   - `SPRING_DATASOURCE_URL` = `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
     *(Equivalently, Railway's standard JDBC format is `jdbc:mysql://<MYSQLHOST>:<MYSQLPORT>/<MYSQLDATABASE>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.)*
   - `SPRING_DATASOURCE_USERNAME` = `${{MySQL.MYSQLUSER}}`
   - `SPRING_DATASOURCE_PASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
5. Railway automatically injects `PORT` and builds with Maven (`./mvnw -DskipTests package` / `java -jar target/*.jar`). Once deployed, open your generated `*.up.railway.app` URL.

---

## Option 2: Render (Free Web Service)

Render's free web service tier runs offline using the default H2 file database with zero external database setup (note that ephemeral container disks reset across restarts/redeploys unless backed by an external SQL database).

1. Push the `smart-canteen` project to a GitHub repository.
2. On **Render**, click **New +** -> **Web Service** and connect your repository.
3. Configure build & start commands:
   - **Runtime**: `Java` (or Docker/Native Java 17)
   - **Build Command**: `./mvnw -DskipTests clean package`
   - **Start Command**: `java -Dserver.port=${PORT:-8080} -jar target/smart-canteen-1.0.0.jar`
4. Environment variables:
   - Leave `SPRING_PROFILES_ACTIVE` unset (or set to `default`) to use the built-in **H2** database out of the box.
   - If connecting to an external MySQL host instead, set `SPRING_PROFILES_ACTIVE=mysql`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
5. Deploy and open your `*.onrender.com` URL.
