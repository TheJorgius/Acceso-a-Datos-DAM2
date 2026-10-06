En este proyecto se a echo una simulación de una aplicación de una gasolinera en la que se han elegido hacer 2 clases cliente y pagos, el main, una clase gestor para las funciones que manejan los clientes y una clase ficheros para csv y otra json junto a su interfaz por si se decide cambiar el formato



Además de las funciones básicas que se solicitan en la aplicación yo e agregado las funciones de crearDirectorioYarchivos y comprobarRuta, básicamente para la primera vez que se ejecute el programa crear las carpetas o por si el usuario las borra por error



También se han creado funciones para traducir los ficheros de Csv a Json para hacer el cambio de formato



Lo primero que hace el programa es preparar las listas de clientes y pagos para poder trabajar con ellas a partir de ahí y no tener que leerlas en cada función y desplegara el menú con todas las funciones hasta que el usuario quiera salir y con el nuevo cambio las escrituras se harán al final del código con el propósito de no tener que carga el archivo y escribir constantemente ralentizando el rendimiento pero sacrificando esa actualización instantánea que puede resolver problemas de crash en el programa a que si se corta no se actualizara el archivo

