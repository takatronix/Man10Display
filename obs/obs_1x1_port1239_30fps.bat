ffmpeg -y -f dshow -i video="OBS Virtual Camera" -vf scale=128:128 -f rawvideo -c:v mjpeg -qscale:v 1 -r 30 udp://127.0.0.1:1239