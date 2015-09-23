package com.microcontrollerbg.tesy;

import java.io.BufferedReader;
import java.io.PrintStream;

import com.microcontrollerbg.tesy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * This activity displays an image on the screen. 
 * The image has three different regions that can be clicked / touched.
 * When a region is touched, the activity changes the view to show a different
 * image.
 *
 */

public class ImageAreasActivity extends Activity 
    implements View.OnTouchListener 
{

/**
 * Create the view for the activity.
 *
 */
	private Vibrator myVib;
	TcpSocketChannel schannel;

	LircClient client;
	String Remote = "Tesy";
	String Code;
@Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
	
    if (android.os.Build.VERSION.SDK_INT > 9) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
    ImageView iv = (ImageView) findViewById (R.id.image);
    if (iv != null) {
       iv.setOnTouchListener (this);
    }

//    toast ("Touch the screen to discover where the regions are.");
    myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
    
    try {
		client = new LircClient("192.168.2.1",8765, true, 3000);
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		
		e.printStackTrace();
	}
    
  
   
    
}

/**
 * Respond to the user touching the screen.
 * Change images to make things appear and disappear from the screen.
 *
 */    

public boolean onTouch (View v, MotionEvent ev) 
{
    boolean handledHere = false;

    final int action = ev.getAction();

    final int evX = (int) ev.getX();
    final int evY = (int) ev.getY();
    int nextImage = -1;			// resource id of the next image to display

    // If we cannot find the imageView, return.
    ImageView imageView = (ImageView) v.findViewById (R.id.image);
    if (imageView == null) return false;

    // When the action is Down, see if we should show the "pressed" image for the default image.
    // We do this when the default image is showing. That condition is detectable by looking at the
    // tag of the view. If it is null or contains the resource number of the default image, display the pressed image.
    Integer tagNum = (Integer) imageView.getTag ();
    int currentResource = (tagNum == null) ? R.drawable.p2_ship_default : tagNum.intValue ();

    // Now that we know the current resource being displayed we can handle the DOWN and UP events.

    switch (action) {
    case MotionEvent.ACTION_DOWN :
       if (currentResource == R.drawable.p2_ship_default) {
   //      nextImage = R.drawable.p2_ship_pressed;
    //	   toast ("Touch the screen to discover where the regions are.");

          handledHere = true;
       /*
       } else if (currentResource != R.drawable.p2_ship_default) {
         nextImage = R.drawable.p2_ship_default;
         handledHere = true;
       */
       } 
       
       int touchColor = getHotspotColor (R.id.image_areas, evX, evY);
       
       // Compare the touchColor to the expected values. Switch to a different image, depending on what color was touched.
       // Note that we use a Color Tool object to test whether the observed color is close enough to the real color to
       // count as a match. We do this because colors on the screen do not match the map exactly because of scaling and
       // varying pixel density.
       ColorTool ct = new ColorTool ();
       int tolerance = 25;
       nextImage = R.drawable.p2_ship_default;
       handledHere = true;
       if (ct.closeMatch (Color.RED, touchColor, tolerance)) 
    	   {
    	   nextImage = R.drawable.tesy_timer_pushed;
    	   myVib.vibrate(50);
    	  
    	 
    	   Code = "timer";
    	   Transmit();
    	     	   
    	   
    	   }
       else if (ct.closeMatch (Color.BLUE, touchColor, tolerance))
    	   {
    	   nextImage = R.drawable.tesy_temp_pushed;   myVib.vibrate(50);
    	  
    	   Code = "temp";
    	   Transmit();
    	   
    	   }
       else if (ct.closeMatch (Color.YELLOW, touchColor, tolerance)) {
    	   nextImage = R.drawable.tesy_on_off_pushed;   myVib.vibrate(50);
    	   Code = "power";
    	   Transmit();
       }
       else if (ct.closeMatch (Color.WHITE, touchColor, tolerance)) {
    	   nextImage = R.drawable.p2_ship_default;   myVib.vibrate(50);
       }
       
       else if (ct.closeMatch (Color.CYAN, touchColor, tolerance)) {
    	   nextImage = R.drawable.tesy_minus_pushed;   myVib.vibrate(50);
    	   Code = "minus";
    	   Transmit();
       }
       
       else if (ct.closeMatch (Color.MAGENTA, touchColor, tolerance)) {
    	   nextImage = R.drawable.tesy_plus_pushed;   myVib.vibrate(50);
    	   Code = "plus";
    	   Transmit();
    	  
       }
     
       // If the next image is the same as the last image, go back to the default.
       // toast ("Current image: " + currentResource + " next: " + nextImage);
       if (currentResource == nextImage) {
        
    	   nextImage = R.drawable.p2_ship_default;
       } 
       handledHere = true; 
       break;

    case MotionEvent.ACTION_UP :
       // On the UP, we do the click action.
       // The hidden image (image_areas) has three different hotspots on it.
       // The colors are red, blue, and yellow.
       // Use image_areas to determine which region the user touched.
    	  nextImage = R.drawable.p2_ship_default;
          handledHere = true;
       break;

    default:
       handledHere = false;
    } // end switch

    if (handledHere) {
 
       if (nextImage > 0) {
          imageView.setImageResource (nextImage);
          imageView.setTag (nextImage);
          
       }
    }
    return handledHere;
}   

/**
 * Resume the activity.
 */

@Override protected void onResume() {
    super.onResume();

 
}

/**
 * Handle a click on the Wglxy views at the bottom.
 *
 */    

public void onClickWglxy (View v) {
    Intent viewIntent = new Intent ("android.intent.action.VIEW", 
                                    Uri.parse("http://double-star.appspot.com/blahti/ds-download.html"));
    startActivity(viewIntent);
    
}


/**
 */
// More methods

/**
 * Get the color from the hotspot image at point x-y.
 * 
 */

public int getHotspotColor (int hotspotId, int x, int y) {
    ImageView img = (ImageView) findViewById (hotspotId);
    if (img == null) {
       Log.d ("ImageAreasActivity", "Hot spot image not found");
       return 0;
    } else {
      img.setDrawingCacheEnabled(true); 
      Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache()); 
      if (hotspots == null) {
         Log.d ("ImageAreasActivity", "Hot spot bitmap was not created");
         return 0;
      } else {
        img.setDrawingCacheEnabled(false);
        return hotspots.getPixel(x, y);
      }
    }
}

/**
 * Show a string on the screen via Toast.
 * 
 * @param msg String
 * @return void
 */
private void Transmit() {

	Thread thread = new Thread() {
		@Override
		public void run() {

			

			
				try {
					client.sendIr1Command(Remote,
							Code, 1);
					Thread.sleep(1000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
			//		toast("No connection!");
					e.printStackTrace();
				}
				
				} 
				

			

		
	};

	thread.start();

}

public void toast (String msg)
{
    Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_LONG).show ();
} // end toast

} // end class