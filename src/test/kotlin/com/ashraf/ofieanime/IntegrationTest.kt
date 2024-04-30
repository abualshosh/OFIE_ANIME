package com.ashraf.ofieanime

import com.ashraf.ofieanime.config.AsyncSyncConfiguration
import com.ashraf.ofieanime.config.EmbeddedElasticsearch
import com.ashraf.ofieanime.config.EmbeddedSQL
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [OfieAnimeApp::class, AsyncSyncConfiguration::class])
@EmbeddedElasticsearch
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
annotation class IntegrationTest
