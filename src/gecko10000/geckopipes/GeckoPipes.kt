package gecko10000.geckopipes

import gecko10000.geckoanvils.di.MyKoinContext
import gecko10000.geckolib.config.YamlFileManager
import gecko10000.geckopipes.config.Config
import gecko10000.geckopipes.guis.PipeEndGUIListener
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class GeckoPipes : JavaPlugin() {

    val pipeEndKey = NamespacedKey(this, "pipe_end")

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

    fun pipeItem(): ItemStack {
        val item = ItemStack.of(Material.CAULDRON)
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        item.editPersistentDataContainer {
            it.set(pipeEndKey, PersistentDataType.BOOLEAN, true)
        }
        return item
    }

}
