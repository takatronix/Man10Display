ffmpeg -y -f dshow -i video="OBS Virtual Camera" -vf scale=640:384 -f rawvideo -c:v mjpeg -qscale:v 1 -r 30 udp://man10:12345