package gecko10000.geckopipes

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckolib.blockdata.BlockDataManager
import gecko10000.geckolib.misc.Task
import gecko10000.geckopipes.config.PipeEndData
import gecko10000.geckopipes.guis.PipeEndGUI
import gecko10000.geckopipes.model.PipeEnd
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.joml.Matrix4f
import org.joml.Vector3f
import org.koin.core.component.inject
import java.util.*
import java.util.function.Predicate
import kotlin.math.PI
import kotlin.random.Random

class PipeEndManager : MyKoinComponent, Listener {

    private val plugin: GeckoPipes by inject()
    private val json: Json by inject()

    private val bdm = BlockDataManager("pipe", PersistentDataType.STRING, false)
    private val internalPipeEnds = mutableMapOf<Block, PipeEnd>()
    val loadedPipeEnds: Map<Block, PipeEnd>
        get() = internalPipeEnds

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.worlds
            .flatMap { it.loadedChunks.toList() }
            .forEach { loadChunk(it) }
    }

    private fun getPipeInfo(block: Block): PipeEndData? {
        val existingDataString = bdm[block] ?: return null
        return json.decodeFromString<PipeEndData>(existingDataString)
    }

    companion object {

        private const val EPSILON = 0.01f

        // Scale x/z 1f + 2f / 16: Make the cauldron 1 pixel bigger on each side
        // Scale y 1f + 1f / 4: Make the cauldron envelop the bottom fourth of the container
        // translate -1f / 16: Center the cauldron
        private fun Matrix4f.finalTransformation() = this
            .scale(1f + 2f / 16, 1f + 1f / 4, 1f + 2f / 16)
            .translate(-1f / 16, 0f, -1f / 16)

        // Trial and error is a powerful thing
        private val matrixMappings = mapOf(
            BlockFace.UP to Matrix4f().finalTransformation(),
            BlockFace.DOWN to Matrix4f()
                .translate(Vector3f(0f, 1f, 1f))
                .rotate(PI.toFloat(), 1f, 0f, 0f)
                .finalTransformation(),
            BlockFace.SOUTH to Matrix4f()
                .translate(Vector3f(0f, 1f, 0f))
                .rotate(PI.toFloat() / 2, 1f, 0f, 0f)
                .finalTransformation(),
            BlockFace.NORTH to Matrix4f()
                .translate(Vector3f(0f, 0f, 1f))
                .rotate(3 * PI.toFloat() / 2, 1f, 0f, 0f)
                .finalTransformation(),
            BlockFace.WEST to Matrix4f()
                .translate(Vector3f(1f, 0f, 0f))
                .rotate(PI.toFloat() / 2, 0f, 0f, 1f)
                .finalTransformation(),
            BlockFace.EAST to Matrix4f()
                .translate(Vector3f(0f, 1f, 0f))
                .rotate(3 * PI.toFloat() / 2, 0f, 0f, 1f)
                .finalTransformation(),
        )
    }

    private fun getDisplayByPredicate(set: Set<UUID>, predicate: Predicate<Material>): BlockDisplay? {
        return set
            .map { plugin.server.getEntity(it) }
            .filterIsInstance<BlockDisplay>()
            .firstOrNull {
                predicate.test(it.block.material)
            }
    }

    private fun Matrix4f.addRandomization(random: Random): Matrix4f {
        val rand = random.nextFloat() * EPSILON - EPSILON / 2
        return this.scale(Vector3f(1f + rand))
            .translate(Vector3f(-rand))
    }

    private fun updateDisplay(block: Block): Set<UUID> {
        val existing = internalPipeEnds[block]
        val pipeEndData = getPipeInfo(block) ?: return emptySet()
        val existingCauldron = existing?.let { getDisplayByPredicate(existing.displays, { it == Material.CAULDRON }) }
        val cauldron = existingCauldron ?: block.world.spawn(block.location, BlockDisplay::class.java) {
            it.isPersistent = false
        }
        cauldron.block = Material.CAULDRON.createBlockData()
        // Just some primes
        val seed = block.location.blockX * 37 + block.location.blockY * 113 + block.location.blockZ * 83
        val random = Random(seed)
        val matrix = matrixMappings.getValue(pipeEndData.direction)
        cauldron.setTransformationMatrix(
            Matrix4f(matrix).addRandomization(random)
        )

        val existingIndicator = existing?.let {
            getDisplayByPredicate(existing.displays, {
                it == plugin.config.inputDisplayMaterial || it == plugin.config.outputDisplayMaterial
            })
        }
        val indicator = existingIndicator ?: block.world.spawn(block.location, BlockDisplay::class.java) {
            it.isPersistent = false
        }
        val indicatorMaterial =
            if (pipeEndData.isOutput) plugin.config.outputDisplayMaterial else plugin.config.inputDisplayMaterial
        indicator.block = indicatorMaterial.createBlockData()
        indicator.setTransformationMatrix(
            Matrix4f()
                .translate(Vector3f(EPSILON / 2))
                .scale(Vector3f(1f - EPSILON))
        )
        return setOf(cauldron, indicator).map { it.uniqueId }.toSet()
    }

    private fun loadChunk(chunk: Chunk) {
        bdm.getValuedBlocks(chunk).forEach { block ->
            val displays = updateDisplay(block)
            val data = getPipeInfo(block)!!
            internalPipeEnds[block] = PipeEnd(block, data, displays)
        }
    }

    private fun getPlacedFace(player: Player): BlockFace {
        val pitch = player.pitch
        if (pitch > 45) return BlockFace.UP
        if (pitch < -45) return BlockFace.DOWN
        return player.facing.oppositeFace
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun BlockPlaceEvent.onPlacePipeEnd() {
        // Only pipe ends
        if (!this.itemInHand.persistentDataContainer.has(plugin.pipeEndKey)) return
        val direction = getPlacedFace(player)
        val pipeInfo = PipeEndData(direction)
        bdm[this.blockPlaced] = json.encodeToString(pipeInfo)
        val displays = updateDisplay(this.blockPlaced)
        internalPipeEnds[this.blockPlaced] = PipeEnd(this.blockPlaced, pipeInfo, displays)
        // Otherwise you get a POI data mismatch, probably because server
        // expects a cauldron after the place event is complete.
        Task.syncDelayed { ->
            this.blockPlaced.type = Material.BARRIER
        }
    }

    fun updateData(pipeEnd: PipeEnd, block: (PipeEndData) -> PipeEndData) {
        val newData = block(pipeEnd.data)
        bdm[pipeEnd.block] = json.encodeToString(newData)
        val newDisplays = updateDisplay(pipeEnd.block)
        internalPipeEnds[pipeEnd.block] = pipeEnd.copy(data = newData, displays = newDisplays)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun PlayerInteractEvent.onUpdatePipeEndDirection() {
        // Right clicks on blocks
        if (this.action != Action.RIGHT_CLICK_BLOCK) return
        // with your main hand
        if (this.hand != EquipmentSlot.HAND) return
        val clickedBlock = this.clickedBlock ?: return
        // that are pipe ends
        val pipe = internalPipeEnds[clickedBlock] ?: return
        // and not a shift click
        if (player.isSneaking) return
        this.isCancelled = true
        PipeEndGUI.getOrCreate(clickedBlock).gui.open(player)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun PlayerInteractEvent.onBreakPipeEnd() {
        // Left clicks on blocks
        if (this.action != Action.LEFT_CLICK_BLOCK) return
        val clickedBlock = this.clickedBlock ?: return
        // that are pipe ends
        val pipe = internalPipeEnds.remove(clickedBlock) ?: return
        this.isCancelled = true
        clickedBlock.type = Material.AIR
        bdm.remove(clickedBlock)
        // Remove displays
        pipe.displays.forEach {
            clickedBlock.world.getEntity(it)?.remove()
        }
        // Close any open invs
        PipeEndGUI.openGUIs[clickedBlock]?.gui?.inventory?.close()
        // Drop item
        clickedBlock.world.dropItemNaturally(clickedBlock.location.toCenterLocation(), plugin.pipeItem())
    }

    @EventHandler
    private fun ChunkLoadEvent.onChunkLoad() = loadChunk(this.chunk)

}
