package coder36;

import java.util.HashMap;

/**
 * Utility class for recording framerates
 * @author Mark Middleton
 */
public class FrameRate {
	
	public HashMap<String, FrameCounter> counter = new HashMap<String, FrameCounter>();

	/**
	 * Constructor
	 */
    public class FrameCounter
    {
        public long elapsedTime = 0;
        public int frameCounter;
        public int frameRate;
        public long lastTime;
    }

    /**
     * Register an update to a frame counter
     * @param name frame counter name
     */
    public void update(String name)
    {
        if (!counter.containsKey(name))
        {
        	counter.put(name, new FrameCounter());
        }

        FrameCounter fc = counter.get(name); 
        
        fc.elapsedTime += System.currentTimeMillis()-fc.lastTime;
        fc.lastTime = System.currentTimeMillis();
        fc.frameCounter++;
        
        if ( fc.elapsedTime > 1000 ) {
        	fc.elapsedTime = 0;
        	fc.frameRate = fc.frameCounter; 
        	fc.frameCounter = 0;
        }
   
    }	

}
