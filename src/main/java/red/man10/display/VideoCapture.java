package red.man10.display;

import com.google.common.collect.EvictingQueue;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Comparator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.map.MapCanvas;
import java.awt.*;

import static org.bukkit.Bukkit.getLogger;

// https://stackoverflow.com/questions/21420252/how-to-receive-mpeg-ts-stream-over-udp-from-ffmpeg-in-java
class VideoCaptureUDPServer extends Thread {
    public boolean running = true;

    private final Logger logger = getLogger();

    private DatagramSocket socket;

    public void onFrame(BufferedImage frame) {

    }

    public void run() {
        try {
            byte[] buffer = new byte[1024 * 1024]; // 1 mb


            //logger.info("Listening to stream on port " + Main.configData.getStreamPort());
            socket = new DatagramSocket(1234);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            int soi = 0; // start of image / SOI
            int eoi = 0; // end of image / EOI
            while (running) {
                socket.receive(packet);

                byte[] data = packet.getData();

                int length = packet.getLength();
                for (int i = packet.getOffset(); i < length; i++) {
                    byte b = data[i];
                    switch (b) {
                        case (byte) 0xFF:
                            if (soi % 2 == 0) soi++; // find next byte
                            if (eoi == 0) eoi++;
                            break;
                        case (byte) 0xD8:
                            if (soi % 2 == 1) {
                                soi++; // first SOI found
                            }
                            if (soi == 4) {
                                // found another SOI, probably incomplete frame.
                                // discard previous data, restart with this SOI
                                output.reset();
                                output.write(0xFF);
                                soi = 2;
                            }
                            break;
                        case (byte) 0xD9:
                            if (eoi == 1) eoi++; // EOI found
                            break;
                        default:
                            // wrong byte, reset
                            if (soi == 1) soi = 0;
                            if (eoi == 1) eoi = 0;
                            if (soi == 3) soi--;
                            break;
                    }
                    output.write(b);
                    if (eoi == 2) { // image is complete
                        try {
                            ByteArrayInputStream stream = new ByteArrayInputStream(output.toByteArray());
                            BufferedImage bufferedImage = ImageIO.read(stream);
                            if (bufferedImage != null) {
                                onFrame(bufferedImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // reset
                        output.reset();
                        soi = 0;
                        eoi = 0;
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        running = false;
        if (socket != null)
            socket.close();
    }
}

public class VideoCapture extends Thread {
    public static final Set<ScreenPart> screens = new TreeSet<>(Comparator.comparingInt(to -> to.mapId));

    public int width;
    public int height;
    Main plugin;
    public static final Queue<BufferedImage> frameQueue = EvictingQueue.create(1);;

    VideoCaptureUDPServer videoCaptureUDPServer;
    private BufferedImage currentFrame;
    public VideoCapture(Main plugin, int width, int height) {
        this.plugin = plugin;
        this.width = width;
        this.height = height;

        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        videoCaptureUDPServer = new VideoCaptureUDPServer() {
            @Override
            public void onFrame(BufferedImage frame) {
                currentFrame = frame;
            }
        };
        videoCaptureUDPServer.start();
    }

    public void renderCanvas(ScreenPart screen, MapCanvas mapCanvas) {
        BufferedImage frame = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = frame.createGraphics();
        graphics.drawImage(currentFrame,0,0,null);

        var id = screen.mapId;
        var partId = screen.partId;

//        var w = Main.configData.getMapWidth();
        var w = 10;
        var x = partId % w;
        var y = partId / w;

        graphics.drawImage(currentFrame,x * -128,y * -128,null);

        switch (id) {
            case 0: graphics.drawImage(currentFrame,0,0,null); break;
            case 1: graphics.drawImage(currentFrame,-128,0,null); break;
            case 484: graphics.drawImage(currentFrame,0,0,null); break;

            //case 2: graphics.drawImage(currentFrame,-256,0,null); break;
            //case 3: graphics.drawImage(currentFrame,0,-128,null); break;
            //case 4: graphics.drawImage(currentFrame,-128,-128,null); break;
            //case 5: graphics.drawImage(currentFrame,-256,-128,null); break;
        }

        mapCanvas.drawImage(0,0, frame);
        graphics.dispose();
    }

    static public void render(ScreenPart screenPart){
        BufferedImage frame = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

        Bukkit.getLogger().info("rendering screen part " + screenPart.mapId + " partId" + screenPart.partId);

        Graphics2D graphics = frame.createGraphics();
       // graphics.drawImage(currentFrame,0,0,null);
        graphics.dispose();
    }

    public void cleanup() {
        videoCaptureUDPServer.cleanup();
    }
}
