package red.man10.display
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.function.Consumer
import javax.imageio.ImageIO

open class VideoCaptureServer(port:Int) : Thread() , AutoCloseable {
    private var running = true
    private var socket: DatagramSocket? = null
    private var frameConsumer: Consumer<BufferedImage>? = null
    private var portNo = port
    override fun close() {
        running = false
        socket?.let {
            it.disconnect()
            it.close()
        }
    }
    fun onFrame(consumer: Consumer<BufferedImage>) {
        frameConsumer = consumer
    }
     fun run2() {
        try {
            val buffer = ByteArray(2024 * 1024) // 1 mb
            socket = DatagramSocket(portNo)
            val packet = DatagramPacket(buffer, buffer.size)
            val output = ByteArrayOutputStream()
            while (running) {
                socket!!.receive(packet)
                val data = packet.data
                if (data[0].toInt() == -1 && data[1].toInt() == -40) { // FF D8 (start of file)
                    if (output.size() > 0) {
                        try {
                            val stream = ByteArrayInputStream(output.toByteArray())
                            val bufferedImage = ImageIO.read(stream)
                            bufferedImage?.let {
                                frameConsumer?.accept(it)
                            }
                        } catch (e: IOException) {
                        }
                        output.reset()
                    }
                }
                output.write(data, 0, packet.length)
                //System.out.println(String.format("%02X", data[0])+" "+String.format("%02X", data[1]));
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
     override fun run() {
        try {
            val buffer = ByteArray(3024 * 2024)

            socket = DatagramSocket(portNo)
            val packet = DatagramPacket(buffer, buffer.size)
            val output = ByteArrayOutputStream()
            var soi = 0 // start of image / SOI
            var eoi = 0 // end of image / EOI
            while (running) {
                socket!!.receive(packet)
                val data = packet.data
                val length = packet.length
                for (i in packet.offset until length) {
                    val b = data[i]
                    when (b) {
                        0xFF.toByte() -> {
                            if (soi % 2 == 0) soi++ // find next byte
                            if (eoi == 0) eoi++
                        }
                        0xD8.toByte() -> {
                            if (soi % 2 == 1) {
                                soi++               // first SOI found
                            }
                            if (soi == 4) {
                                // found another SOI, probably incomplete frame.
                                // discard previous data, restart with this SOI
                                output.reset()
                                output.write(0xFF)
                                soi = 2
                            }
                        }

                        0xD9.toByte() -> if (eoi == 1) eoi++ // EOI found
                        else -> {
                            // wrong byte, reset
                            if (soi == 1) soi = 0
                            if (eoi == 1) eoi = 0
                            if (soi == 3) soi--
                        }
                    }
                    output.write(b.toInt())
                    if (eoi == 2) { // image is complete
                        try {
                            val stream = ByteArrayInputStream(output.toByteArray())
                            val bufferedImage = ImageIO.read(stream)
                            bufferedImage?.let {
                                frameConsumer?.accept(it)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        // reset
                        output.reset()
                        soi = 0
                        eoi = 0
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deinit() {
        close()
    }
}

