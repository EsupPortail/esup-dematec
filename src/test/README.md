# Tests d'intégration - EsupDematEC

## Architecture des tests

**Tout passe par MockMvc** : Les tests utilisent uniquement les contrôleurs via MockMvc pour interagir avec l'application. Aucun appel direct à `persist()`, `merge()` ou aux services n'est effectué dans les tests.

## Configuration

### Fichiers de configuration

1. **`src/main/resources/META-INF/spring/*.properties`** : Configuration de production (NON modifiée)
2. **`src/test/resources/application-test.properties`** : Surcharge des propriétés pour les tests
3. **`src/test/resources/META-INF/spring/applicationContext-test.xml`** : Configuration Spring spécifique aux tests

### Base de données PostgreSQL avec Testcontainers

Les tests utilisent **Testcontainers** pour démarrer automatiquement un conteneur PostgreSQL.

La base de données est configurée en mode `create-drop` et est recréée à chaque exécution de tests.

```bash
# Exécuter les tests (Testcontainers démarre automatiquement PostgreSQL)
mvn clean compile test -DskipTests=false -Dtest=IntegrationTestSuite
```

### Intégration continue

Le workflow `.github/workflows/integration-tests.yml` lance `IntegrationTestSuite`
sur toutes les pull requests et les push vers `master`. Il peut aussi être lancé
manuellement depuis l'onglet Actions de GitHub.

Il utilise Java 17 sur Ubuntu avec un cache Maven. Testcontainers démarre PostgreSQL
automatiquement ; aucun service de base de données externe ni secret n'est nécessaire.
La suite s'exécute dans un seul job pour préserver l'ordre des tests et leurs données
partagées. Les rapports `target/surefire-reports/` sont conservés pendant 14 jours
dans l'artefact `integration-test-reports`, y compris en cas d'échec des tests.

### Structure des tests

```
src/test/java/fr/univrouen/poste/test/
├── AbstractDatabaseTest.java           # Configuration BDD + Testcontainers
├── AbstractControllerTest.java         # Configuration MockMvc
├── IntegrationTestSuite.java          # Suite ordonnée de tests
│
├── security/
│   └── SecurityAccessTest.java        # Tests de sécurité
│
└── web/
    ├── LoginControllerTest.java       # Tests login
    │
    ├── admin/
    │   ├── AdminControllerTest.java   # Tests admin
    │   └── UserControllerTest.java    # Gestion utilisateurs
    │
    ├── candidat/
    │   └── MyPosteCandidatureControllerTest.java
    │
    └── membre/
        └── PosteAPourvoirControllerTest.java
```

### Ordre d'exécution

La suite `IntegrationTestSuite` exécute les tests dans un ordre spécifique pour construire progressivement les données en base :

1. **SecurityAccessTest** - Tests de sécurité (sans création de données)
2. **LoginControllerTest** - Tests de connexion
3. **UserControllerTest** - Gestion des utilisateurs (crée admin, manager, membre)
4. **GalaxieImportControllerTest** - Import Excel Galaxie et génération automatique (candidats, postes, candidatures)
5. **PosteAPourvoirControllerTest** - Gestion des postes
6. **MyPosteCandidatureControllerTest** - Gestion des candidatures
7. **AdminControllerTest** - Tests des fonctionnalités admin (stats, export)

Chaque test peut utiliser les données créées par les tests précédents.

## Tests navigateur avec Playwright

Le workflow [Tests navigateur](../../.github/workflows/browser-tests.yml) lance Chromium
sur l'application démarrée dans Jetty, avec PostgreSQL 17 et les données
d'initialisation de l'application. Il s'exécute sur les pull requests et les push
vers `master`. En cas d'échec, l'artefact `playwright-diagnostics` contient les
traces Playwright, le journal Jetty et les rapports Maven.

Pour rejouer la suite sous Linux avec Java 17, Maven et Docker, lancer ces
commandes depuis la racine du dépôt.

```bash
mvn -B -ntp -Pbrowser-tests exec:java@playwright \
  '-Dexec.args=install --with-deps chromium'

(
  db_container=$(docker run --rm -d -p 127.0.0.1:55432:5432 \
    -e POSTGRES_DB=dematec_browser_test -e POSTGRES_USER=dematec_browser \
    -e POSTGRES_PASSWORD=dematec_browser postgres:17) || exit
  trap 'docker stop "$db_container" >/dev/null' EXIT
  for attempt in {1..30}; do
    docker exec "$db_container" pg_isready -h 127.0.0.1 -U dematec_browser -d dematec_browser_test && break
    sleep 1
  done
  bash src/test/browser/run.sh
)
```

Hibernate recrée le schéma au démarrage. Réserver la base locale
`dematec_browser_test` à cette suite. Les traces sont disponibles dans
`target/playwright/`.

L'indicateur de robustesse affiche actuellement le niveau calculé, puis
`pschecker` lève une exception sur le champ de confirmation qu'il attend.
Le scénario vérifie l'affichage et tolère uniquement cette exception connue.

Le scénario des paramètres de campagne couvre la saisie, l'enregistrement
et la relecture de la date, de la couleur et du texte enrichi.
