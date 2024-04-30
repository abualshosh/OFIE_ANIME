package com.ashraf.ofieanime.config

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import tech.jhipster.config.JHipsterConstants
import java.util.*

class TestContainersSpringContextCustomizerFactory : ContextCustomizerFactory {

    private val log = LoggerFactory.getLogger(TestContainersSpringContextCustomizerFactory::class.java)

    companion object {
        private var elasticsearchBean: ElasticsearchTestContainer? = null
        private var devTestContainer: SqlTestContainer? = null
        private var prodTestContainer: SqlTestContainer? = null
    } override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return ContextCustomizer { context, _ ->
            val beanFactory = context.beanFactory
            var testValues = TestPropertyValues.empty()
            val sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL::class.java)
            if (null != sqlAnnotation) {
                log.debug("detected the EmbeddedSQL annotation on class {}", testClass.name)
                log.info("Warming up the sql database")
                if (context.environment.activeProfiles.contains("test${JHipsterConstants.SPRING_PROFILE_DEVELOPMENT}")) {
                    if (null == devTestContainer) {
                        try {
                            val containerClass = Class.forName("${javaClass.packageName}.PostgreSqlTestContainer") as (Class<out SqlTestContainer>)
                            devTestContainer = beanFactory.createBean(containerClass)
                            beanFactory.registerSingleton(containerClass.name, devTestContainer)
                            // (beanFactory as DefaultListableBeanFactory).registerDisposableBean(containerClass.name, devTestContainer)
                        } catch (e: ClassNotFoundException) {
                            throw RuntimeException(e)
                        }
                    }
                    devTestContainer?.let {
                        testValues = testValues.and("spring.datasource.url=" + it.getTestContainer().jdbcUrl + "")
                        testValues = testValues.and("spring.datasource.username=" + it.getTestContainer().username)
                        testValues = testValues.and("spring.datasource.password=" + it.getTestContainer().password)
                    }
                }
                if (context.environment.activeProfiles.asList().contains("test${JHipsterConstants.SPRING_PROFILE_PRODUCTION}")) {
                    if (null == prodTestContainer) {
                        try {
                            val containerClass = Class.forName("${javaClass.packageName}.PostgreSqlTestContainer") as Class<out SqlTestContainer>
                            prodTestContainer = beanFactory.createBean(containerClass)
                            beanFactory.registerSingleton(containerClass.name, prodTestContainer)
                            // (beanFactory as (DefaultListableBeanFactory)).registerDisposableBean(containerClass.name, prodTestContainer)
                        } catch (e: ClassNotFoundException) {
                            throw RuntimeException(e)
                        }
                    }
                    prodTestContainer?.let {
                        testValues = testValues.and("spring.datasource.url=" + it.getTestContainer().jdbcUrl + "")
                        testValues = testValues.and("spring.datasource.username=" + it.getTestContainer().username)
                        testValues = testValues.and("spring.datasource.password=" + it.getTestContainer().password)
                    }
                }
            }

            val elasticsearchAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedElasticsearch::class.java)
            if (null != elasticsearchAnnotation) {
                log.debug("detected the EmbeddedElasticsearch annotation on class {}", testClass.name)
                log.info("Warming up the elastic database")
                if (null == elasticsearchBean) {
                    elasticsearchBean = beanFactory.createBean(ElasticsearchTestContainer::class.java)
                    beanFactory.registerSingleton(ElasticsearchTestContainer::class.java.name, elasticsearchBean)
                    // (beanFactory as (DefaultListableBeanFactory)).registerDisposableBean(ElasticsearchTestContainer::class.java.name, elasticsearchBean)
                }
                elasticsearchBean?.let {
                    testValues =
                        testValues.and(
                            "spring.elasticsearch.uris=http://" + it.getElasticsearchContainer()?.httpHostAddress
                        )
                }
            }
            testValues.applyTo(context)
        }
    }
}
