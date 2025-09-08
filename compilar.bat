@echo off
title SGI LIB - Sistema de Gestion de Libreria
echo ==========================================
echo    SGI LIB - Sistema de Gestion de Libreria
echo    Con Base de Datos XML y Vista Previa
echo ==========================================
echo.
echo compilando proyecto...
javac -cp . LibreriaApp.java
if %errorlevel% == 0 (
    echo ✓ ¡compilacion exitosa!
    echo.
    echo iniciando aplicacion con nuevas caracteristicas:
    echo • base de datos xml persistente
    echo • vista previa de portadas de libros
    echo • operaciones crud completas
    echo.
    echo ejecutando...
    java LibreriaApp
) else (
    echo ✗ error en la compilacion
    pause
)

