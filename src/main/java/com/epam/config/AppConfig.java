package com.epam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.epam")
@EnableTransactionManagement
public class AppConfig {



    //uncomment this if you want to use h2 instead of mysql and remove the original datasource entitymanagerfactory
    //also uncomment mysql driver in the pom and uncomment h2 driver dependency
    /**
     @Bean
     public DataSource dataSource() {
     DriverManagerDataSource dataSource = new DriverManagerDataSource();
     dataSource.setDriverClassName("org.h2.Driver");
     dataSource.setUrl("jdbc:h2:mem:gym_crm_db;DB_CLOSE_DELAY=-1"); // DB_CLOSE_DELAY=-1 keeps the DB alive
     dataSource.setUsername("sa");
     dataSource.setPassword("");
     return dataSource;
     }

     @Bean
     public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
     LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
     entityManagerFactoryBean.setDataSource(dataSource());
     entityManagerFactoryBean.setPackagesToScan("com.epam.domain");
     entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

     Properties properties = new Properties();
     properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
     // Changed the dialect for H2 compatibility
     properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
     properties.setProperty("hibernate.show_sql", "true"); // Optional: good for debugging

     entityManagerFactoryBean.setJpaProperties(properties);

     return entityManagerFactoryBean;
     }
     */


    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/gym_crm_db");
        dataSource.setUsername("springstudent");
        dataSource.setPassword("springstudent");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("com.epam.domain");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        entityManagerFactoryBean.setJpaProperties(properties);

        return entityManagerFactoryBean;
    }

    //transaction manager config
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();

        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return jpaTransactionManager;
    }

    //sql exception translation
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

}