package gecko10000.geckopipes.guis

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.inventorygui.GUI
import gecko10000.geckolib.inventorygui.InventoryGUI
import gecko10000.geckolib.inventorygui.ItemButton
import gecko10000.geckopipes.GeckoPipes
import gecko10000.geckopipes.PipeEndManager
import gecko10000.geckopipes.model.PipeEnd
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject

class PipeEndGUI private constructor(private val block: Block) : MyKoinComponent, InventoryHolder {

    companion object {
        private const val SIZE = 45
        private const val IO_SLOT = 19
        private const val FILTER_SLOT = 16
        private const val EXACT_MATCH_SLOT = 34
        val FILTER_ITEM_SLOTS = listOf(
            12, 13, 14,
            21, 22, 23,
            30, 31, 32,
        )

        val openGUIs: MutableMap<Block, PipeEndGUI> = mutableMapOf()
        fun getOrCreate(block: Block): PipeEndGUI {
            openGUIs[block]?.let { return it }
            val new = PipeEndGUI(block)
            openGUIs[block] = new
            return new
        }
    }

    private val plugin: GeckoPipes by inject()
    private val pipeEndManager: PipeEndManager by inject()

    val gui: InventoryGUI
    val pipeEnd: PipeEnd
        get() = pipeEndManager.loadedPipeEnds.getValue(block)

    init {
        gui = createInventory()
        gui.setOnDestroy { openGUIs.remove(block) }
    }

    private fun ioButton(): ItemButton {
        val material =
            if (pipeEnd.data.isOutput) plugin.config.outputDisplayMaterial else plugin.config.inputDisplayMaterial
        val item = ItemStack.of(material)
        item.editMeta {
            it.itemName(
                parseMM(
                    if (pipeEnd.data.isOutput) "<dark_aqua><b>Output"
                    else "<dark_aqua><b>Input"
                )
            )
            it.lore(
                listOf(
                    parseMM(
                        if (pipeEnd.data.isOutput) "<dark_green>The pipe will push items into the container."
                        else "<dark_green>The pipe will pull items from the container."
                    ),
                    Component.empty(),
                    parseMM("<dark_green><green>Click</green> to toggle.")
                )
            )
        }
        return ItemButton.create(item) { e ->
            if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return@create
            pipeEndManager.updateData(pipeEnd) { it.copy(isOutput = !it.isOutput) }
            updateInventory()
        }
    }

    private fun filterButton(): ItemButton {
        val material = if (pipeEnd.data.isWhitelist) Material.WHITE_CONCRETE else Material.BLACK_CONCRETE
        val item = ItemStack.of(material)
        item.editMeta {
            it.itemName(
                parseMM(
                    if (pipeEnd.data.isWhitelist) "<dark_aqua><b>Whitelist"
                    else "<dark_aqua><b>Blacklist"
                )
            )
            it.lore(
                listOf(
                    parseMM(
                        if (pipeEnd.data.isWhitelist) "<dark_green>Only the items in the filter are included."
                        else "<dark_green>Items in the filter are excluded."
                    ),
                    Component.empty(),
                    parseMM("<dark_green><green>Click</green> to toggle.")
                )
            )
        }
        return ItemButton.create(item) { e ->
            if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return@create
            pipeEndManager.updateData(pipeEnd) { it.copy(isWhitelist = !it.isWhitelist) }
            updateInventory()
        }
    }

    private fun exactMatchButton(): ItemButton {
        val material = if (pipeEnd.data.isExactMatch) Material.ENCHANTED_BOOK else Material.BOOK
        val item = ItemStack.of(material)
        item.editMeta {
            it.itemName(
                parseMM(
                    if (pipeEnd.data.isExactMatch) "<dark_aqua><b>Exact Match"
                    else "<dark_aqua><b>Fuzzy Match"
                )
            )
            it.lore(
                listOf(
                    parseMM(
                        if (pipeEnd.data.isExactMatch) "<dark_green>This will match items exactly."
                        else "<dark_green>This will match items by item type."
                    ),
                    Component.empty(),
                    parseMM("<dark_green><green>Click</green> to toggle."),
                )
            )
        }
        return ItemButton.create(item) { e ->
            if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return@create
            pipeEndManager.updateData(pipeEnd) { data ->
                val newExactMatchValue = !data.isExactMatch
                // Make items plain if we're setting it to be material match
                val newFilterList = if (!newExactMatchValue) {
                    data.filter.map { it.type }.toSet().map { ItemStack.of(it) }
                } else data.filter
                data.copy(filter = newFilterList, isExactMatch = newExactMatchValue)
            }
            updateInventory()
        }
    }

    fun updateInventory(gui: InventoryGUI = this.gui) {
        gui.fill(0, SIZE, GUI.FILLER)
        gui.addButton(IO_SLOT, ioButton())
        gui.addButton(FILTER_SLOT, filterButton())
        gui.addButton(EXACT_MATCH_SLOT, exactMatchButton())
        FILTER_ITEM_SLOTS.forEachIndexed { index, slot ->
            val filterItem = pipeEnd.data.filter.getOrNull(pipeEnd.data.filter.size - 1 - index)
            gui.inventory.setItem(slot, filterItem)
        }
    }

    fun createInventory(): InventoryGUI {
        val gui = InventoryGUI(plugin.server.createInventory(this, SIZE, plugin.config.pipeGUIName))
        updateInventory(gui)
        return gui
    }

    override fun getInventory(): Inventory {
        return gui.inventory
    }

}
