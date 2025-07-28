package gecko10000.geckoanvils.di

import gecko10000.geckopipes.GeckoPipes
import gecko10000.geckopipes.PipeEndManager
import gecko10000.geckopipes.PipeManager
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: GeckoPipes) = module {
    single { plugin }
    single(createdAtStart = true) { PipeEndManager() }
    single(createdAtStart = true) { PipeManager() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
