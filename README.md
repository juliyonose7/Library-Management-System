# SGI LIB - Library Management System

A complete library management system developed in Java featuring a modern graphical user interface, a persistent XML database, and advanced book image management.

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

* **XML Database** for persistent storage
* **Auto-save** functionality for all changes
* **Automatic data loading** upon application startup

## Technologies Used

* **Java 11+** - Core programming language
* **Java Swing** - Graphical User Interface (GUI) toolkit
* **FlatLaf** - Modern dark theme for the interface
* **XML** - Persistent database storage
* **Java 2D Graphics** - Image generation and manipulation
* **BookApiService** - Integration service with the Google Books API to automatically enrich book information within the SGI LIB system

## Migration Progress (Desktop -> Web)

This repository now includes an initial backend module to migrate the desktop solution to a modern web architecture.

### New module: `backend/`

* **Spring Boot 3** bootstrap
* **PostgreSQL** datasource configuration
* **Flyway** baseline migration (`V1__baseline.sql`)
* **JWT Authentication** with roles (`ADMIN`, `LIBRARIAN`, `CLIENT`)
* **Access + Refresh token flow** with persisted refresh tokens
* Initial domain entities and repositories:
    * `Author`
    * `Book`
    * `Client`

### Auth endpoints

* `POST /api/v1/auth/login`
* `POST /api/v1/auth/refresh`

Default bootstrap admin user (for local development):

* Username: `admin`
* Password: `Admin123!`

### API documentation

Once backend is running:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

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

## XML Database

The system uses XML for data persistence. Structure of the `database.xml` file:

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

## License

This project is unlicensed as it is strictly educational and illustrative.
Developed as an educational and illustrative project to demonstrate the capabilities of Java Swing and XML data management.




