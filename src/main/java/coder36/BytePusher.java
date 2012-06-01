package coder36;

import java.util.Timer;
import java.util.List;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * Bytepusher implementation using the java Swing framework
 * @author Mark Middleton
 */
@SuppressWarnings("serial")
public class BytePusher extends JFrame {

	private BytePusherVM vm;
	private BytePusherIODriverImpl driver;

	private boolean hideHelp;
	private boolean paused;
	private boolean hideInfo;
	private Canvas c;
	private FrameTask frameTask;
	private int freq = 60;
	private FrameRate fr = new FrameRate();
	private String romsFolder = System.getProperty("user.dir") + "/roms";
	private int romIndex;
	private String rom;
	private KeyEvent keyPress;
	
	/**
	 * Entry point
	 * @param args
	 */
	public static void main( String [] args ) {
		BytePusher b = new BytePusher();
		b.setVisible(true);
	}	
	
	/**
	 * Constructor
	 */
	public BytePusher() {		
		setUpWindow();
		setUpVm();	
	}
	
	/**
	 * Create a JFrame with a single canvas within.  Setup a key listener which
	 * will record the keypress.  This will subsequently be handled by the FrameTask
	 * which is setup to run every 60th of a second.
	 */
	private void setUpWindow() {
		// create window
		setTitle( "Bytepusher for Java" );
		setLayout( new GridBagLayout() );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(256*3, 256*3) );
		c = new Canvas();
		getContentPane().add(c);
		c.setFocusable(false); // canvas must be mon focusable otherwise key listeners dont work
		c.setSize(new Dimension(256*3, 256*3) );
		pack();		
		c.createBufferStrategy(2);
		
		// register key listener which will be used to detect special jeys
		this.addKeyListener( new KeyAdapter() {			
			public void keyPressed(KeyEvent e) { 
				keyPress = e;
			}			
		});
		
