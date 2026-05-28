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
