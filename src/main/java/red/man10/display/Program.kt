package red.man10.display

import org.bukkit.command.CommandSender
import java.io.File

const val DEFAULT_PROGRAM_DURATION = 10.0
class ProgramData{
    var command = ""
    var fileName = ""
    var duration = DEFAULT_PROGRAM_DURATION

    override fun toString(): String {
        return "$command,$duration,\"$fileName\""
    }
    fun load(str:String){
        val list = str.split(",")
        command = list[0]
        duration = list[1].toDouble()
        fileName = list[2].replace("\"","")
    }
}
class Program {
    private var data = mutableListOf<ProgramData>()
    var index = -1 // 実行中のindex

    init{
        createProgramDirectory()
    }

    fun load(programName: String): Boolean {
        val userdata = File(Main.plugin.dataFolder, File.separator + "programs")
        val f = File(userdata, File.separator + programName + ".csv")
        if (!f.exists()) {
            return false
        }
        val lines = f.readLines()
        data.clear()
        lines.forEach {
            val d = ProgramData()
            d.load(it)
            data.add(d)
        }
        return true
    }
    private fun getProgramList(): List<String> {
        val folder = File(Main.plugin.dataFolder, File.separator + "programs")
        val files = folder.listFiles()
        val list = mutableListOf<String>()
        for (f in files!!) {
            if (f.isFile) {
                var filename = f.name
                //      隠しファイルは無視
                if (filename.substring(0, 1).equals(".", ignoreCase = true)) {
                    continue
                }
                val point = filename.lastIndexOf(".")
                if (point != -1) {
                    filename = filename.substring(0, point)
                }
                list.add(filename)
            }
        }
        return list
    }
    private fun createProgramDirectory(){
        val userdata = File(Main.plugin.dataFolder, File.separator + "programs")
        if(!userdata.exists()){
            userdata.mkdir()
        }
    }
    fun showList(p: CommandSender): Boolean {
        p.sendMessage("§e§l========== Program List =========")
        getProgramList().forEachIndexed { index, s ->
            p.sendMessage("§e§l${index+1}: §f§l$s")
        }
        p.sendMessage("§e§l===================================")
        return true
    }
}