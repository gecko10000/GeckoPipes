@file:UseSerializers(UUIDSerializer::class, InternalItemStackSerializer::class)

package gecko10000.geckopipes.config

import gecko10000.geckolib.config.serializers.InternalItemStackSerializer
import gecko10000.geckolib.config.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

@Serializable
data class PipeEndData(
    val direction: BlockFace,
    val isOutput: Boolean = false,
    val isWhitelist: Boolean = false,
    val filter: List<ItemStack> = listOf(),
)
