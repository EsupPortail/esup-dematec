![EsupDematEC](https://github.com/EsupPortail/esup-dematec/raw/master/src/main/webapp/images/logo-esup-dematec.png)

Application de dématérialisation des recrutements des enseignants chercheurs et ATER liés à l'application nationale Galaxie.
============================

Cette application permet une gestion des authentifications des candidats et autres utilisateurs (administrateur, gestionnaire, membre de commission).

Elle ingère des fichiers Excel Galaxie pour générer ensuite les comptes utilisateurs, postes, candidatures liés.

Elle permet aux candidats de déposer des fichiers pour chacune de ses candidatures.

Elle permet aux membres de consulter les fichiers des candidatures correspondant à leur(s) commission(s) et validées par les gestionnaires (RH : recevable/non-recevable).

L'ensemble des données est stocké dans une base de données, fichiers compris, cela nous a ammené à utiliser PostgreSQL (et non MySQL) pour ses possibilités de streaming sur les blobs. 


## Configurations 

### Configurations Systèmes

Logs : 
* src/main/resources/log4j.properties

Base de données : 
* src/main/resources/META-INF/spring/database.properties pour paramètres de connexion
* src/main/resources/META-INF/persistence.xml pour passage de create à update après premier lancement (création + initialisation de la base de données - utilisateur par défaut d'administration : admin/admin)

Mails : 
* src/main/resources/META-INF/spring/email.properties

Galaxie : 
* src/main/resources/META-INF/spring/applicationContext-galaxie.xml pour le mapping Excel Galaxie -> EsupDematEC

Sécurité : 
* src/main/resources/META-INF/spring/security.properties pour limiter éventuellement l'accès à l'application depuis certaines IP uniquement pour les utilisateurs admin, super-manager et manager


### Configurations via l'interface graphique de l'application

Les admin et super-manager peuvent configurer certains éléments de l'application via le menu 'Configuration' :
* dates de clôture pour les candidats, les candidats actifs, les membres (3 dates dstinctes)
* Titre de l'application visible sur chaque page
* Logo de l'application visible sur chaque page
* Pied de page de l'application visible sur chaque page (HTML)
* Texte d'accueil visible sur la page d'accueil (page d'authentification) (HTML)
* Texte d'accueil après authentification pour le candidat (HTML)
* Texte d'aide sur la page des candidatures pour le candidat (HTML)
* Texte d'aide sur la page de dépôt pour une candidature pour le candidat (HTML)
* Texte d'accueil après authentification pour le membre (HTML)
* Texte d'aide sur la page des candidatures pour le membre (HTML)
* Texte d'aide sur la page de dépôt (consultation) pour une candidature pour le membre (HTML)
* Texte des mails (from, subject et corps) envoyés aux candidats et membres après génération de leurs comptes.
* Texte du mail "password oublié"


## Installation 

### Pré-requis
* Java (JDK - JAVA SE 6 OK):  http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Maven (dernière version 3.0.x ok) : http://maven.apache.org/download.cgi
* Postgresql (8 ou 9 OK) : le mieux est de l'installer via le système de paquets de votre linux.
* Tomcat (Tomcat 6 OK) - ne pas prendre la version 6.0.41 (prendre une version inférieure ou supérieure si disponible) qui contient un [bug rédhibitoire pour EsupDematEC](https://issues.apache.org/bugzilla/show_bug.cgi?id=56561).

### PostgreSQL
* pg_hba.conf : ajout de 

``` 
host    all             all             127.0.0.1/32            password
``` 

* redémarrage de postgresql
* psql

```
create database esupdematec;
create USER esupdematec with password 'esup';
grant ALL ON DATABASE esupdematec to esupdematec;
```

### Paramétrage mémoire JVM :

Pensez à paramétrer les espaces mémoire JVM : 
```
export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

Pour maven :
```
export MAVEN_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

### Lancement simple avec jetty :
```
mvn jetty:run
```
Puis firefox http://localhost:8080/EsupDematEC (compte admin/admin)


### Obtention du war pour déploiement sur tomcat ou autre :
```
mvn clean package
```



## POSTGRESQL

Cette application a été dévelopée en utilisant Spring ROO et donc ses technologies associées.
Elle peut théoriquement supporter les différentes bases de données supportées par Spring ROO dans lequel on utilise ici JPA (pour la gestion des blob nous avons également une adhérence avec Hibernate).

Comme annoncé ci-dessus, l'application a cependant été développée et optimisée dans l'optique d'être installée sur un PostgreSQL : lecture/écriture des blobs dans une transaction par streaming si supporté ; cela afin de pouvoir stocker et récupérer des fichiers de taille importante sans saturation de la RAM.
Nous recommandons donc l'usage de PostgreSQL pour cette application.

Pour une bonne gestion des blob de cette application, il faut ajouter dans PostgreSQL un trigger sur la base de données sur la table big_file.
La fonction lo_manage est nécessaire ici.

Sous debian : 
```
apt-get install postgresql-contrib
```

Puis la création de l'extension lo se fait via un super-user:

* avec postgresql 8 :
```
psql
\c esupdematec
\i /usr/share/postgresql/8.4/contrib/lo.sql
```

* avec postgresql 9 :
```
psql
\c esupdematec
CREATE EXTENSION lo;
```
--

Et enfin ajout du trigger* : 
```
CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON big_file  FOR EACH ROW EXECUTE PROCEDURE lo_manage(binary_file);
```

CF http://docs.postgresqlfr.org/8.3/lo.html

\* afin que les tables soient préalablement créées, notamment la table big_file sur lequel on souhaite mettre le trigger lo_manage, vous devez démarrer l'application une fois ; en n'oubliant pas ensuite, pour ne pas écraser la base au redémarrage, de __modifier src/main/resources/META-INF/persistence.xml : create-> update__ - cf ci-dessous.



## Screenshots
http://www.esup-portail.org/pages/viewpage.action?pageId=282099720#ESUP-DématérialisationEnseignantsChercheurs(etATER)-Screenshots

