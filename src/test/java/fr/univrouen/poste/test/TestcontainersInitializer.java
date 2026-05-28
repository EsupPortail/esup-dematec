/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.test;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Initializer Spring pour démarrer Testcontainers AVANT le chargement du contexte Spring
 *
 * Cette classe est appelée par Spring Test avant la création du contexte d'application.
 * Elle démarre le conteneur PostgreSQL et configure les propriétés de connexion.
 */
public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static PostgreSQLContainer<?> postgresContainer;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        startPostgresContainer();

        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        Map<String, Object> testcontainersProperties = new HashMap<>();
        testcontainersProperties.put("database.url", postgresContainer.getJdbcUrl());
        testcontainersProperties.put("database.username", postgresContainer.getUsername());
        testcontainersProperties.put("database.password", postgresContainer.getPassword());
        testcontainersProperties.put("database.driverClassName", postgresContainer.getDriverClassName());

        environment.getPropertySources().addFirst(
            new MapPropertySource("testcontainersProperties", testcontainersProperties)
        );

        System.out.println("✅ Propriétés Testcontainers injectées dans le contexte Spring");
    }

    private static void startPostgresContainer() {
        if (postgresContainer == null || !postgresContainer.isRunning()) {
            System.out.println("🐳 Démarrage du conteneur PostgreSQL avec Testcontainers...");

            postgresContainer = new PostgreSQLContainer<>("postgres:13-alpine")
                .withDatabaseName("esup_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

            postgresContainer.start();

            System.out.println("✅ Conteneur PostgreSQL démarré:");
            System.out.println("   - URL: " + postgresContainer.getJdbcUrl());
            System.out.println("   - Port hôte: " + postgresContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
            System.out.println("   - Username: " + postgresContainer.getUsername());
            System.out.println("   - Database: " + postgresContainer.getDatabaseName());
            System.out.println("   ℹ️  Le port est dynamique pour éviter les conflits avec PostgreSQL local");

            // Hook pour arrêter le conteneur à la fin des tests, sauf si keepAlive est true
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // TODO : ajouter uen configuration pour choisir de garder le conteneur actif après les tests (keepAlive=true) ou de l'arrêter
                if(true) {
                    System.out.println("🛑 Arrêt du conteneur PostgreSQL...");
                    postgresContainer.stop();
                } else {

                    System.out.println("JDBC URL: " + postgresContainer.getJdbcUrl());
                    System.out.println("Username: " + postgresContainer.getUsername());
                    System.out.println("Password: " + postgresContainer.getPassword());
                    System.out.println("Port: " + postgresContainer.getFirstMappedPort());
                    try {
                        Thread.sleep(Long.MAX_VALUE); // Garde le conteneur actif
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));
        }
    }

    /**
     * Méthode utilitaire pour obtenir le conteneur PostgreSQL dans les tests si nécessaire
     */
    public static PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }

}

