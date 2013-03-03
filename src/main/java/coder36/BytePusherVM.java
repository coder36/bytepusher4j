package coder36;

import java.io.*;
import java.util.Arrays;


/**
 * See: http://esolangs.org/wiki/BytePusher
 * 
 * BytePusher is a minimalist vitual machine:
 * Framerate: 60 frmaes per second
 * CPU:         ByteByteJunp with 3 byte addresses
 * CPU Speed:   65536 instructions per frame (3932160 instructions per second. ~3.93 MHz)
 * Memory:      16Mb RAM
 * Graphics:    256*256 pixels, 1 byte per pixel, 216 fixed colors
 * Sound:       8-bit mono, signed values, 256 samples per frame (15360 samples per second)
 *
 * Usage:
 * every 60th of a second {
 *   vm.run()
 * }
 * 
 * Author: Mark Middleton
 */
public class BytePusherVM {
	private char[] mem = new char[0xFFFFFF];
	private BytePusherIODriver ioDriver;

	public BytePusherVM(BytePusherIODriver ioDriver) {
		this.ioDriver = ioDriver;
	}

	/**
	 * Load ROM into memory
	 * @param rom
	 */
	public void load(InputStream rom) throws IOException {
		for ( int i=0; i < mem.length; i++ ) {
			mem[i] = 0;
		}
		int pc = 0;
		int i = 0;
		while ((i = rom.read()) != -1) {
			mem[pc++] = (char)i;
		}
	}

	/**
	 * CPU loop, to be called every 60th of a second
	 */
	public void run() {
		// run 65536 instructions
		short s = ioDriver.getKeyPress();
		mem[0] = (char) ((s & 0xFF00) >> 8);
		mem[1] = (char) (s & 0xFF);
		int i = 0x10000;
		int pc = getVal(2, 3);
		while (i-- != 0) {
			mem[getVal(pc + 3, 3)] = mem[getVal(pc, 3)];
			pc = getVal(pc + 6, 3);
		}
		ioDriver.renderAudioFrame(copy(getVal(6, 2) << 8, 256));
		ioDriver.renderDisplayFrame(copy(getVal(5, 1) << 16, 256 * 256));
	}

	private int getVal(int pc, int length) {
		int v = 0;
		for (int i = 0; i < length; i++) {
			v = (v << 8) + mem[pc++];
		}
		return v;
	}

	private char[] copy(int start, int length) {
		return Arrays.copyOfRange(mem, start, start + length);
	}
}
