package gecko10000.geckopipes.model

import gecko10000.geckopipes.config.PipeEndData
import java.util.*

data class PipeEnd(
    val data: PipeEndData,
    val displays: Set<UUID>
)
