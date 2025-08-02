@file:UseSerializers(MMComponentSerializer::class)

package gecko10000.geckopipes.config

import com.destroystokyo.paper.MaterialTags
import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import org.bukkit.Material

@Serializable
data class Config(
    val inputDisplayMaterial: Material = Material.LIGHT_BLUE_CONCRETE,
    val outputDisplayMaterial: Material = Material.ORANGE_CONCRETE,
    val pipeGUIName: Component = MM.deserialize("<dark_green>Pipe Funnel Options"),
    val maxFilterItems: Int = 9,
    val pipeBlocks: Set<Material> = MaterialTags.STAINED_GLASS.values,
    val pipeMoveInterval: Long = 20,
    val pipeItemName: Component = parseMM("<dark_aqua><b>Pipe Funnel"),
    val pipeItemLore: List<Component> = listOf(
        parseMM("<dark_green>Used for managing pipe"),
        parseMM("<dark_green>input and output."),
        Component.empty(),
        parseMM("<green>Place<dark_green> to use, facing into the"),
        parseMM("<dark_green>container you want to interact with.")
    )
)
