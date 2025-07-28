package gecko10000.geckopipes

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.isEmpty
import gecko10000.geckolib.misc.Task
import gecko10000.geckopipes.config.PipeEndData
import gecko10000.geckopipes.model.PipeEnd
import org.bukkit.block.Block
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject

class PipeManager : MyKoinComponent, Listener {

    private val plugin: GeckoPipes by inject()
    private val pipeEndManager: PipeEndManager by inject()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        Task.syncRepeating({ ->
            tick()
        }, plugin.config.pipeMoveInterval, plugin.config.pipeMoveInterval)
    }

    private fun endAcceptsItem(item: ItemStack, data: PipeEndData): Boolean {
        val inFilter = { i: ItemStack ->
            if (data.isExactMatch) {
                i.isSimilar(item)
            } else {
                i.type == item.type
            }
        }
        return if (data.isWhitelist) { // Whitelist: any match
            data.filter.any(inFilter)
        } else { // Blacklist: none match
            data.filter.none(inFilter)
        }
    }

    private fun firstAcceptableItem(inventory: Inventory, pipeInfo: PipeEndData): Int {
        for (i in 0..<inventory.size) {
            val item = inventory.getItem(i)
            if (item.isEmpty()) continue
            item!!
            if (endAcceptsItem(item, pipeInfo)) {
                return i
            }
        }
        return -1
    }

    private fun moveItems(inputEnd: PipeEnd, outputs: List<Block>) {
        val inputBlock = inputEnd.block.getRelative(inputEnd.data.direction)
        val inputInventory = (inputBlock.getState(false) as? InventoryHolder)?.inventory ?: return
        val slotToMove = firstAcceptableItem(inputInventory, inputEnd.data)
        if (slotToMove == -1) return
        val original = inputInventory.getItem(slotToMove)?.clone() ?: return
        var remaining = original.clone()
        var allMoved = false
        for (output in outputs) {
            val pipeEnd = pipeEndManager.loadedPipeEnds[output] ?: continue
            // Output accepts item
            if (!endAcceptsItem(remaining, pipeEnd.data)) continue
            val outputBlock = output.getRelative(pipeEnd.data.direction)
            // Ensure it's an inventory
            val outputInventory = (outputBlock.getState(false) as? InventoryHolder)?.inventory ?: continue
            val leftover = outputInventory.addItem(remaining).values.firstOrNull()
            if (leftover == null) {
                allMoved = true
                break
            }
            remaining = leftover
        }
        inputInventory.setItem(
            slotToMove, if (allMoved) inputInventory.getItem(slotToMove)?.add(-original.amount) else
                remaining
        )
    }

    private fun tickInput(block: Block, pipeCache: MutableMap<Block, PipeFinder>) {
        // We've already ticked another input in this pipe.
        // Let's not search again and just move to the closest output.
        val existingPipeFinder = pipeCache[block]
        if (existingPipeFinder != null) {
            moveItems(pipeEndManager.loadedPipeEnds.getValue(block), existingPipeFinder.outputs)
        }
        // Otherwise, do the search
        val pipeFinder = PipeFinder(block)
        moveItems(pipeEndManager.loadedPipeEnds.getValue(block), pipeFinder.outputs)
        pipeFinder.inputs.forEach { pipeCache[it] = pipeFinder }
    }

    fun tick() {
        val pipeCache = mutableMapOf<Block, PipeFinder>()
        for (block in pipeEndManager.loadedPipeEnds.keys) {
            if (pipeEndManager.loadedPipeEnds.getValue(block).data.isOutput) continue
            tickInput(block, pipeCache)
        }
    }

}
