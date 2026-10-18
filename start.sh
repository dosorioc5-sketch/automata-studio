#!/bin/bash

# Iniciar pantalla virtual
Xvfb :0 -screen 0 1280x800x24 &
sleep 1

# Iniciar gestor de ventanas
openbox &

# Ejecutar la aplicación Java
java -jar /app/app.jar &

# Iniciar servidor VNC
x11vnc -display :0 -forever -shared -rfbport 5900 -nopw &
sleep 1

# Iniciar noVNC en el puerto 10000
/usr/share/novnc/utils/novnc_proxy --vnc localhost:5900 --listen 10000
