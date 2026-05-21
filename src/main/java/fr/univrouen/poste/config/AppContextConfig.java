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

import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.LogService;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * JavaConfig principal remplaçant applicationContext.xml.
 */
@Configuration
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableAspectJAutoProxy
@ComponentScan(
        basePackages = "fr.univrouen.poste",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        classes = org.springframework.stereotype.Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        classes = org.springframework.web.bind.annotation.ControllerAdvice.class),
                @ComponentScan.Filter(type = FilterType.REGEX,
                        pattern = "fr\\.univrouen\\.poste\\.config\\.(WebMvcConfig|SecurityConfig|MethodSecurityConfig)")
        }
)
@EnableJpaRepositories(basePackages = "fr.univrouen.poste.repository")
@PropertySources({
        @PropertySource("classpath:META-INF/spring/database.properties"),
        @PropertySource("classpath:META-INF/spring/email.properties"),
        @PropertySource("classpath:META-INF/spring/security.properties")
})
public class AppContextConfig {

    /* ------------------------------------------------------------------ */
    /* PropertySourcesPlaceholderConfigurer – doit être static             */
    /* ------------------------------------------------------------------ */

    @Bean
    public static org.springframework.context.support.PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();
    }

    /* ------------------------------------------------------------------ */
    /* DataSource                                                           */
    /* ------------------------------------------------------------------ */

    @Value("${database.driverClassName}")
    private String dbDriverClassName;

    @Value("${database.url}")
    private String dbUrl;

    @Value("${database.username}")
    private String dbUsername;

    @Value("${database.password}")
    private String dbPassword;

    @Bean(destroyMethod = "close")
    public BasicDataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbDriverClassName);
        ds.setUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1_800_000);
        ds.setNumTestsPerEvictionRun(3);
        ds.setMinEvictableIdleTimeMillis(1_800_000);
        ds.setValidationQuery("SELECT version();");
        return ds;
    }

    /* ------------------------------------------------------------------ */
    /* JPA / Transaction                                                    */
    /* ------------------------------------------------------------------ */

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceUnitName("persistenceUnit");
        emf.setDataSource(dataSource);
        // Important: charge le persistence.xml situé dans META-INF
        emf.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /* ------------------------------------------------------------------ */
    /* Security – encodeur de mots de passe                                */
    /* ------------------------------------------------------------------ */

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* ------------------------------------------------------------------ */
    /* Mail                                                                 */
    /* ------------------------------------------------------------------ */

    @Value("${email.host}")
    private String mailHost;

    @Value("${email.protocol}")
    private String mailProtocol;

    @Value("${email.port:25}")
    private int mailPort;

    @Value("${email.isEnabled}")
    private boolean emailEnabled;

    @Value("${email.username:}")
    private String mailUsername;

    @Value("${email.password:}")
    private String mailPassword;

    @Value("${email.starttls:false}")
    private boolean mailStartTls;

    @Value("${email.ssl:false}")
    private boolean mailSsl;

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailHost);
        sender.setProtocol(mailProtocol);
        sender.setPort(mailPort);
        if (!mailUsername.isEmpty()) {
            sender.setUsername(mailUsername);
            sender.setPassword(mailPassword);
        }
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", !mailUsername.isEmpty());
        props.put("mail.smtp.starttls.enable", mailStartTls);
        props.put("mail.smtp.ssl.enable", mailSsl);
        return sender;
    }

    @Bean
    public EmailService emailService(JavaMailSenderImpl mailSender, LogService logService) {
        EmailService service = new EmailService();
        service.setMailSender(mailSender);
        service.setLogService(logService);
        service.setIsEnabled(emailEnabled);
        return service;
    }

    /* ------------------------------------------------------------------ */
    /* Cache                                                                */
    /* ------------------------------------------------------------------ */

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(new ConcurrentMapCache("appliConfig")));
        return cacheManager;
    }

    /* ------------------------------------------------------------------ */
    /* HandlerMappingIntrospector – requis avec Spring 6                   */
    /* ------------------------------------------------------------------ */

    @Bean(name = "mvcHandlerMappingIntrospector")
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    /* ------------------------------------------------------------------ */
    /* MessageSource                                                        */
    /* ------------------------------------------------------------------ */

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("WEB-INF/i18n/messages", "WEB-INF/i18n/application");
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(0);
        return ms;
    }

    /* ------------------------------------------------------------------ */
    /* Async – exécuteur de tâches (@Async)                                */
    /* ------------------------------------------------------------------ */

    @Bean(name = "defaultExecutor")
    public ThreadPoolTaskExecutor defaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}

