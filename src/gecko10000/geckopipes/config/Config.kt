package gecko10000.geckopipes.config

import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
data class Config(
    val inputDisplayMaterial: Material = Material.LIGHT_BLUE_CONCRETE,
    val outputDisplayMaterial: Material = Material.ORANGE_CONCRETE,
)
