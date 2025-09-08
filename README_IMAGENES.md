# SGI LIB - Gestión de Imágenes de Libros

## Nuevas Funcionalidades Implementadas

### Gestión de Imágenes Personalizadas

El sistema ahora permite agregar y gestionar imágenes personalizadas para los libros, mejorando significativamente la experiencia visual.

#### Características Principales:

1. **Vista Previa Mejorada**
   - Tamaño de vista previa aumentado para pantalla completa
   - Imágenes más grandes y nítidas
   - Diseño responsivo que se adapta al tamaño de la ventana

2. **Carga de Imágenes Personalizadas**
   - Botón "Agregar Imagen" en la vista previa del libro
   - Selector de archivos con filtros para formatos de imagen (JPG, PNG, GIF, BMP)
   - Redimensionamiento automático manteniendo la proporción
   - Centrado automático de la imagen

3. **Eliminación de Imágenes**
   - Botón "Eliminar Imagen" para quitar imágenes personalizadas
   - Confirmación antes de eliminar
   - Vuelta automática a la portada generada

4. **Persistencia de Datos**
   - Las rutas de las imágenes se guardan automáticamente en la base de datos XML
   - Las imágenes se cargan automáticamente al iniciar la aplicación
   - Cambios persistentes entre sesiones

#### Cómo Usar:

1. **Agregar una Imagen:**
   - Selecciona un libro en la tabla del catálogo
   - En la vista previa, haz clic en "Agregar Imagen"
   - Selecciona una imagen desde tu computadora
   - La imagen se mostrará inmediatamente en la vista previa

2. **Eliminar una Imagen:**
   - Selecciona un libro que tenga imagen personalizada
   - Haz clic en "Eliminar Imagen"
   - Confirma la eliminación
   - El libro volverá a mostrar la portada generada automáticamente

3. **Formatos Soportados:**
   - JPG/JPEG
   - PNG
   - GIF
   - BMP

#### Mejoras Técnicas:

- **Redimensionamiento Inteligente:** Las imágenes se redimensionan manteniendo su proporción original
- **Calidad de Renderizado:** Antialiasing y interpolación bilineal para mejor calidad visual
- **Manejo de Errores:** Mensajes informativos si una imagen no se puede cargar
- **Interfaz Intuitiva:** Botones claros y feedback visual inmediato

#### Estructura de Archivos:

```
SGI LIB/
├── images/           # Directorio para almacenar imágenes
├── database.xml      # Base de datos con rutas de imágenes
├── ui/
│   ├── CatalogPanel.java      # Panel principal con gestión de imágenes
│   └── BookCoverGenerator.java # Generador de portadas mejorado
└── model/
    ├── Book.java              # Modelo con soporte de imágenes
    └── XMLDatabaseManager.java # Persistencia de rutas de imágenes
```

#### Notas Importantes:

- Las imágenes se referencian por su ruta absoluta en el sistema de archivos
- Si mueves o eliminas una imagen del sistema, el libro volverá a mostrar la portada generada
- Se recomienda mantener las imágenes en el directorio `images/` del proyecto
- El sistema es compatible con versiones anteriores (libros sin imágenes personalizadas)

---

**Desarrollado para SGI LIB - Sistema de Gestión de Librería**

