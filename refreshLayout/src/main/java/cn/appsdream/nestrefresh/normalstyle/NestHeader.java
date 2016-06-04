package cn.appsdream.nestrefresh.normalstyle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.anzewei.pagelist.R;


/**
 * ClassName : PullHeader <br>
 * 功能描述： <br>
 * History <br>
 * Create User: An Zewei <br>
 * Create Date: 2013-12-31 下午1:29:19 <br>
 * Update User: <br>
 * Update Date:
 */
public class NestHeader extends RelativeLayout implements NestRefreshLayout.LoaderDecor {

    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private int mState = STATE_NORMAL;
    private String mstrTitle;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;
    private TextPaint mPaint = new TextPaint();

    public NestHeader(Context context) {
        this(context, null);
    }


    public NestHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public NestHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.pull_header, this);
        if (isInEditMode()) {
            return;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        int padding = (int) (15 / scale + 0.5f);
        setPadding(0, padding, 0, padding);
        int[] attsRes = new int[]{R.attr.pullDrawable};
        final TypedArray a = context.obtainStyledAttributes(attrs, attsRes, defStyle, R.style.pull_style);
        Drawable pullDrawable = a.getDrawable(0);
        a.recycle();
        mstrTitle = getResources().getString(R.string.loader_pull_load);
        mArrowImageView = (ImageView) findViewById(R.id.header_arrow);
        TextView hintText = (TextView) findViewById(R.id.header_hint);
        mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);
        mArrowImageView.setImageDrawable(pullDrawable);
        mPaint.setTextSize(hintText.getTextSize());
        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAntiAlias(true);
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    public void scrollRate(int y) {
//        float rate = y - mScrollY;
//        rate = rate * 180f / mArrowImageView.getMeasuredHeight();
//        mScrollY = y;
//        float centerX = mArrowImageView.getMeasuredWidth() / 2;
//        float centerY = mArrowImageView.getMeasuredHeight() / 2;
//        Matrix matrix = mArrowImageView.getImageMatrix();
//        mArrowImageView.setScaleType(ScaleType.MATRIX);
//        matrix.postRotate(rate, centerX, centerY);
//        mArrowImageView.setImageMatrix(matrix);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mArrowImageView.setRotation(y * 180f);
//        }
    }

    public void setState(int state) {
        if (state == mState)
            return;
        if (state == STATE_REFRESHING) { // 显示进度
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else if(state == STATE_SUCCESS){
            mArrowImageView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
        }else {// 显示箭头图片
            mArrowImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_READY) {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                } else if (mState == STATE_REFRESHING) {
                    mArrowImageView.clearAnimation();
                }
//            mHintTextView.setText(R.string.pinter_pull_load);
                mstrTitle = getResources().getString(R.string.loader_pull_load);
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
//                mHintTextView.setText(R.string.pinter_pull_ready);
                    mstrTitle = getResources().getString(R.string.loader_pull_ready);
                }
                break;
            case STATE_REFRESHING:
//            mHintTextView.setText(R.string.pinter_loading);
                mstrTitle = getResources().getString(R.string.loader_loading);
                break;
            case STATE_SUCCESS:
//            mHintTextView.setText(R.string.pinter_loading);
                mstrTitle = getResources().getString(R.string.loader_success);
                break;
            default:
        }

        mState = state;
    }

    /**
     * @see android.view.ViewGroup#dispatchDraw(Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mState == STATE_REFRESHING) {
//
//            float centerX = mArrowImageView.getMeasuredWidth() / 2;
//            float centerY = mArrowImageView.getMeasuredHeight() / 2;
//            Matrix matrix = mArrowImageView.getImageMatrix();
//            mArrowImageView.setScaleType(ScaleType.MATRIX);
//            matrix.postRotate(25, centerX, centerY);
//            mArrowImageView.setImageMatrix(matrix);
        }
        super.dispatchDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        FontMetrics fontMetrics = mPaint.getFontMetrics();
        // 计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字baseline
        float textBaseY = getHeight() - (getHeight() - fontHeight) / 2 - fontMetrics.bottom;
        canvas.drawText(mstrTitle, getWidth() / 2 + mArrowImageView.getMeasuredWidth(), textBaseY, mPaint);
    }

}
