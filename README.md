# SGI LIB - Sistema de Gestión de Librería

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/Swing-UI-blue.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![XML](https://img.shields.io/badge/XML-Database-green.svg)](https://www.w3.org/XML/)

Sistema completo de gestión de librería desarrollado en Java con una interfaz gráfica moderna, base de datos XML persistente y gestión avanzada de imágenes de libros.

## Características Principales

### Gestión de Catálogo
- **CRUD completo** de libros que permite Crear, Leer, Actualizar, Eliminar
- **Búsqueda avanzada** por título y autor
- **Control de inventario** con gestión de stock
- **Categorización** de libros Novelas, Libros de texto
- **Vista previa** de portadas con imágenes personalizadas

### Gestión de Autores
- Registro y gestión de autores
- Asociación múltiple de autores por libro
- Búsqueda y filtrado de autores

### Gestión de Clientes
- Registro de clientes con identificadores únicos
- Historial de compras por cliente
- Gestión de ventas con control de stock

### Sistema de Imágenes
- **Portadas generadas automáticamente** con información del libro
- **Imágenes personalizadas** con carga desde archivo
- **Vista previa mejorada** con redimensionamiento inteligente
- **Persistencia** de rutas de imágenes en base de datos XML

### Persistencia de Datos
- **Base de datos XML** para almacenamiento persistente
- **Auto-guardado** automático de cambios
- **Carga automática** de datos al iniciar la aplicación

## Tecnologías Utilizadas

- **Java 11+** - Lenguaje de programación principal
- **Java Swing** - Interfaz gráfica de usuario
- **FlatLaf** - Tema moderno y oscuro para la interfaz
- **XML** - Base de datos persistente
- **Java 2D Graphics** - Generación y manipulación de imágenes
-  **BookApiService** La clase BookApiService es un servicio de integración con Google Books API que permite enriquecer automáticamente la información de los libros en el sistema SGI LIB


## Requisitos del Sistema

- **Java Runtime Environment JRE11** o superior
- **Sistema operativo:** Windows, macOS, Linux
- **Memoria RAM:** Mínimo 512MB recomendado
- **Espacio en disco:** 100MB para la aplicación y datos

### Instalación Rápida
puedes simplemente usar el .exe adjunto, pero si buscas ejecutarlo en linux este es el procedimiento:

1. **Descargar el proyecto:**
   ```bash
   # Clonar el repositorio o descargar SGI LIB.zip
   ```

2. **Compilar y ejecutar:**
   ```bash
   # En Windows (usando el script incluido)
   compilar.bat
   
   # En Linux/macOS
   javac -cp . LibreriaApp.java
   java LibreriaApp
   ```

3. **Ejecutar desde JAR:**
   ```bash
   java -jar "SGI LIB.jar"
   ```

### Estructura del Proyecto

```
SGI LIB/
├── 📁 model/                 # Modelos de datos
│   ├── Book.java            # Clase abstracta para libros
│   ├── Novel.java           # Implementación para novelas
│   ├── TextBook.java        # Implementación para libros de texto
│   ├── Author.java          # Modelo de autor
│   ├── Client.java          # Modelo de cliente
│   ├── Library.java         # Clase principal de gestión
│   ├── BookApiService.java  # Servicio de API de libros
│   └── XMLDatabaseManager.java # Gestor de base de datos XML
├── 📁 ui/                   # Interfaz de usuario
│   ├── CatalogPanel.java    # Panel del catálogo
│   ├── AuthorsPanel.java    # Panel de autores
│   ├── ClientsPanel.java    # Panel de clientes
│   ├── AddBookDialog.java   # Diálogo para agregar libros
│   ├── EditBookDialog.java  # Diálogo para editar libros
│   ├── BookTableModel.java  # Modelo de tabla de libros
│   └── BookCoverGenerator.java # Generador de portadas
├── 📁 images/               # Directorio de imágenes personalizadas
├── 📁 book_images/          # Imágenes de libros predefinidas
├── 📄 database.xml          # Base de datos XML
├── 📄 LibreriaApp.java      # Clase principal de la aplicación
├── 📄 FlatDarkLaf.java      # Tema de interfaz oscuro
├── 📄 compilar.bat          # Script de compilación para Windows
└── 📄 README.md             # Este archivo
```

## Funcionalidades Detalladas

### Gestión de Libros

#### Agregar un Nuevo Libro
1. Navega a la pestaña **"Catálogo"**
2. Haz clic en **"Agregar Libro"**
3. Completa la información:
   - **Título:** Nombre del libro
   - **ISBN:** Código único del libro
   - **Precio:** Precio de venta
   - **Año:** Año de publicación
   - **Stock:** Cantidad disponible
   - **Tipo:** Novela o Libro de texto
   - **Autores:** Selecciona de la lista o agrega nuevos

#### Editar un Libro
1. Selecciona un libro en la tabla
2. Haz clic en **"Editar Libro"**
3. Modifica los campos necesarios
4. Guarda los cambios

#### Eliminar un Libro
1. Selecciona un libro en la tabla
2. Haz clic en **"Eliminar Libro"**
3. Confirma la eliminación

### Gestión de Imágenes

#### Agregar Imagen Personalizada
1. Selecciona un libro en el catálogo
2. En la vista previa, haz clic en **"Agregar Imagen"**
3. Selecciona una imagen desde tu computadora
4. La imagen se mostrará inmediatamente

#### Eliminar Imagen Personalizada
1. Selecciona un libro con imagen personalizada
2. Haz clic en **"Eliminar Imagen"**
3. Confirma la eliminación
4. El libro volverá a mostrar la portada generada

**Formatos soportados:** JPG, PNG, GIF, BMP

### Gestión de Ventas

#### Realizar una Venta
1. Navega a la pestaña **"Catálogo"**
2. Selecciona un libro con stock disponible
3. Haz clic en **"Vender Libro"**
4. Selecciona el cliente
5. Confirma la venta

#### Ver Historial de Cliente
1. Navega a la pestaña **"Clientes"**
2. Selecciona un cliente
3. Revisa su historial de compras

#### clase BookApiServiceC
Con el boton de asignar imagenes automaticamente se obtienen datos de la API de google para asignar imagenes en todos los libros que esten listados
simplemente agrege los libros y busque los metadatos de sus libros listados.

## Configuración Avanzada

### Personalización del Tema
El sistema utiliza FlatLaf con tema oscuro por defecto. Para cambiar el tema, modifica `LibreriaApp.java`:

```java
// Cambiar a tema claro
UIManager.setLookAndFeel(new FlatLightLaf());

// Usar tema del sistema
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
```

### Configuración de Base de Datos
La base de datos XML se encuentra en `database.xml`. Para cambiar la ubicación, modifica `XMLDatabaseManager.java`:

```java
private static final String DATABASE_FILE = "ruta/personalizada/database.xml";
```

## Base de Datos XML

El sistema utiliza XML para persistencia de datos. Estructura del archivo `database.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<library>
    <books>
        <book>
            <title>Nombre del Libro</title>
            <isbn>978-1234567890</isbn>
            <price>29.99</price>
            <year>2023</year>
            <stock>10</stock>
            <type>novel</type>
            <customImagePath>/ruta/a/imagen.jpg</customImagePath>
            <authors>
                <author>Nombre del Autor</author>
            </authors>
        </book>
    </books>
    <authors>
        <author>
            <name>Nombre del Autor</name>
            <biography>Biografía del autor</biography>
        </author>
    </authors>
    <clients>
        <client>
            <id>CLI001</id>
            <name>Nombre del Cliente</name>
            <email>cliente@email.com</email>
            <purchasedBooks>
                <book>978-1234567890</book>
            </purchasedBooks>
        </client>
    </clients>
</library>
```

## Solución de Problemas

### Error de Compilación
- **Problema:** `javac: command not found`
- **Solución:** Instalar Java JDK 11 o superior

### Error de Ejecución
- **Problema:** `java.lang.OutOfMemoryError`
- **Solución:** Aumentar memoria de Java: `java -Xmx1g LibreriaApp`

### Imágenes No Se Cargan
- **Problema:** Las imágenes personalizadas no aparecen
- **Solución:** Verificar que las rutas de las imágenes sean correctas y los archivos existan

### Base de Datos Corrupta
- **Problema:** Error al cargar la base de datos
- **Solución:** Hacer backup de `database.xml` y restaurar desde una versión anterior


## Licencia

Este proyecto no tiene licencia debido a que es meramente educativo e ilustrativo

## Autor

**Julian David Cardenas Guevara** - https://www.linkedin.com/in/julicardenas/ 

Desarrollado como proyecto educativo e ilustrativo para demostrar las capacidades de Java Swing y gestión de datos XML.

## Soporte

Para reportar bugs o solicitar nuevas funcionalidades:

- Email: [juliyonose7@gmail.com]

---

**¡Gracias por usar SGI LIB!**
