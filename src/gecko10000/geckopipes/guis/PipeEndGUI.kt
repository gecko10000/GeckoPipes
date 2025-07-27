package gecko10000.geckopipes.guis

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckolib.inventorygui.GUI
import gecko10000.geckolib.inventorygui.InventoryGUI
import gecko10000.geckolib.inventorygui.ItemButton
import gecko10000.geckopipes.GeckoPipes
import gecko10000.geckopipes.PipeEndManager
import gecko10000.geckopipes.model.PipeEnd
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
        private const val FILTER_SLOT = 25

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
        return ItemButton.create(item) { e ->
            if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return@create
            pipeEndManager.updateData(pipeEnd) { it.copy(isOutput = !it.isOutput) }
            updateInventory()
        }
    }

    private fun filterButton(): ItemButton {
        val material = if (pipeEnd.data.isWhitelist) Material.WHITE_CONCRETE else Material.BLACK_CONCRETE
        val item = ItemStack.of(material)
        return ItemButton.create(item) { e ->
            if (e.click != ClickType.LEFT && e.click != ClickType.RIGHT) return@create
            pipeEndManager.updateData(pipeEnd) { it.copy(isWhitelist = !it.isWhitelist) }
            updateInventory()
        }
    }

    fun updateInventory(gui: InventoryGUI = this.gui) {
        gui.fill(0, SIZE, GUI.FILLER)
        gui.addButton(IO_SLOT, ioButton())
        gui.addButton(FILTER_SLOT, filterButton())
    }

    fun createInventory(): InventoryGUI {
        val gui = InventoryGUI(plugin.server.createInventory(this, SIZE, plugin.config.pipeGUIName))
        updateInventory(gui)
        return gui
    }

    override fun getInventory(): Inventory {
        TODO("Not yet implemented")
    }

}
