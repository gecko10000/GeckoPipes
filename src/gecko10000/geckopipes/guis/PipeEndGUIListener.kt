package gecko10000.geckopipes.guis

import gecko10000.geckopipes.di.MyKoinComponent
import gecko10000.geckopipes.GeckoPipes
import gecko10000.geckopipes.PipeEndManager
import gecko10000.geckopipes.model.PipeEnd
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.koin.core.component.inject

class PipeEndGUIListener : MyKoinComponent, Listener {

    private val plugin: GeckoPipes by inject()
    private val pipeEndManager: PipeEndManager by inject()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun addFromInv(pipeEnd: PipeEnd, inventory: Inventory, slot: Int) {
        if (pipeEnd.data.filter.size >= plugin.config.maxFilterItems) return
        val clickedItem = inventory.getItem(slot) ?: return
        val shouldAdd: Boolean
        val itemToAdd: ItemStack
        if (pipeEnd.data.isExactMatch) {
            shouldAdd = !pipeEnd.data.filter.any { it.isSimilar(clickedItem) }
            itemToAdd = clickedItem.asOne()
        } else {
            shouldAdd = !pipeEnd.data.filter.any { it.type == clickedItem.type }
            itemToAdd = ItemStack(clickedItem.type)
        }
        if (!shouldAdd) return
        pipeEndManager.updateData(pipeEnd) {
            it.copy(filter = it.filter.plus(itemToAdd))
        }
        return
    }

    @EventHandler
    private fun InventoryClickEvent.onClickInPipeGUI() {
        val pipeEndGUI = inventory.getHolder(false) as? PipeEndGUI ?: return
        isCancelled = true
        if (click != ClickType.LEFT && click != ClickType.SHIFT_LEFT) return
        val clickedInv = clickedInventory
        if (clickedInv is PlayerInventory) {
            addFromInv(pipeEndGUI.pipeEnd, clickedInv, slot)
            pipeEndGUI.updateInventory()
            return
        }
        if (clickedInv?.getHolder(false) !is PipeEndGUI) return
        if (slot !in PipeEndGUI.FILTER_ITEM_SLOTS) return
        val itemToRemove = clickedInv.getItem(slot) ?: return
        pipeEndManager.updateData(pipeEndGUI.pipeEnd) {
            it.copy(filter = it.filter.minus(itemToRemove))
        }
        pipeEndGUI.updateInventory()
    }
}
