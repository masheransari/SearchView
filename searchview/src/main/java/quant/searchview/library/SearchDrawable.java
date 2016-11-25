package quant.searchview.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cz on 11/25/16.
 * code from jjsearchview
 */
public class SearchDrawable extends Drawable{
    public static final int STATE_ANIM_NONE = 0;
    public static final int STATE_ANIM_START = 1;
    public static final int STATE_ANIM_STOP = 2;
    public static final int DEFAULT_ANIM_TIME = 400;
    public static final float DEFAULT_ANIM_STARTF = 0;
    public static final float DEFAULT_ANIM_ENDF = 1;

    @State
    protected int mState = STATE_ANIM_NONE;

    @IntDef({STATE_ANIM_NONE,STATE_ANIM_START, STATE_ANIM_STOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    protected float pro = -1;
    protected float[] pos = new float[2];
    private float cx, cy, cr, scr, scx, scy;
    private final RectF rectF, outRectF;
    private float sign = 0.707f;
    private final Paint paint;

    public SearchDrawable(float size) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(size);
        paint.setColor(Color.WHITE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        rectF = new RectF();
        outRectF = new RectF();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        int width = bounds.width();
        int height = bounds.height();
        cr = width / 3;
        scr = width / 4.5f;
        cx = width/ 2;
        cy = height / 2;
        scx = cx + cr * 2 * sign;
        scy = cy + (cr * 2 * sign - scr);
        rectF.left = scx - scr;
        rectF.right = scx + scr;
        rectF.top = scy - scr;
        rectF.bottom = scy + scr;
        outRectF.left = cx - cr;
        outRectF.right = cx + cr;
        outRectF.top = cy - cr;
        outRectF.bottom = cy + cr;
    }

    @Override
    public void draw(Canvas canvas) {
        switch (mState) {
            case STATE_ANIM_NONE:
                drawNormalView(paint, canvas);
                break;
            case STATE_ANIM_START:
                drawStopAnimView(paint, canvas);
                break;
            case STATE_ANIM_STOP:
                drawStartAnimView(paint, canvas);
                break;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public ValueAnimator startSearchViewAnim() {
        ValueAnimator valueAnimator = startSearchViewAnim(DEFAULT_ANIM_STARTF, DEFAULT_ANIM_ENDF,
                DEFAULT_ANIM_TIME);
        return valueAnimator;
    }

    public ValueAnimator startSearchViewAnim(float startF, float endF, long time) {
        ValueAnimator valueAnimator =startSearchViewAnim(startF, endF, time, null);
        return valueAnimator;
    }

    public ValueAnimator startSearchViewAnim(float startF, float endF, long time, final PathMeasure pathMeasure) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                pro = (float) valueAnimator.getAnimatedValue();
                if (null != pathMeasure){
                    pathMeasure.getPosTan(pro, pos, null);
                }
                invalidateSelf();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
        pro = 0;
        return valueAnimator;
    }

    private void drawStopAnimView(Paint paint, Canvas canvas) {
        canvas.save();
        if (pro <= 0.25) {
            canvas.drawLine(cx - cr, scy - scr, scx, scy - scr, paint);
            canvas.drawLine(cx - cr, scy - scr, cx - cr + scr * (0.25f - pro),
                    scy - scr - scr * (0.25f - pro), paint);
            canvas.drawLine(cx - cr, scy - scr, cx - cr + scr * (0.25f - pro),
                    scy - scr + scr * (0.25f - pro), paint);
        } else if (pro > 0.25 && pro <= 0.5f) {
            canvas.drawArc(rectF, -90, 180 * (pro - 0.25f) * 4, false, paint);
            canvas.drawLine(cx - cr + (scx - cx + cr) * (pro - 0.25f) * 4, scy - scr,
                    scx, scy - scr, paint);
        } else {
            canvas.drawLine(cx + cr * sign + cr * sign * (1 - (pro - 0.5f) * 2),
                    cy + cr * sign + cr * sign * (1 - (pro - 0.5f) * 2),
                    cx + cr * 2 * sign, cy + cr * 2 * sign, paint);
            canvas.drawArc(outRectF, 45, 720 * (pro - 0.5f), false, paint);
        }
        canvas.restore();
    }

    private void drawStartAnimView(Paint paint, Canvas canvas) {
        canvas.save();
        if (pro <= 0.75) {
            canvas.drawArc(outRectF, 45, 360 * (1 - pro / 0.75f), false, paint);
        }
        if (pro <= 0.25) {
            canvas.drawLine(cx + cr * sign + cr * sign * pro * 4, cy + cr * sign + cr * sign
                    * pro * 4, cx + cr * 2 * sign, cy + cr * 2 * sign, paint);
            canvas.drawArc(rectF, 90, -180 * pro * 4, false, paint);
        } else if (pro > 0.25 && pro <= 0.5f) {
            canvas.drawArc(rectF, -90, 180 * (1 - (pro - 0.25f) * 4), false, paint);
            canvas.drawLine(cx - cr * (pro - 0.25f) * 4, scy - scr, scx, scy - scr, paint);
        } else if (pro > 0.5f && pro < 0.75f) {
            canvas.drawLine(cx - cr * (pro - 0.5f) * 4, scy - scr, scx - 20, scy - scr, paint);
        } else {
            canvas.drawLine(cx - cr, scy - scr, scx - 20, scy - scr, paint);
            canvas.drawLine(cx - cr, scy - scr, cx - cr + scr * pro, scy - scr - scr * pro, paint);
            canvas.drawLine(cx - cr, scy - scr, cx - cr + scr * pro, scy - scr + scr * pro, paint);
        }
        canvas.restore();
    }

    private void drawNormalView(Paint paint, Canvas canvas) {
        canvas.save();
        canvas.drawCircle(cx, cy, cr, paint);
        canvas.drawLine(cx + cr * sign, cy + cr * sign, scx, cy + cr * 2 * sign, paint);
        canvas.restore();
    }

    public void startAnim() {
        if (mState == STATE_ANIM_START) return;
        mState = STATE_ANIM_START;
        startSearchViewAnim();
    }

    public void resetAnim() {
        if (mState == STATE_ANIM_STOP) return;
        mState = STATE_ANIM_STOP;
        startSearchViewAnim();
    }
}
