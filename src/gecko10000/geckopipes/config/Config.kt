@file:UseSerializers(MMComponentSerializer::class)

package gecko10000.geckopipes.config

import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import org.bukkit.Material

@Serializable
data class Config(
    val inputDisplayMaterial: Material = Material.LIGHT_BLUE_CONCRETE,
    val outputDisplayMaterial: Material = Material.ORANGE_CONCRETE,
    val pipeGUIName: Component = MM.deserialize("<dark_green>Pipe Menu"),
)
