package gecko10000.geckopipes

import gecko10000.geckopipes.di.MyKoinComponent
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.strokkur.commands.annotations.Aliases
import net.strokkur.commands.annotations.Command
import net.strokkur.commands.annotations.Executes
import net.strokkur.commands.annotations.Permission
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.inject

@Command("geckopipes")
@Aliases("gp")
@Permission("geckopipes.command")
class CommandHandler : MyKoinComponent {

    private val plugin: GeckoPipes by inject()
    private val pipeEndManager: PipeEndManager by inject()

    fun register() {
        plugin.lifecycleManager
            .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(LifecycleEventHandler { event ->
                CommandHandlerBrigadier.register(
                    event.registrar()
                )
            }))
    }

    private fun give(sender: CommandSender, target: Player) {
        target.give(pipeEndManager.pipeItem())
    }

    @Executes("give")
    @Permission("geckopipes.give")
    fun giveSelf(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendRichMessage("<red>Specify a target!")
            return
        }
        give(sender, sender)
    }

    @Executes("give")
    @Permission("geckopipes.give.other")
    fun giveOther(sender: CommandSender, target: Player) {
        give(sender, target)
    }

    @Executes("reload")
    @Permission("geckopipes.reload")
    fun reload(sender: CommandSender) {
        plugin.reloadConfigs()
        sender.sendRichMessage("<green>Config reloaded.")
    }

}
