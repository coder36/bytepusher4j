package coder36;

import java.io.*;
import java.util.Arrays;

/**
 * 
 * @author Mark Middleton
 *
 */
public class BytePusherVM {
	char[] mem = new char[0xFFFFFF];
	BytePusherIODriver ioDriver;

	public BytePusherVM(BytePusherIODriver ioDriver) {
		this.ioDriver = ioDriver;
	}

	// load rom into memory
	public void load(String rom) {
		try {
			mem = new char[0xFFFFFF];
			FileInputStream fs = null;
			try {
				 fs = new FileInputStream(rom);
				int pc = 0;
				int i = 0;
				while ((i = fs.read()) != -1) {
					mem[pc++] = (char)i;
				}
			}
			finally { if( fs != null ) fs.close(); };
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

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

	int getVal(int pc, int length) {
		int v = 0;
		for (int i = 0; i < length; i++) {
			v = (v << 8) + mem[pc++];
		}
		return v;
	}

	char[] copy(int start, int length) {
		return Arrays.copyOfRange(mem, start, start + length);
	}
}
