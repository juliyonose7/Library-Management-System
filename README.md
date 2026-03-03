# SGI LIB - Library Management System

[![Backend CI](https://github.com/juliyonose7/Library-Management-System/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/juliyonose7/Library-Management-System/actions/workflows/backend-ci.yml)
[![Backend CD](https://github.com/juliyonose7/Library-Management-System/actions/workflows/backend-cd.yml/badge.svg)](https://github.com/juliyonose7/Library-Management-System/actions/workflows/backend-cd.yml)
[![Release Please](https://github.com/juliyonose7/Library-Management-System/actions/workflows/release-please.yml/badge.svg)](https://github.com/juliyonose7/Library-Management-System/actions/workflows/release-please.yml)

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?logo=grafana&logoColor=white)
![Alertmanager](https://img.shields.io/badge/Alertmanager-Notifications-E6522C)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?logo=flyway&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?logo=jsonwebtokens&logoColor=white)

A hybrid library management system: Java Swing desktop + Spring Boot API + Angular frontend, with SQL persistence, desktop↔web synchronization, and XML export support.

## New Version (v2.0.0)

### Highlights

* **Hybrid operation**: Desktop and web app working on the same backend/API.
* **SQL-first desktop mode**: Desktop can load and persist through backend API (PostgreSQL).
* **XML as explicit export**: XML is now export/fallback, not the primary persistence in sync mode.
* **Metadata enrichment improved**: ISBN enrichment now uses Google Books + Open Library fallback.
* **Modernized Angular UI**: app shell, dark theme, toasts, confirm dialogs, sorting, pagination, skeleton/empty states.
* **Desktop cover compatibility**: desktop now supports local image paths and HTTP/HTTPS cover URLs.

## Key Features

### Catalog Management

* **Full CRUD** operations for books (Create, Read, Update, Delete)
* **Advanced search** by title and author
* **Inventory control** with stock management
* **Categorization** of books (Novels, Textbooks)
* **Cover preview** with custom images

### Author Management

* Author registration and management
* Multiple author association per book
* Author search and filtering

### Client Management

* Client registration with unique identifiers
* Purchase history per client
* Sales management with automatic stock adjustment

### Image System

* **Auto-generated covers** using book information
* **Custom images** supported via file upload
* **Enhanced preview** with intelligent resizing
* **Persistence** of image paths in the XML database

### Data Persistence

* **PostgreSQL (via backend API)** as primary persistence in synchronized mode.
* **Desktop startup SQL-first** with fallback to local XML when backend is unavailable.
* **XML export** available explicitly from desktop UI (`Exportar XML`).

## Technologies Used

* **Java 17+** - Core programming language
* **Java Swing** - Graphical User Interface (GUI) toolkit
* **FlatLaf** - Modern dark theme for the interface
* **Spring Boot + PostgreSQL** - API and primary persistence
* **Angular** - Web frontend
* **XML** - Export/fallback persistence format
* **Java 2D Graphics** - Image generation and manipulation
* **BookApiService** - Integration service with ISBN metadata providers (Google Books and Open Library fallback)

## Migration Progress (Desktop -> Web)

This repository now includes an initial backend module to migrate the desktop solution to a modern web architecture.

### New module: `backend/`

* **Spring Boot 3** bootstrap
* **PostgreSQL** datasource configuration
* **Flyway** baseline migration (`V1__baseline.sql`)
* **JWT Authentication** with roles (`ADMIN`, `LIBRARIAN`, `CLIENT`)
* **Access + Refresh token flow** with persisted refresh tokens
* **Google Books metadata enrichment** by ISBN (cover + publisher + category + description)
* Initial domain entities and repositories:
    * `Author`
    * `Book`
    * `Client`

### Auth endpoints

* `POST /api/v1/auth/login`
* `POST /api/v1/auth/refresh`

### Sales endpoints

* `POST /api/v1/sales` (register sale and decrease stock)
* `GET /api/v1/sales` (global sales history)
* `GET /api/v1/clients/{id}/sales` (purchase history by client)

### Google Books enrichment endpoint

* `GET /api/v1/books/enrich?isbn=<isbn>`

This endpoint returns metadata and cover information (when available) using Google Books first and Open Library as fallback.

Toggle integration:

* `integration.google-books.enabled=true|false` in `backend/src/main/resources/application.yml`

Default bootstrap admin user (for local development):

* Username: `admin`
* Password: provided via `APP_BOOTSTRAP_ADMIN_PASSWORD` (see `.env.example`)

### Desktop API sync configuration

Desktop sync can be configured with environment variables:

* `SGILIB_DESKTOP_API_SYNC=true|false`
* `SGILIB_API_BASE=http://localhost:8080/api/v1`
* `SGILIB_API_USER=admin`
* `SGILIB_API_PASSWORD=<set-a-local-secret>`

### API documentation

Once backend is running:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Docker local environment

Run backend + PostgreSQL with Docker Compose:

```bash
docker compose up --build
```

Services:

* Backend: `http://localhost:8080`
* PostgreSQL: `localhost:5432`

### Observability (Prometheus + Grafana)

The Docker Compose stack now includes monitoring services:

* Prometheus: `http://localhost:9090`
* Grafana: `http://localhost:3000`
* Alertmanager: `http://localhost:9093`

Default Grafana credentials:

* Username: from `GRAFANA_ADMIN_USER`
* Password: from `GRAFANA_ADMIN_PASSWORD`

Backend metrics endpoint:

* `http://localhost:8080/actuator/prometheus`

Start all services:

```bash
docker compose up --build
```

Monitoring files:

* `monitoring/prometheus/prometheus.yml`
* `monitoring/prometheus/alerts.yml`
* `monitoring/alertmanager/alertmanager.yml`
* `monitoring/grafana/provisioning/datasources/datasource.yml`
* `monitoring/grafana/provisioning/dashboards/dashboard.yml`
* `monitoring/grafana/dashboards/backend-overview.json`
* `monitoring/grafana/dashboards/api-business-overview.json`
* `monitoring/grafana/dashboards/auth-overview.json`

Included baseline alerts:

* `BackendDown` (critical)
* `HighHttp5xxRate` (warning, > 5% for 5m)
* `HighLatencyP95` (warning, > 800ms for 10m)

Alert notification routing:

* Critical alerts -> Slack receiver (`slack-critical`)
* Warning alerts -> Email receiver (`email-warning`)

Before using in production/local demo, replace placeholders in:

* `monitoring/alertmanager/alertmanager.yml`

Required custom values:

* Slack webhook URL (`api_url`)
* SMTP host/credentials (`smarthost`, `auth_username`, `auth_password`)
* Target email addresses (`to`, `from`)

### Frontend Angular

Angular app is available in `frontend/`.

Current web modules:

* Books (CRUD + metadata enrichment by ISBN)
* Authors (CRUD)
* Clients (CRUD)
* Sales (register sale + purchase history)

Run locally:

```bash
cd frontend
npm install
npm start
```

Frontend URL:

* `http://localhost:4200`

### One-command local startup (Windows)

From repository root, run:

```powershell
.\scripts\start-dev.ps1
```

This launches:

* Backend (Spring Boot, profile `test`, port `8080`)
* Frontend (Angular, port `4200`)
* Desktop app (`LibreriaApp`) with API sync enabled

To stop everything:

```powershell
.\scripts\stop-dev.ps1
```

Also available as double-click friendly wrappers:

* `scripts/start-dev.bat`
* `scripts/stop-dev.bat`

Default login for local development:

* Username: `admin`
* Password: same value configured in `APP_BOOTSTRAP_ADMIN_PASSWORD`

### CI

GitHub Actions workflow available at:

* `.github/workflows/backend-ci.yml`

It runs backend tests automatically on push/PR affecting `backend/**`.

### Quality gates

Backend quality checks run on `mvn verify` and in CI:

* **JaCoCo** coverage report + minimum line coverage gate
* **Checkstyle** static rules (`backend/config/checkstyle/checkstyle.xml`)

Run locally:

```bash
cd backend
mvn verify
```

### CD

GitHub Actions workflow available at:

* `.github/workflows/backend-cd.yml`

What it does on push to `main`:

* Builds backend Docker image
* Pushes image to GHCR (`ghcr.io/<owner>/<repo>/backend`)
* Optionally triggers Render deploy hook if `RENDER_DEPLOY_HOOK_URL` secret is configured

### Automated releases

Release automation workflow available at:

* `.github/workflows/release-please.yml`

Configuration files:

* `release-please-config.json`
* `.release-please-manifest.json`

This creates/updates release PRs and tags based on Conventional Commits.

### Run backend locally

1. Install **Java 17+** and **Maven 3.9+**
2. Create a PostgreSQL database named `sgi_lib`
3. From the `backend` folder, run:

```bash
mvn spring-boot:run
```

By default, the API starts on `http://localhost:8080`.

## System Requirements

* **Java Runtime Environment:** JRE 11 or higher
* **Operating System:** Windows, macOS, Linux
* **RAM:** Minimum 512MB recommended
* **Disk Space:** 100MB for the application and data

### Quick Installation

You can simply execute the attached `.exe` file. However, if you wish to run it on Linux or compile it manually, follow this procedure:

1. **Download the project:**
```bash
# Clone the repository or download SGI LIB.zip

```


2. **Compile and Run:**
```bash
# On Windows (using the included script)
compilar.bat

# On Linux/macOS
javac -cp . LibreriaApp.java
java LibreriaApp

```


3. **Run from JAR:**
```bash
java -jar "SGI LIB.jar"

```



### Project Structure

```
SGI LIB/
├── 📁 model/                # Data models
│   ├── Book.java            # Abstract class for books
│   ├── Novel.java           # Implementation for novels
│   ├── TextBook.java        # Implementation for textbooks
│   ├── Author.java          # Author model
│   ├── Client.java          # Client model
│   ├── Library.java         # Main management class
│   ├── BookApiService.java  # Books API Service
│   └── XMLDatabaseManager.java # XML Database Manager
├── 📁 ui/                   # User Interface
│   ├── CatalogPanel.java    # Catalog panel
│   ├── AuthorsPanel.java    # Authors panel
│   ├── ClientsPanel.java    # Clients panel
│   ├── AddBookDialog.java   # Dialog for adding books
│   ├── EditBookDialog.java  # Dialog for editing books
│   ├── BookTableModel.java  # Book table model
│   └── BookCoverGenerator.java # Cover generator
├── 📁 images/               # Directory for custom images
├── 📁 book_images/          # Predefined book images
├── 📄 database.xml          # XML Database
├── 📄 LibreriaApp.java      # Main application class
├── 📄 FlatDarkLaf.java      # Dark interface theme
├── 📄 compilar.bat          # Compilation script for Windows
└── 📄 README.md             # This file

```

## Detailed Functionality

### Book Management

#### Add a New Book

1. Navigate to the **"Catalog"** tab.
2. Click on **"Add Book"**.
3. Fill in the information:
* **Title:** Name of the book
* **ISBN:** Unique book code
* **Price:** Selling price
* **Year:** Publication year
* **Stock:** Quantity available
* **Type:** Novel or Textbook
* **Authors:** Select from the list or add new ones



#### Edit a Book

1. Select a book from the table.
2. Click on **"Edit Book"**.
3. Modify the necessary fields.
4. Save changes.

#### Delete a Book

1. Select a book from the table.
2. Click on **"Delete Book"**.
3. Confirm deletion.

### Image Management

#### Add Custom Image

1. Select a book in the catalog.
2. In the preview pane, click **"Add Image"**.
3. Select an image file from your computer.
4. The image will be displayed immediately.

#### Remove Custom Image

1. Select a book with a custom image.
2. Click on **"Remove Image"**.
3. Confirm deletion.
4. The book will revert to the auto-generated cover.

**Supported formats:** JPG, PNG, GIF, BMP

### Sales Management

#### Process a Sale

1. Navigate to the **"Catalog"** tab.
2. Select a book with available stock.
3. Click on **"Sell Book"**.
4. Select the client.
5. Confirm the sale.

#### View Client History

1. Navigate to the **"Clients"** tab.
2. Select a client.
3. Review their purchase history.

#### BookApiService Class

Using the automatic image assignment button, data is retrieved from the Google Books API to assign images to all listed books. Simply add the books and the system will fetch metadata for your listed inventory.

## Advanced Configuration

### Theme Customization

The system uses FlatLaf with a dark theme by default. To change the theme, modify `LibreriaApp.java`:

```java
// Switch to light theme
UIManager.setLookAndFeel(new FlatLightLaf());

// Use system look and feel
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());

```

### Database Configuration

The XML database is located at `database.xml`. To change the location, modify `XMLDatabaseManager.java`:

```java
private static final String DATABASE_FILE = "custom/path/database.xml";

```

## XML Database (Export/Fallback)

In this version, XML is used as export format and local fallback source when backend sync is unavailable. Structure of the `database.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<library>
    <books>
        <book>
            <title>Book Title</title>
            <isbn>978-1234567890</isbn>
            <price>29.99</price>
            <year>2023</year>
            <stock>10</stock>
            <type>novel</type>
            <customImagePath>/path/to/image.jpg</customImagePath>
            <authors>
                <author>Author Name</author>
            </authors>
        </book>
    </books>
    <authors>
        <author>
            <name>Author Name</name>
            <biography>Author biography</biography>
        </author>
    </authors>
    <clients>
        <client>
            <id>CLI001</id>
            <name>Client Name</name>
            <email>client@email.com</email>
            <purchasedBooks>
                <book>978-1234567890</book>
            </purchasedBooks>
        </client>
    </clients>
</library>

```

## Troubleshooting

### Compilation Error

* **Issue:** `javac: command not found`
* **Solution:** Install Java JDK 11 or higher.

### Runtime Error

* **Issue:** `java.lang.OutOfMemoryError`
* **Solution:** Increase Java memory allocation: `java -Xmx1g LibreriaApp`

### Images Not Loading

* **Issue:** Custom images do not appear.
* **Solution:** Verify that image paths are correct and that the files exist in the specified directory.

### Corrupt Database

* **Issue:** Error loading the database.
* **Solution:** Create a backup of `database.xml` and restore from a previous version.

## UML Diagram and Screenshots

<p align="center"><strong>UML Class Diagram</strong></p>

<p align="center">
    <img src="Images/UML%20SGI%20FINAL.png" alt="UML Class Diagram" width="950" />
</p>

<p align="center"><strong>Application Screenshots</strong></p>

<table align="center">
    <tr>
        <td align="center"><strong>Catalog</strong></td>
        <td align="center"><strong>Management</strong></td>
    </tr>
    <tr>
        <td><img src="Images/Screenshot%202026-03-02%20182535.png" alt="Desktop Catalog View" width="430" /></td>
        <td><img src="Images/Screenshot%202026-03-03%20012050.png" alt="Desktop Management View" width="430" /></td>
    </tr>
    <tr>
        <td colspan="2" align="center"><strong>Detailed View</strong></td>
    </tr>
    <tr>
        <td colspan="2" align="center"><img src="Images/Screenshot%202026-03-03%20021615.png" alt="Desktop Detailed View" width="760" /></td>
    </tr>
</table>

## License

This project is unlicensed as it is strictly educational and illustrative.
Developed as an educational and illustrative project to demonstrate the capabilities of Java Swing and XML data management.




