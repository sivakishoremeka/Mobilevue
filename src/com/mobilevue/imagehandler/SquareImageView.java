package com.mobilevue.imagehandler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView
{
   public static float radius = 18.0f;  
    public SquareImageView(Context context)
    {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
    
    	  Path clipPath = new Path();
          RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
          clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
          canvas.clipPath(clipPath);
    	super.onDraw(canvas);
    }
}