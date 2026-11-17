![EsupDematEC](https://github.com/EsupPortail/esup-dematec/raw/master/src/main/webapp/images/logo-esup-dematec.png)

Application de dématérialisation des recrutements des enseignants-chercheurs et ATER liés à l'application nationale Galaxie.
============================

Cette application permet une gestion des authentifications des candidats et autres utilisateurs (administrateur, gestionnaire, membre de commission).

Elle ingère des fichiers Excel Galaxie pour générer ensuite les comptes utilisateurs, postes, candidatures liés.

Elle permet aux candidats de déposer des fichiers pour chacune de ses candidatures.

Elle permet aux membres de consulter les fichiers des candidatures correspondant à leur(s) commission(s) et validées par les gestionnaires (RH : recevable/non-recevable).

L'ensemble des données est stocké dans une base de données, fichiers compris, cela nous a amené à utiliser PostgreSQL (et non MySQL) pour ses possibilités de streaming sur les blobs.


## Configuration

### Configuration système

Logs :
* `src/main/resources/logback.xml` : journalisation avec SLF4J/Logback. La sortie console est activée ; les sorties fichier et mail sont optionnelles et doivent être activées dans ce fichier.

Base de données :
* src/main/resources/META-INF/spring/database.properties pour paramètres de connexion
* `src/main/resources/META-INF/persistence.xml` : gestion du schéma Hibernate. La valeur livrée de `hibernate.hbm2ddl.auto` est `create` : elle recrée le schéma à chaque démarrage. Voir les procédures distinctes de première installation et de mise à niveau ci-dessous.

Mails :
* `src/main/resources/META-INF/spring/email.properties` : serveur SMTP, port, authentification, STARTTLS/SSL et activation des envois (`email.isEnabled`, désactivé par défaut). Les mails servent notamment à activer les comptes et à renouveler les mots de passe.

Sécurité :
* src/main/resources/META-INF/spring/security.properties pour limiter éventuellement l'accès à l'application depuis certaines IP uniquement pour les utilisateurs admin, super-manager et manager


### Configuration via l'interface graphique de l'application

Les admin et super-manager peuvent configurer certains éléments de l'application via les menus 'Mapping Galaxie', 'Configuration', 'Type de fichiers', 'Templates' :
* mapping Galaxie pour prendre en compte la structure des fichiers Excel exportés depuis Galaxie
* dates de clôture pour les candidats, les candidats actifs, les membres (3 dates distinctes)
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
* ...

## Installation

### Prérequis

* **Git** : récupération des sources et maintien des configurations locales dans une branche d’exploitation.
* **OpenJDK 17** : version de référence et cible de compilation du projet.
* **Maven 3.9.x** : nécessaire pour compiler le projet et pour le lancement local avec Jetty. Maven n'est pas nécessaire sur un serveur qui déploie uniquement le WAR.
* **PostgreSQL** : base obligatoire, y compris pour les fichiers stockés sous forme de Large Objects (BLOB), lus et écrits en streaming dans des transactions. PostgreSQL 17 est proposé comme version de référence.
* **Tomcat 10.1.x** pour le déploiement.

Installer de préférence Java et PostgreSQL via les paquets de la distribution. Vérifier que le JDK utilisé par Maven et celui utilisé par Tomcat correspondent aux prérequis.

### Récupération des sources et gestion des configurations

Récupérer les sources avec Git, sélectionner le dernier tag par numéro de version, puis créer une branche locale pour les configurations de l’installation :

```bash
git clone https://github.com/EsupPortail/esup-dematec.git
cd esup-dematec
git fetch origin --tags
latest_tag=$(git tag --list 'EsupDematEC-*' --sort=-version:refname | head -n 1)
git switch -c exploitation "refs/tags/$latest_tag"
```

Personnaliser les fichiers décrits dans la section « Configuration » et conserver ces adaptations dans la branche d’exploitation. Lors des mises à jour, intégrer le dernier tag du dépôt d’origine dans cette branche : Git conserve les adaptations locales et permet de résoudre les éventuels conflits. 
### Préparation de PostgreSQL

Les commandes suivantes illustrent une installation locale sous Debian. Adapter les chemins et les comptes à l'environnement.

Ouvrir une session d'administration :

```bash
sudo -u postgres psql
```

Créer le compte applicatif et sa base :

```sql
SET password_encryption = 'scram-sha-256';
CREATE USER esupdematec WITH PASSWORD 'A_REMPLACER_PAR_UN_MOT_DE_PASSE_FORT';
CREATE DATABASE esupdematec OWNER esupdematec;
```

Reporter le mot de passe choisi et les paramètres de connexion dans `src/main/resources/META-INF/spring/database.properties` ; remplacer le mot de passe d'exemple livré avec le projet.

Si les règles existantes de `pg_hba.conf` ne permettent pas déjà cet accès, ajouter des règles limitées à la base et au compte applicatifs, avant toute règle qui les masquerait :

```text
host    esupdematec    esupdematec    127.0.0.1/32    scram-sha-256
host    esupdematec    esupdematec    ::1/128         scram-sha-256
```

Recharger la configuration PostgreSQL. La méthode `password`, qui transmet le mot de passe en clair sans protection TLS, n'est pas recommandée. Voir l'[authentification PostgreSQL](https://www.postgresql.org/docs/current/auth-password.html).

#### Extension de gestion des Large Objects

L'extension `lo` et son trigger `lo_manage` restent nécessaires pour libérer les Large Objects lors des modifications ou suppressions dans `big_file`. Supprimer une ligne ne supprime pas automatiquement le Large Object qu'elle référence.

Sur Debian récent, les fichiers de l'extension sont déjà fournis par le paquet serveur PostgreSQL : il n'est pas (plus) nécessaire d'installer séparément `postgresql-contrib`. Sur une autre distribution, vérifier la disponibilité de l'extension et installer le paquet correspondant si nécessaire.

Se connecter à la base avec son propriétaire :

```bash
psql -h localhost -U esupdematec -d esupdematec
```

Puis activer l'extension :

```sql
CREATE EXTENSION IF NOT EXISTS lo;
```

L'extension est « trusted » : un utilisateur disposant du privilège `CREATE` sur la base peut l'installer ; un superutilisateur n'est pas systématiquement nécessaire.

Le trigger doit être créé **après la création de la table `big_file`**, selon la procédure ci-dessous. Voir la [documentation de l'extension `lo`](https://www.postgresql.org/docs/current/lo.html).

### Première installation sur une base vide

1. Préparer PostgreSQL et l'extension `lo`, puis renseigner les fichiers de configuration décrits plus haut, notamment la connexion et le SMTP.
2. Pour cette seule initialisation, conserver `hibernate.hbm2ddl.auto=create` dans `src/main/resources/META-INF/persistence.xml`.
3. Construire le WAR avec `mvn clean package`, déployer `target/EsupDematEC.war` dans Tomcat et démarrer l'application une seule fois pour créer et initialiser les tables.
4. Arrêter l'application. Passer immédiatement `hibernate.hbm2ddl.auto` de `create` à `update` dans les sources, puis reconstruire le WAR et remplacer celui qui a été déployé. **Modifier le fichier source ne modifie pas le WAR déjà construit.** Ne pas redémarrer avec le WAR d'initialisation : il recréerait le schéma et effacerait les données.
5. Avec le propriétaire de la base, créer le trigger suivant si celui-ci n'existe pas déjà :

   ```sql
   CREATE TRIGGER t_big_file
       BEFORE UPDATE OR DELETE ON big_file
       FOR EACH ROW EXECUTE FUNCTION lo_manage(binary_file);
   ```

6. Vérifier sa présence avec `\d big_file` dans `psql`, puis démarrer l'application avec le WAR configuré en `update`.
7. Se connecter avec le compte initial **admin/admin** et changer immédiatement son mot de passe. Vérifier ensuite le SMTP, les réglages de la campagne et un dépôt, une consultation et une suppression de fichier.

### Construction et déploiement du WAR

Depuis la racine du projet, après configuration :

```bash
mvn clean package
```

Le fichier produit est `target/EsupDematEC.war`. Sous Tomcat, le déploiement de ce fichier sans changement de nom utilise généralement le contexte `/EsupDematEC` : par exemple `http://localhost:8080/EsupDematEC`. Adapter cette URL au contexte et au frontal réellement configurés.

Le POM désactive les tests par défaut (`skipTests=true`) : la réussite de cette commande ne signifie donc pas que les tests d'intégration ont été exécutés. Leur procédure est décrite dans le [README des tests](src/test/README.md). Testcontainers nécessite un environnement Docker disponible.

Les paramètres modifiés dans les sources doivent être inclus dans un nouveau WAR avant déploiement.

### Lancement local avec Jetty

Le lancement avec le plugin Jetty défini dans `pom.xml` s'effectue avec :

```bash
mvn jetty:run
```

### Taille des fichiers déposés

`src/main/webapp/WEB-INF/web.xml` fixe une limite de **500 Mio (524 288 000 octets) par fichier et par requête multipart**. La limite par requête inclut l'ensemble des fichiers et les données du formulaire.

Aligner les limites et délais du serveur frontal et du conteneur sur les besoins de la campagne. Toute modification de `web.xml` doit être incluse dans un nouveau WAR.

## Mise à niveau d'une installation existante

La procédure suivante s’applique entre deux versions de l’application, y compris entre deux versions majeures. La réaliser d’abord sur une copie de la base avant la mise en production.

1. Arrêter les écritures et sauvegarder la base complète, **Large Objects compris**, ainsi que les configurations et le WAR existants. Vérifier la restauration sur un environnement de recette.
2. Depuis la branche d’exploitation, enregistrer les adaptations locales, puis récupérer et intégrer le dernier tag par numéro de version :

   ```bash
   git fetch origin --tags
   latest_tag=$(git tag --list 'EsupDematEC-*' --sort=-version:refname | head -n 1)
   git merge "refs/tags/$latest_tag"
   ```

   Résoudre les éventuels conflits en conservant les paramètres de l’installation et en tenant compte des évolutions des fichiers de configuration.
3. Vérifier les prérequis indiqués plus haut pour la version récupérée. Si PostgreSQL change de version majeure, suivre sa procédure propre de migration (`pg_upgrade` ou sauvegarde/restauration). Vérifier que `hibernate.hbm2ddl.auto=update` est conservé **avant tout démarrage, y compris celui d’un batch** : ne jamais utiliser `create` sur la base existante.
4. Compiler les sources configurées et exécuter le batch de mise à niveau, application arrêtée, depuis la racine du projet :

   ```bash
   mvn clean compile
   mvn exec:java -Dexec.args="dbupgrade"
   ```

   Ce batch utilise les paramètres de connexion de l’application. Il applique les traitements de `DbToolService` selon la version enregistrée en base, puis enregistre la version cible définie dans le code. Contrôler les journaux et le résultat sur la copie de base avant de reproduire l’opération en production.

5. Vérifier la présence de l'extension `lo` et du trigger `t_big_file`, en conservant ceux qui existent déjà.
6. Construire puis déployer le WAR configuré en `update`. Contrôler les connexions, les envois de mails, les accès selon les rôles et les opérations sur les fichiers.

Les mots de passe actuels utilisent BCrypt. Pour les comptes provenant d'anciennes versions avec un mot de passe au format SHA-256, le code prévoit l'envoi d'un lien de changement de mot de passe lors de la tentative de connexion. **Le SMTP doit être opérationnel avant la réouverture aux utilisateurs.**

## Captures d'écran

[Captures d'écran historiques sur le wiki ESUP-Portail](https://www.esup-portail.org/pages/viewpage.action?pageId=282099720#ESUP-DématérialisationEnseignantsChercheurs(etATER)-Screenshots).
