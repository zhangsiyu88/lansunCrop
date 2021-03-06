package com.lansun.qmyo.view;

import com.lansun.qmyo.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;



public class CloudView extends SurfaceView implements SurfaceHolder.Callback,
		Runnable {
	private Canvas canvas;
	private float screen_width;
	private float screen_height;
	private Bitmap background; // 背景图片
	private Bitmap background2; // 背景图片
	private float bg_y; // 图片的坐标
	private float bg_y2;
	private float scalex;
	private float scaley;
	private SurfaceHolder sfh;
	private Paint paint;
	public boolean threadFlag = true;
	private Thread thread;
	public  Context mContext;

	public CloudView(Context context) {
		super(context);
		mContext = context;
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		sfh = this.getHolder();
		
		sfh.setFormat(PixelFormat.TRANSPARENT);
		sfh.addCallback(this);
		paint = new Paint();
//		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);   //设置画笔风格，空心或者实心。 
		paint.setStrokeWidth(5);  // 设置空心的边框宽度。 
	}

	public CloudView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
	}

	public CloudView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		screen_width = this.getWidth();
		screen_height = this.getHeight();
		sfh.setFixedSize((int)screen_width, (int)screen_height);
		initBitmap();
		thread = new Thread(this);
		thread.start();
	}
	
	 private Bitmap decodeResource(Resources resources, int id) {
         TypedValue value = new TypedValue();
         resources.openRawResource(id, value);
         BitmapFactory.Options opts = new BitmapFactory.Options();
         opts.inTargetDensity = value.density;
         return BitmapFactory.decodeResource(resources, id, opts);
 }
	
	public void initBitmap() {
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
//		options.inTargetDensity = displayMetrics.densityDpi;
//		options.inDensity = 96;
//		options.inScaled = true;
//		//getBitmapDensity()用于设置图片将要被显示的密度。
//		//options.inDensity = getBitmapDensity();
//		
//		options.inDensity = TypedValue.DENSITY_NONE;
//		options.inTargetDensity = TypedValue.DENSITY_NONE ;
		
	
		
//		background = BitmapFactory.decodeResource(getResources(),
//				R.drawable.cloud_1);
//		background2 = BitmapFactory.decodeResource(getResources(),
//				R.drawable.cloud_2);
		
		background = decodeResource(getResources(),R.drawable.cloud_1);
		background2 = decodeResource(getResources(),R.drawable.cloud_2);
		
		scalex = screen_width / background.getWidth();
		scaley = screen_height / background.getHeight();
		bg_y = screen_height;
		bg_y2 = bg_y - screen_height;
	}

	@Override
	public void run() {
		while (threadFlag) {
			drawSelf();
			viewLogic(); // 背景移动的逻辑
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		threadFlag = false;
	}

	// 绘图方法
	public void drawSelf() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.WHITE); // 绘制背景色
				canvas.save();
				
				// 计算背景图片与屏幕的比例
				canvas.scale(scalex, scaley, 0, 0);
				canvas.drawBitmap(background, 0, bg_y, paint); // 绘制背景图
				canvas.drawBitmap(background2, 0, bg_y2, paint); // 绘制背景图
			}
		} catch (Exception err) {
			err.printStackTrace();
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	public void viewLogic() {

		bg_y -= 1.2;
		bg_y2 -= 1.2;
		// 当第一张图片的Y坐标超出屏幕，
		// 立即将其坐标设置到第二张图的上方
		if (bg_y < -screen_height) {
			bg_y = bg_y2 + background.getHeight() + 111;
			/*bg_y = bg_y2 + background.getHeight() + 10;*/
		}
		// 当第二张图片的Y坐标超出屏幕，
		// 立即将其坐标设置到第一张图的上方
		if (bg_y2 < -screen_height) {
			bg_y2 = bg_y + background.getHeight() + 111;
			/*bg_y2 = bg_y + background.getHeight() + 10;*/
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

}