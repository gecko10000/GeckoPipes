package gecko10000.geckopipes

import gecko10000.geckolib.config.YamlFileManager
import gecko10000.geckopipes.config.Config
import gecko10000.geckopipes.di.MyKoinContext
import gecko10000.geckopipes.guis.PipeEndGUIListener
import org.bukkit.plugin.java.JavaPlugin

class GeckoPipes : JavaPlugin() {

    private val configFile = YamlFileManager(
        configDirectory = this.dataFolder,
        initialValue = Config(),
        serializer = Config.serializer()
    )
    val config: Config
        get() = configFile.value

    override fun onEnable() {
        MyKoinContext.init(this)
        CommandHandler().register()
        PipeEndGUIListener()
    }

    fun reloadConfigs() {
        configFile.reload()
    }

}
