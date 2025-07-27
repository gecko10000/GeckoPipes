package gecko10000.geckopipes.model

import gecko10000.geckopipes.config.PipeEndData
import org.bukkit.block.Block
import java.util.*

data class PipeEnd(
    val block: Block,
    val data: PipeEndData,
    val displays: Set<UUID>
)
