package com.zientarski.multiroom;

import java.io.*;

public class LoopableFileInputStream {
    private final File file;
    private InputStream currentStream;

    public LoopableFileInputStream(final File file) throws IOException {
        this.file = file;
        resetStream();
    }

    private void resetStream() throws IOException {
        close();
        currentStream = new FileInputStream(file);
    }

    public void read(final byte[] buffer) throws IOException {
        read(buffer, 0);
    }

    private void read(final byte[] buffer, int writeOffset) throws IOException {
        writeOffset += currentStream.read(buffer, writeOffset, buffer.length-writeOffset);
        if(writeOffset < buffer.length - 1){
            resetStream();
            read(buffer, writeOffset);
        }
    }

    public void close() throws IOException {
        if(currentStream != null){
            currentStream.close();
        }
    }
}