		// when window is resized, also resize canvas
		this.addComponentListener( new ComponentAdapter() {		
			public void componentResized(ComponentEvent e) {
				// need to mess about with the height and the width, otherwise the edges are missing
				c.setSize( getWidth()-15, getHeight()-38 );			
			}
		});			
	}
	
	private void setUpVm() {
		// set up bytepusher vm
		driver = new BytePusherIODriverImpl();
		vm = new BytePusherVM( driver );;
		
		// register key listened which will be used by the driver
		this.addKeyListener( driver );
				
		// load first rom
		String [] l = getRoms();
		if ( l.length != 0 ) {
			rom = l[0];
			loadRom( rom );
		}
		
		// startup vm
		setFrequency(freq);			
	}
	
	/**
	 * Load ROM into VM.  
	 * @param rom
	 */
	private void loadRom( String rom ) {
		try {
			FileInputStream fis = new FileInputStream( rom );
			vm.load( fis );
			fis.close();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Set the CPU frequency
	 * @param f
	 */
	private void setFrequency( int f ) {
		// cancel any previous tasks
		if ( frameTask != null ) frameTask.cancel();
		// create new task
		frameTask = new FrameTask();
		new Timer().schedule(frameTask, 0, 1000/freq);
	}
	
	/**
	 * Scan romsFolder for files with pattern *.bytepusher
	 * @return List of roms
	 */
	private String [] getRoms() {
		File[] l = new File( romsFolder ).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				String f = pathname.getName().toLowerCase();
				return f.endsWith(".bytepusher" ); 
			}			
		});
		
		if ( l == null ) return new String[0];
		// convert to filenames
		List<String> names = new ArrayList<String>();
		for ( int i=0; i < l.length; i ++ ) {
			names.add( l[i].getPath() );
		}
		
		return names.toArray(new String [0]);
	}
	
	/**
	 * Handle special key presses
	 */
	private void handleSpecialKeys() {
		if ( keyPress == null ) return;
		
		switch( keyPress.getKeyCode() ) {
			case KeyEvent.VK_H: 
				hideHelp = hideHelp ? false : true; 
				break;
			case KeyEvent.VK_I: 
				hideInfo = hideInfo ? false : true; 
				break;						
			case KeyEvent.VK_P:
				paused = paused ? false : true; 
				break;
			case KeyEvent.VK_Q:
				System.exit(0);
				break;
			case KeyEvent.VK_R:
				freq=60;
				setFrequency(freq);
				break;				
			case KeyEvent.VK_PAGE_DOWN:
				if ( freq != 1 ) freq--;
				setFrequency(freq);
				break;
			case KeyEvent.VK_PAGE_UP:
				freq++;
				setFrequency( freq );
				break;		
			case KeyEvent.VK_RIGHT:
				String [] l = getRoms();
				if ( l.length != 0 ) {
					if ( romIndex < l.length - 1 ) {
						romIndex++;
					}
					rom = l[romIndex];
					loadRom( rom );				
				}
				break;
			case KeyEvent.VK_LEFT:
				l = getRoms();
	            if ( l.length != 0) {
	            	if (romIndex != 0) {
	                   	romIndex--;
	                }
	                rom = l[romIndex]; 
	                loadRom( rom );
	            }
	            break;
		}
		keyPress = null;
	}
	
	/**
	 * Write rom/cpu/fre to the display
	 * @param g
	 */
	private void drawInfo( Graphics g )
	{
		
		if ( hideInfo ) return;
		String info = "rom: " + rom + "\n" +
					  "cpu: " + fr.counter.get( "gpu" ).frameRate + "\n" +
		              "fre: " + freq;
		writeText( g, info, 3, 10);
	}
	
	/**
	 * Write help message to the display
	 * @param g
	 */
    private void drawHelp(Graphics g)
    {
        if (hideHelp) return;
        String help = "BytePusher for Java by Mark Middleton (http://coder36.blogspot.co.uk)\n\n" +
                      "Left Arrow:  Load Previous Rom\n" +
                      "Right Arrow: Load Next Rom\n" +
                      "P:           Pause\n" +
                      "H:           Toggle Help\n" +
                      "I:           Toggle Info\n" +
                      "Page Up:     Increase Frequency\n" +
                      "Page Down:   Decrease Frequency\n" +
                      "R:           Reset Frequency\n" +
                      "Q:           Quit\n" +
                      "ROM folder:  " + romsFolder + "\n\n" + 
                      "Full virtual machine specs can be found at http://esolangs.org/wiki/BytePusher";
        writeText( g, help, 100, 100);
        
    }
    
    /**
     * Writes a line of text to the display.  Allows new lines.
     * @param g
     * @param text the text
     * @param x coord
     * @param y coord
     */
    private void writeText( Graphics g, String text, int x, int y ) {    	    	
    	String [] lines = text.split("\n");
    	for ( int i=0; i < lines.length; i++ ) {
    		g.drawString( lines[i], x, y);
    		y=y+12;
    	}
    	
    }
    
    /**
     * TimerTask which is setup to fire every 60th of a second
     */
	private class FrameTask extends TimerTask {
		
		/**
		 * Runs the VM every 6-th of a second and renders graphics
		 */
		public void run() {
			fr.update("gpu");
			if ( !paused ) {
				vm.run();
				// render vm image to screen
				Graphics g = c.getBufferStrategy().getDrawGraphics();
				Font font = new Font("Courier New", Font.PLAIN, 12);
				g.setFont(font);					
				g.drawImage(driver.getDisplayImage(), 0, 0, c.getWidth(), c.getHeight(), null);				
				// render text to screen
				g.setColor(Color.WHITE);
				drawHelp( g );
				drawInfo( g );				
				// flip buffer 
				c.getBufferStrategy().show();
				g.dispose();
			}			
			handleSpecialKeys();
		}
	
	}    	
}
