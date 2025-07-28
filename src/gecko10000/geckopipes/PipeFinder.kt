package gecko10000.geckopipes

import gecko10000.geckoanvils.di.MyKoinComponent
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.koin.core.component.inject
import java.util.*

class PipeFinder(private val startBlock: Block) : MyKoinComponent {

    companion object {
        private val adjacentFaces = setOf(
            BlockFace.UP, BlockFace.DOWN,
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
        )
    }

    private val plugin: GeckoPipes by inject()
    private val pipeEndManager: PipeEndManager by inject()

    private val isPipeCenter: (Block) -> Boolean = {
        it.type in plugin.config.pipeBlocks
    }

    val inputs: List<Block>
    val outputs: List<Block>

    init {
        val (inputs, outputs) = run()
        this.inputs = inputs
        this.outputs = outputs
    }

    // Returns only the pipe ends
    // since that's what we need.
    private fun bfs(block: Block): List<Block> {
        val found = mutableListOf(block)
        val visited = mutableSetOf(block)
        val queue = ArrayDeque(setOf(block))
        while (queue.isNotEmpty()) {
            val block = queue.poll()
            for (face in adjacentFaces) {
                val neighbor = block.getRelative(face)
                if (!visited.add(neighbor)) continue
                if (isPipeCenter(neighbor)) {
                    queue.add(neighbor)
                } else if (pipeEndManager.loadedPipeEnds.containsKey(neighbor)) {
                    found.add(neighbor)
                }
            }
        }
        return found
    }

    private fun run(): Pair<List<Block>, List<Block>> {
        val found = bfs(startBlock)
        val inputs = mutableListOf<Block>()
        val outputs = mutableListOf<Block>()
        for (block in found) {
            if (pipeEndManager.loadedPipeEnds.getValue(block).data.isOutput) {
                outputs += block
            } else {
                inputs += block
            }
        }
        return inputs to outputs
    }

}
