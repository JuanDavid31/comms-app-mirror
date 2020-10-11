package com.rallytac.engageandroid.legba.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.rallytac.engageandroid.R;

import java.text.DecimalFormat;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;

public class SwipeButton extends RelativeLayout {

    private TextView redCircle;
    private float initialY;
    private TextView centerText;
    private Context context;
    private SOSEmergencyListener sosEmergencyListener;

    public void setSosEmergencyListener(SOSEmergencyListener sosEmergencyListener){
        this.sosEmergencyListener = sosEmergencyListener;
    }

    public SwipeButton(Context context) {
        super(context);
        this.context = context;
        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(21)
    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        //Background
        RelativeLayout background = new RelativeLayout(context);

        background.setClipToPadding(false);
        background.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_button_layout_shape));
        background.setAlpha(0.9f);
        int padding = convertDpToPx(context, 3.8);
        background.setPadding(padding, padding, padding, padding);
        LayoutParams layoutParamsView = new LayoutParams(convertDpToPx(context, 66), convertDpToPx(context, 206));
        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(background, layoutParamsView);


        // Moving icon

        final TextView swipeButton = new TextView(context);
        this.redCircle = swipeButton;
        redCircle.setGravity(Gravity.CENTER);
        redCircle.setText("SOS");
        redCircle.setLetterSpacing(0.02f);
        redCircle.setTypeface(ResourcesCompat.getFont(context, R.font.call_of_ops_duty));
        redCircle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        redCircle.setPadding(15, 20, 15, 20);

        LayoutParams layoutParamsButton = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        int margin = convertDpToPx(context, 3.8);
        layoutParamsButton.setMargins(margin, margin, margin, margin);

        layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
        redCircle.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_button_shape));
        addView(swipeButton, layoutParamsButton);

        // Text

        final TextView centerText = new TextView(context);
        this.centerText = centerText;
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.vertical_text);
        animation.setFillAfter(true);
        centerText.setAlpha(0.9f);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        centerText.setText("Swipe Down");
        centerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        centerText.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans_regular));
        centerText.setRotation(-90);
        centerText.setTextColor(getResources().getColor(R.color.white05, null));
        centerText.setGravity(Gravity.FILL_VERTICAL);
        centerText.setPadding(35, 35, 35, 35);
        background.addView(centerText, layoutParams);


        setOnTouchListener(getButtonTouchListener());
    }

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        actionMove(event);
                        return true;
                    case MotionEvent.ACTION_UP:
                        moveButtonBack();
                        return true;
                }

                return false;
            }
        };
    }

    private void actionMove(MotionEvent event) {

        int paddingTop = 15;
        int paddingBottom = 15;
        int circleHeight = redCircle.getHeight();
        int buttonCenter = redCircle.getHeight() / 2; // 120
        DecimalFormat df = new DecimalFormat("0.00");

        float leftSide = redCircle.getY() - paddingTop;
        int rightSide = getHeight() - (paddingTop + paddingBottom + circleHeight);
        double percentage = leftSide / rightSide;
        double halfPercentage = (leftSide) / (rightSide / 2);

        if (initialY == 0) {
            initialY = redCircle.getY();
        }
        if (event.getY() > initialY + buttonCenter && event.getY() + buttonCenter < getHeight()) {
            redCircle.setY(event.getY() - buttonCenter);
        }

        if (halfPercentage < 1) {
            centerText.setText("Swipe Down");
            centerText.setAlpha(1 - (float) percentage);
        } else {
            centerText.setText("Let Go to cancel");
            centerText.setAlpha((float) halfPercentage - 1);
        }

        int padding = convertDpToPx(context, 3.8);

        if (event.getY() + buttonCenter > getHeight() - padding && redCircle.getY() + buttonCenter < getHeight() - padding) {
            redCircle.setY(getHeight() - redCircle.getHeight() - padding);
        }

        if (event.getY() < buttonCenter && redCircle.getY() > 0) {
            redCircle.setY(padding);
        }

        if(percentage > 0.90){
            sosEmergencyListener.onStart();
        }else{
            sosEmergencyListener.onStop();
        }
    }

/*    private void actionUp() {
        if (active) {
            //collapseButton();
        } else {
            initialButtonWidth = slidingButton.getWidth();

            if (slidingButton.getX() + slidingButton.getWidth() > getWidth() * 0.85) {
                //expandButton();
            } else {
                //moveButtonBack();
            }
        }
    }*/

    private void collapseButton() {
/*        //Creates the movement animation
        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                initialButtonWidth);

        //Moves the button
        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params =  slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });

        //Sets the state to inactive on animationEnd
        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                active = false;
                slidingButton.setImageDrawable(disabledDrawable);
            }
        });

        //Creates the fadeIn animation
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(objectAnimator, widthAnimator);
        animatorSet.start();*/
    }

    private void expandButton() {
/*        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(slidingButton.getX(), 0);

        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(x);
            }
        });


        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                getWidth());

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                active = true;
                slidingButton.setImageDrawable(enabledDrawable);
            }
        });

        animatorSet.playTogether(positionAnimator, widthAnimator);
        animatorSet.start();*/
    }

    private void moveButtonBack() {
        int initialPos = convertDpToPx(context, 3.8);
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(redCircle.getY(), 0 + initialPos);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float y = (Float) positionAnimator.getAnimatedValue();
                redCircle.setY(y);
            }
        });

        positionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                sosEmergencyListener.onFinish();
            }
        });



        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1);
        positionAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        centerText.setText("Swipe Down");
        animatorSet.start();
        sosEmergencyListener.onStop();
    }

    public interface SOSEmergencyListener {
        void onStart();
        void onStop();
        void onFinish();
    }
}
