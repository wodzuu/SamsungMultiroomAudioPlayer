package com.zientarski.multiroom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpSpeakerClient {
    private static final int SPEAKER_PORT = 55001;
    private static final String SetNewPlaylistPlaybackControlTemplate = "<name>SetNewPlaylistPlaybackControl</name>" +
            "<p type=\"dec\" name=\"selcount\" val=\"1\" />" +
            "<p type=\"dec\" name=\"playtime\" val=\"0\" />" +
            "<p type=\"str\" name=\"type\" val=\"new\" />" +
            "<p type=\"str\" name=\"device_udn\" val=\"c2255a0e-6bd7-49bd-bb7c-11ed799fafd0\" />" +
            "<p type=\"str\" name=\"objectid\" val=\"759BE7368F126413F048177D26CD75FA\" />" +
            "<p type=\"cdata\" name=\"songtitle\" val=\"empty\"><![CDATA[Title]]></p>" +
            "<p type=\"cdata\" name=\"thumbnail\" val=\"empty\"><![CDATA[]]></p>" +
            "<p type=\"cdata\" name=\"artist\" val=\"empty\"><![CDATA[Artist]]></p>";

    private final InetAddress speakerIP;

    public HttpSpeakerClient(final InetAddress speakerIP) {
        this.speakerIP = speakerIP;
    }

    public void play() throws IOException, URISyntaxException {
        final URI uri = new URI("http", null, speakerIP.getHostAddress(), SPEAKER_PORT, "/UIC", "cmd=" + SetNewPlaylistPlaybackControlTemplate, null);
        HttpClient.get(uri.toURL());
    }
}
