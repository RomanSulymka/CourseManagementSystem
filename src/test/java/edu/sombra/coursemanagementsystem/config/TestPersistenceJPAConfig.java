package edu.sombra.coursemanagementsystem.config;

import org.springframework.context.annotation.ComponentScan;

//@TestConfiguration
//@EnableTransactionManagement
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan("edu.sombra.coursemanagementsystem.repository")
public class TestPersistenceJPAConfig {

    /*
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    @Primary
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, HibernateJpaVendorAdapter jpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("edu.sombra.coursemanagementsystem.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter);

        Properties properties = new Properties();
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }*/
}
