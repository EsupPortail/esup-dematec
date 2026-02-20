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
package fr.univrouen.poste.config;

import fr.univrouen.poste.provider.CheckProfilSpringSecurityFilter;
import fr.univrouen.poste.provider.DatabaseAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.event.LoggerListener;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import java.util.List;

/**
 * JavaConfig Spring Security remplaçant applicationContext-security.xml.
 * Utilise l'API SecurityFilterChain de Spring Security 6 (pas de WebSecurityConfigurerAdapter).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private DatabaseAuthenticationProvider databaseAuthenticationProvider;

    @Autowired
    private UserDetailsService databaseUserDetailsService;

    /* ------------------------------------------------------------------ */
    /* Chaîne de sécurité pour /resources/** (pas de sécurité)             */
    /* ------------------------------------------------------------------ */

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain resourcesFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/resources/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /* ------------------------------------------------------------------ */
    /* Chaîne de sécurité principale                                       */
    /* ------------------------------------------------------------------ */

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain mainFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login/impersonate")
                    .hasAnyRole("ADMIN", "SUPER_MANAGER")
                .requestMatchers("/logout/impersonate")
                    .hasRole("PREVIOUS_ADMINISTRATOR")
                .requestMatchers("/")
                    .authenticated()
                .requestMatchers("/admin/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/membre/**")
                    .hasAnyRole("MEMBRE", "ADMIN", "MANAGER")
                .requestMatchers(
                        "/login",
                        "/forgotpassword/**",
                        "/signup",
                        "/signup/initpassword",
                        "/signup/activate/**")
                    .permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(Customizer.withDefaults())
            .sessionManagement(session -> session
                .maximumSessions(5)
                .sessionRegistry(sessionRegistry())
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/denied"))
            .addFilterAt(switchUserProcessingFilter(), SwitchUserFilter.class)
            .addFilterAfter(checkProfilFilter(), SwitchUserFilter.class)
            .addFilterAfter(resourceUrlEncodingFilter(), CheckProfilSpringSecurityFilter.class);

        return http.build();
    }

    /* ------------------------------------------------------------------ */
    /* AuthenticationManager                                               */
    /* ------------------------------------------------------------------ */

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(databaseAuthenticationProvider));
    }

    /* ------------------------------------------------------------------ */
    /* Session Registry (pour la concurrence de sessions)                  */
    /* ------------------------------------------------------------------ */

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /* ------------------------------------------------------------------ */
    /* Filtres personnalisés                                                */
    /* ------------------------------------------------------------------ */

    @Bean
    public SwitchUserFilter switchUserProcessingFilter() {
        SwitchUserFilter filter = new SwitchUserFilter();
        filter.setUserDetailsService(databaseUserDetailsService);
        filter.setSwitchUserUrl("/login/impersonate");
        filter.setExitUserUrl("/logout/impersonate");
        filter.setTargetUrl("/");
        return filter;
    }

    @Bean
    public CheckProfilSpringSecurityFilter checkProfilFilter() {
        return new CheckProfilSpringSecurityFilter();
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }

    /* ------------------------------------------------------------------ */
    /* Expression handler et listener d'événements                        */
    /* ------------------------------------------------------------------ */


    @Bean
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }
}
