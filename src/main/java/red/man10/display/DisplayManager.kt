package red.man10.display

class DisplayManager {
    val displays = mutableListOf<Display>()

    fun addDisplay(display: Display) {
        displays.add(display)
    }

    fun printDisplays() {
        for (display in displays) {
            when (display) {
                is ImageDisplay -> {
                  //  println("red.man10.display.ImageDisplay with width ${display.width}, height ${display.height}, format ${display.imageFormat}")
                }
                is StreamDisplay -> {
                    println("red.man10.display.StreamDisplay with stream URL ${display.streamUrl}")
                }
                else -> {
                    println("Unknown display type")
                }
            }
        }
    }

}