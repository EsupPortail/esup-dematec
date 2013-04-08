![EsupDematEC](https://github.com/EsupPortail/esup-dematec/raw/master/src/main/webapp/images/logo-esup-dematec.png)

Application de dématérialisation des recrutements des enseignants chercheurs et ATER liés à l'application nationale Galaxie.
============================

Cette application permet une gestion des authentifications des candidats et autres utilisateurs (administrateur, gestionnaire, président, membre de commission).

Elle ingère des fichiers Excel Galaxie pour générer ensuite les comptes utilisateurs, postes, candidatures liés.
Elle permet aux candidats de déposer des fichiers pour chacune de ses candidatures.

L'ensemble des données est stocké dans une base de données, fichiers compris, cela nous a ammené à utiliser PostgreSQL (et non MySQL) pour ses possibilités de streaming sur les blobs. 

## POSTGRESQL

Cette application a été dévelopée en utilisant Spring ROO et donc ses technologies associées.
Elle peut théoriquement supporter les différentes bases de données supportées par Spring ROO dans lequel on utilise ici JPA (pour la gestion des blob nous avons également une adhérence avec Hibernate).

Comme annoncé ci-dessus, l'application a cependant été développée et optimisée dans l'optique d'être installée sur un PostgreSQL : lecture/écriture des blobs dans une transaction par streaming si supporté ; cela afin de pouvoir stocker et récupérer des fichiers de taille importante sans saturation de la RAM.
Nous recommandons donc l'usage de PostgreSQL pour cette application.

Pour une bonne gestion des blob de cette application, il faut ajouter dans PostgreSQL un trigger sur la base de données sur la table big_file.
La fonction lo_manage est nécessaire ici.

Sous debian : 
apt-get install postgresql-contrib

Puis la création de l'extension lo se fait via un super-user:
psql
\c esupdematec
CREATE EXTENSION lo;

Et enfin ajout du trigger : 
CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON big_file  FOR EACH ROW EXECUTE PROCEDURE lo_manage(binary_file);

CF http://docs.postgresqlfr.org/8.3/lo.html


## Configurations 

### Configurations Systèmes

Logs : src/main/resources/log4j.properties

Base de données : 
* src/main/resources/META-INF/spring/database.properties pour paramètres de connexion
* src/main/resources/META-INF/persistence.xml pour passage de create à update après premier lancement (création + initialisation de la base de données)

Mails : 
* src/main/resources/META-INF/spring/email.properties

Galaxie : 
* src/main/resources/META-INF/spring/applicationContext-galaxie.xml pour le mapping Excel Galaxie -> EsupDematEC

Sécurité : 
* src/main/resources/META-INF/spring/security.properties pour limiter éventuellement l'accès à l'application depuis certaines IP uniquement pour les utilisateurs admin, super-manager et manager


### Configurations via l'application

Les admin et super-manager peuvent configurer certains éléments de l'application via le menu 'Configuration'


## Installation

### lancement simple avec jetty :
mvn jetty:run

### obtention du war pour déploiement sur tomcat ou autre :
mvn clean package


