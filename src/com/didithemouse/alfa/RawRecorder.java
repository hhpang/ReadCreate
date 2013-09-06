package com.didithemouse.alfa;

import java.io.File;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

//http://andrewbrobinson.com/2011/11/27/capturing-raw-audio-data-in-android/

//http://rehearsalassist.svn.sourceforge.net/viewvc/rehearsalassist/android/trunk/src/urbanstew/RehearsalAssistant/RehearsalAudioRecorder.java?revision=86&content-type=text%2Fplain

public class RawRecorder {

	String filename = "";
	Thread thread;
	public RawRecorder(String _filename)
	{
		filename = _filename;
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				auxRecord();
			}
		});
	}
	public void record()
	{
		thread.start();
		looper = true;
	}
	
	boolean looper = true;
	public void stop()
	{
		looper = false;	
	}
	
	Runnable onFinish = null;
	public void setOnFinishListener(Runnable r)
	{
		onFinish = r;
	}
	
	void finishRecord()
	{
		// looper == true => finalizó por si mismo
		if (onFinish != null && looper == true) onFinish.run();
	}
	
	void auxRecord(){
		
		int sampleRate = 44100;
		int audioSource = MediaRecorder.AudioSource.MIC;
		int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    
		int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding);
	
        // Setup the audio recording machinery
        AudioRecord audioRecord = new AudioRecord(audioSource,
        sampleRate, channelConfiguration,
        audioEncoding, bufferSize);
 
        // The short and file buffers, this might not be the most
        // efficient way to do things, but since we're planning on
        // redirecting this data into an encoder in a later version
        // of this project, we're not worried about it.
 
        // 320 = 16kHz * 20ms - Number of frames of audio required.
        // 882 = 44.1Khz * 20 ms
        short[] buffer = new short[882];
        byte[] fileBuffer = new byte[882  * 2];
        audioRecord.startRecording();
 
        RandomAccessFile f = null;
        File fx = new File(filename);
        fx.delete();
        fx=null;
        
        try {
            f = new RandomAccessFile(filename,"rw");
            f.writeBytes("RIFF");
			f.writeInt(0); // Final file size not known yet, write 0 
			f.writeBytes("WAVE");
			f.writeBytes("fmt ");
			f.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
			f.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
			f.writeShort(Short.reverseBytes((short)1));// Number of channels, 1 for mono, 2 for stereo
			f.writeInt(Integer.reverseBytes(sampleRate)); // Sample rate
			f.writeInt(Integer.reverseBytes(sampleRate*2)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
			f.writeShort(Short.reverseBytes((short)2)); // Block align, NumberOfChannels*BitsPerSample/8
			f.writeShort(Short.reverseBytes((short)16)); // Bits per sample
			f.writeBytes("data");
			f.writeInt(0); 
        } catch (Exception e) {
        	
        }
 
        // Blocking loop uses about 40% of the CPU to do this.
        int sampleNumber = 0;
 
        // We'll capture 1000 samples of 20ms each,
        // giving us 20 seconds of audio data.
        //
        while(sampleNumber < 1000 && looper) {
            audioRecord.read(buffer, 0, 882);
 
            for(int i = 0; i < buffer.length; i++) {
                fileBuffer[i*2] = (byte)(buffer[i] & (short)0xFF);
                fileBuffer[i*2 + 1] = (byte)(buffer[i] >> 8);
            }
 
            try {
                f.write(fileBuffer);
            } catch (Exception e) {
            	
            }
 
            sampleNumber++;
        }
 
        try {
            int payloadSize = sampleNumber*fileBuffer.length;
            f.seek(4);
            f.writeInt(Integer.reverseBytes(36+payloadSize));
            f.seek(40);
            f.writeInt(Integer.reverseBytes(payloadSize));
            f.close();
            
            audioRecord.release();
            finishRecord();
        } catch (Exception e) {
        	
        }
	}
}
