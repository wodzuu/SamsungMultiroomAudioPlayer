package com.zientarski.multiroom;

import java.io.File;
import java.net.InetAddress;

public class Application {
    public static void main(String[] args) throws Exception {
        //final File audioFile = new File("D:\\Downloads\\DD_Beat_VTEcho_170-03.mp3");
        //final File audioFile = new File("D:\\Downloads\\Oberschlesien - Jo Chca  22. Przystanek Woodstock.mp3");
        final File audioFile = new File("D:\\Downloads\\Casio-MT-45-16-Beat.ogg");
        final HttpAudioServer audioServer = new HttpAudioServer(audioFile);
        final HttpSpeakerClient speakerClient = new HttpSpeakerClient(InetAddress.getByName("192.168.0.16"));
        audioServer.listen();
        speakerClient.play();
    }
}
