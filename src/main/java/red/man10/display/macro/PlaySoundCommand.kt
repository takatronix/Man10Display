package red.man10.display.macro

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.display.Display

class PlaySoundCommand(private var macroName: String, private var macroCommand: MacroCommand) : MacroCommandHandler() {
    override fun run(display: Display, players: List<Player>, sender: CommandSender?) {
        var sound = macroCommand.params[0]
        sound = sound.replace("\"", "")
        if (sound.isEmpty()) {
            players.forEach {
                it.stopAllSounds()
            }
            return
        }
        // volume and pitch are optional
        if (macroCommand.params.size == 1) {
            players.forEach {
                it.playSound(it.location, sound, 1.0f, 1.0f)
            }
            return
        }
        val volume = macroCommand.params[1].toFloat()
        val pitch = macroCommand.params[2].toFloat()
        players.forEach {
            it.playSound(it.location, sound, volume, pitch)
        }
    }
}