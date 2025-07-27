@file:UseSerializers(UUIDSerializer::class)

package gecko10000.geckopipes.config

import gecko10000.geckolib.config.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.block.BlockFace

@Serializable
data class PipeEndData(
    val direction: BlockFace,
    val isOutput: Boolean = false,
)
