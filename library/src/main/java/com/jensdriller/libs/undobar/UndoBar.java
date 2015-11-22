package com.jensdriller.libs.undobar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public class UndoBar {

    public enum Style {
        /**
         * The default style of the device's current API level.
         */
        DEFAULT(R.layout.undo_bar),
        /**
         * The default style for API Level <= 18.
         * <p/>
         * Example:<br>
         * <img src="https://camo.githubusercontent.com/3559ea695528c547ecdb918004b0c1df7ac83999/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74312e706e67" />
         * <br>
         * <img src="https://camo.githubusercontent.com/22ac172d0a9e1273b87d9164a99c6a0933996164/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74322e706e67" />
         */
        HOLO(R.layout.undo_bar_holo),
        /**
         * The default style for API Level 19 + 20.
         * <p/>
         * Example:<br>
         * <img src="https://camo.githubusercontent.com/bec5d8cf19564df3091cf5e2e77aff6760e88273/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74332e706e67" />
         * <br>
         * <img src="https://camo.githubusercontent.com/107d8ed2fd880038b1d4a71dec9bbd1e02fd58e7/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74342e706e67" />
         */
        KITKAT(R.layout.undo_bar_kitkat),
        /**
         * The default style for API Level >= 21.
         * <p/>
         * Example:<br>
         * <img src="https://camo.githubusercontent.com/a32255c0a1f5abe56607d46bb9782b8f338fd9e3/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74352e706e67" />
         * <br>
         * <img src="https://camo.githubusercontent.com/62d186f3ce9d55fa2b114b62887c714733155d5e/68747470733a2f2f7261772e6769746875622e636f6d2f6a656e7a7a2f416e64726f69642d556e646f4261722f6d61737465722f6173736574732f53637265656e73686f74362e706e67" />
         */
        LOLLIPOP(R.layout.undo_bar_lollipop);

        private final int mLayoutResId;

        private Style(int layoutResId) {
            mLayoutResId = layoutResId;
        }

        int getLayoutResId() {
            return mLayoutResId;
        }
    }

    /**
     * Listener for actions of the undo bar.
     */
    public interface Listener {
        /**
         * Will be fired when the undo bar disappears without being actioned.
         */
        void onHide();

        /**
         * Will be fired when the undo button is pressed.
         */
        void onUndo(Parcelable token);
    }

    /**
     * Default duration in milliseconds the undo bar will be displayed.
     */
    public static final int DEFAULT_DURATION = 5000;
    /**
     * Default duration in milliseconds of the undo bar show and hide animation.
     */
    public static final int DEFAULT_ANIMATION_DURATION = 300;

    protected Context mContext;
    protected UndoBarView mView;
    protected ViewCompat mViewCompat;
    protected final Handler mHandler = new Handler();

    private final Runnable mHideRunnable = new Runnable() {

        @Override
        public void run() {
            onHide();
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final OnClickListener mOnUndoClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onUndo();
        }
    };

    protected boolean mButtonVisible;
    protected Typeface mTypeface;
    protected Listener mUndoListener;
    protected Parcelable mUndoToken;
    protected CharSequence mUndoMessage;
    protected CharSequence mButtonLabel;
    protected Drawable mButtonDrawable;
    protected int mDuration = DEFAULT_DURATION;
    protected int mAnimationDuration = DEFAULT_ANIMATION_DURATION;
    protected boolean mUseEnglishLocale;
    protected Style mStyle = Style.DEFAULT;
    protected int mBkgColor = -1;
    protected int mUndoColor = Color.WHITE;
    protected boolean mAlignParentBottom;

    protected boolean isBackgroundColorCustomized;
    protected boolean isButtonDrawableCustomized;
    protected boolean isButtonLabelCustomized;
    protected boolean isTypefaceCustomized;


    public UndoBar(Context context){
        this((context instanceof Activity)?(Activity)context:null);
        if(mContext == null){
            mContext = context;
            mButtonVisible = false;
        }
    }

    public UndoBar(Context context, Style style) {
        this((context instanceof Activity)?(Activity)context:null,style);
        if(mContext == null){
            mContext = context;
            mButtonVisible = false;
        }
    }


    /**
     * Creates a new undo bar instance to be displayed in the given {@link Activity}.
     */
    public UndoBar(Activity activity) {
        this(activity.getWindow());
    }

    /**
     * Creates a new undo bar instance to be displayed in the given {@link Activity}.
     * <p/>
     * The style forces the the undo bar to match the look and feel of a certain API level.<br>
     * By default, it uses the style of the device's current API level.
     * <p/>
     * This is useful, for example, if you want to show a consistent
     * Lollipop style across all API levels.
     */
    public UndoBar(Activity activity, Style style) {
        this(activity.getWindow(), style);
    }

    /**
     * Creates a new undo bar instance to be displayed in the given {@link Dialog}.
     */
    public UndoBar(Dialog dialog) {
        this(dialog.getWindow());
    }

    /**
     * Creates a new undo bar instance to be displayed in the given {@link Dialog}.
     * <p/>
     * The style forces the the undo bar to match the look and feel of a certain API level.<br>
     * By default, it uses the style of the device's current API level.
     * <p/>
     * This is useful, for example, if you want to show a consistent
     * Lollipop style across all API levels.
     */
    public UndoBar(Dialog dialog, Style style) {
        this(dialog.getWindow(), style);
    }

    /**
     * Creates a new undo bar instance to be displayed in the given {@link Window}.
     */
    public UndoBar(Window window) {
        this(window, null);
    }

    /**
     * Creates a new undo bar instance to be displayed in the given {@link Window}.
     * <p/>
     * The style forces the the undo bar to match the look and feel of a certain API level.<br>
     * By default, it uses the style of the device's current API level.
     * <p/>
     * This is useful, for example, if you want to show a consistent
     * Lollipop style across all API levels.
     */
    public UndoBar(Window window, Style style) {
        if (style == null) {
            style = Style.DEFAULT;
        }

        mStyle = style;
        if(window != null) {
            mContext = window.getContext();
            mView = getView(window);
            mView.setOnUndoClickListener(mOnUndoClickListener);
            mViewCompat = new ViewCompatImpl(mView);

            hide(false);
        }
    }

    /**
     * Sets the background color of the undo bar;
     */
    public void setBackgroundColor(int color){
        isBackgroundColorCustomized = true;
        mBkgColor = color;
    }

    /**
     * Sets the message to be displayed on the left of the undo bar.
     */
    public void setMessage(CharSequence message) {
        mUndoMessage = message;
    }

    /**
     * Sets the message to be displayed on the left of the undo bar.
     */
    public void setMessage(int messageResId) {
        mUndoMessage = mContext.getString(messageResId);
    }

    /**
     * Sets the button(right of the undo bar) should be shown
     */
    public void setButtonVisible(boolean isVisible){
        if(mView != null) {
            mButtonVisible = isVisible;
        }
    }

    public void setTypeface(Typeface typeface){
        isTypefaceCustomized = true;
        mTypeface = typeface;
    }

    /**
     * Sets the message to be displayed on the right of the undo bar.
     */
    public void setButtonLabel(CharSequence buttonLabel){
        isButtonLabelCustomized = true;
        mButtonLabel = buttonLabel;
    }

    /**
     * Sets the message to be displayed on the right of the undo bar.
     */
    public void setButtonLabel(int buttonLabelRes){
        isButtonLabelCustomized = true;
        mButtonLabel = mContext.getString(buttonLabelRes);
    }

    /**
     * Sets the drawable to be displayed on the right of the undo bar;
     * can be ignored if Style is LOLLIPOP;
     */
    public void setButtonDrawable(Drawable buttonDrawable){
        isButtonDrawableCustomized = true;
        mButtonDrawable = buttonDrawable;
    }

    /**
     * Sets the drawable to be displayed on the right of the undo bar;
     * can be ignored if Style is LOLLIPOP;
     */
    public void setButtonDrawable(int buttonDrawableRes){
        isButtonDrawableCustomized = true;
        mButtonDrawable = mContext.getResources().getDrawable(buttonDrawableRes);
    }

    /**
     * Sets the {@link Listener UndoBar.Listener}.
     */
    public void setListener(Listener undoListener) {
        mUndoListener = undoListener;
    }

    /**
     * Sets a {@link Parcelable} token to the undo bar which will be returned in
     * the {@link Listener UndoBar.Listener}.
     */
    public void setUndoToken(Parcelable undoToken) {
        mUndoToken = undoToken;
    }

    /**
     * Sets the duration the undo bar will be shown.<br>
     * Default is {@link #DEFAULT_DURATION}.
     *
     * @param duration in milliseconds
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Sets the duration of the animation for showing and hiding the undo bar.<br>
     * Default is {@link #DEFAULT_ANIMATION_DURATION}.
     *
     * @param animationDuration in milliseconds
     */
    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    /**
     * Forces the English {@link java.util.Locale Locale} to be used explicitly.<br>
     * This means that the undo bar label will always show <b>UNDO</b>
     * regardless of the device's current {@link java.util.Locale Locale}.
     */
    public void setUseEnglishLocale(boolean useEnglishLocale) {
        mUseEnglishLocale = useEnglishLocale;
    }

    /**
     * Sets the text color of the undo button.<br>
     * The default color is white.<br>
     * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
     * style and ignored otherwise.
     */
    public void setUndoColor(int color) {
        mUndoColor = color;
    }

    /**
     * Sets the text color resource id of the undo button.<br>
     * The default color is white.<br>
     * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
     * style and ignored otherwise.
     */
    public void setUndoColorResId(int colorResId) {
        mUndoColor = mContext.getResources().getColor(colorResId);
    }

    /**
     * If set to {@code true}, the undo bar will appear stuck at the bottom without any margins.<br>
     * The default is {@code false}.<br>
     * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
     * style on devices with a smallest width of less than 600dp and ignored otherwise.
     */
    public void setAlignParentBottom(boolean alignParentBottom) {
        mAlignParentBottom = alignParentBottom;
    }

    private void setButtonLabelCustomized(boolean isCustomized) {
        isButtonLabelCustomized = isCustomized;
    }

    private void setButtonDrawableCustomized(boolean isCustomized) {
        isButtonDrawableCustomized = isCustomized;
    }

    private void setBackgroundColorCustomized(boolean isCustomized) {
        isBackgroundColorCustomized = isCustomized;
    }

    private void setTypefaceCustomized(boolean isCustomized) {
        isTypefaceCustomized = isCustomized;
    }

    /**
     * Calls {@link #show(boolean)} with {@code shouldAnimate = true}.
     */
    public void show() {
        show(true);
    }

    /**
     * Shows the {@link UndoBar}.
     *
     * @param shouldAnimate whether the {@link UndoBar} should animate in
     */
    public void show(boolean shouldAnimate) {
        if(mView == null){
            mButtonVisible = false;
        }
        if(mButtonVisible) {
            if (isBackgroundColorCustomized) {
                Drawable coloredBackground = mView.getBackground();
                coloredBackground.setColorFilter(mBkgColor, PorterDuff.Mode.SRC_IN);
                mView.setBackgroundDrawable(coloredBackground);
            }

            if (isTypefaceCustomized) {
                mView.setTypeface(mTypeface);
            }

            mView.setMessage(mUndoMessage);

            if (isButtonLabelCustomized) {
                mView.setButtonLabel(mButtonLabel);
            } else {
                mView.setButtonLabel(mUseEnglishLocale ? R.string.undo_english : R.string.undo);
            }

            if (isLollipopStyle(mStyle)) {
                mView.setUndoColor(mUndoColor);
                if (mAlignParentBottom && isAlignBottomPossible()) {
                    removeMargins(mView);
                }
            } else {
                if (isButtonDrawableCustomized) {
                    mView.setButtonDrawable(mButtonDrawable);
                }
            }

            mHandler.removeCallbacks(mHideRunnable);
            mHandler.postDelayed(mHideRunnable, mDuration);

            mView.setVisibility(View.VISIBLE);
            if (shouldAnimate) {
                animateIn();
            } else {
                mViewCompat.setAlpha(1);
            }
        }else{
            //I dreamed in a dream...
            //lets get back to stock-style Toast.
            View toastLayout = LayoutInflater.from(mContext).inflate(mStyle.getLayoutResId(), null);
            View divider = toastLayout.findViewById(R.id.divider);
            if(divider != null){
                divider.setVisibility(View.GONE);
            }
            (toastLayout.findViewById(R.id.button)).setVisibility(View.GONE);

            TextView tvMessage = (TextView) toastLayout.findViewById(R.id.message);
            tvMessage.setText(mUndoMessage);
            if (isBackgroundColorCustomized) {
                Drawable coloredBackground = toastLayout.getBackground();
                coloredBackground.setColorFilter(mBkgColor, PorterDuff.Mode.SRC_IN);
                toastLayout.setBackgroundDrawable(coloredBackground);
            }
            if(isTypefaceCustomized) {
                tvMessage.setTypeface(mTypeface);
            }

            Toast toast = new Toast(mContext);
            toast.setView(toastLayout);
            toast.setDuration(mDuration > DEFAULT_DURATION ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            if (isLollipopStyle(mStyle)) {
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.setMargin(0.0f, 0.0f);//but this don't makes us to margin bottom;
            }
            toast.show();

            ///for compatibility measures;
            mHandler.removeCallbacks(mHideRunnable);
            mHandler.postDelayed(mHideRunnable, mDuration);
        }
    }

    /**
     * Checks whether the given style is {@link Style#LOLLIPOP}.
     * Either explicitly set or the system default.
     */
    private boolean isLollipopStyle(Style style) {
        return style == Style.LOLLIPOP || (style == Style.DEFAULT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * Checks whether the given style is {@link Style#HOLO}.
     * Either explicitly set or the system default.
     */
    private boolean isHoloStyle(Style style) {
        return style == Style.HOLO || (style == Style.DEFAULT && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT);
    }

    /**
     * Checks whether aligning the undo bar at the bottom is possible
     * for the current device configuration.
     */
    private boolean isAlignBottomPossible() {
        return mContext.getResources().getBoolean(R.bool.is_align_bottom_possible);
    }

    /**
     * Removes any margins from the given view.
     */
    private static void removeMargins(View view) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = layoutParams.topMargin = layoutParams.rightMargin = layoutParams.bottomMargin = 0;
        view.setLayoutParams(layoutParams);
    }

    /**
     * Calls {@link #hide(boolean)} with {@code shouldAnimate = true}.
     */
    public void hide() {
        hide(true);
    }

    /**
     * Hides the {@link UndoBar}.
     *
     * @param shouldAnimate whether the {@link UndoBar} should animate out
     */
    public void hide(boolean shouldAnimate) {
        mHandler.removeCallbacks(mHideRunnable);

        if (shouldAnimate) {
            animateOut();
        } else {
            if(mViewCompat != null) {
                mViewCompat.setAlpha(0);
            }
            if(mView != null) {
                mView.setVisibility(View.GONE);
            }
            mUndoMessage = null;
            mUndoToken = null;
        }
    }

    /**
     * Checks if the undo bar is currently visible.
     *
     * @return {@code true} if visible, {@code false} otherwise
     */
    public boolean isVisible() {
        return mView.getVisibility() == View.VISIBLE;
    }

    /**
     * Performs the actual show animation.
     */
    protected void animateIn() {
        mViewCompat.animateIn(mAnimationDuration);
    }

    /**
     * Performs the actual hide animation.
     */
    protected void animateOut() {
        mViewCompat.animateOut(mAnimationDuration, new ViewCompat.AnimatorListener() {
            @Override
            public void onAnimationEnd() {
                mView.setVisibility(View.GONE);
                mUndoMessage = null;
                mUndoToken = null;
            }
        });
    }

    /**
     * Called when the undo bar disappears without being actioned.<br>
     * Hides the undo bar and notifies potential listener.
     */
    protected void onHide() {
        hide(true);
        safelyNotifyOnHide();
        mUndoListener = null;
    }

    /**
     * Called when the undo button is pressed.<br>
     * Hides the undo bar and notifies potential listener.
     */
    protected void onUndo() {
        hide(true);
        safelyNotifyOnUndo();
    }

    /**
     * Notifies listener if available.
     */
    protected void safelyNotifyOnHide() {
        if (mUndoListener != null) {
            mUndoListener.onHide();
        }
    }

    /**
     * Notifies listener if available.
     */
    protected void safelyNotifyOnUndo() {
        if (mUndoListener != null) {
            mUndoListener.onUndo(mUndoToken);
        }
    }

    /**
     * Checks if there is already an {@link UndoBarView} instance added to the
     * given {@link Window}.<br>
     * If {@code true}, returns that instance.<br>
     * If {@code false}, inflates a new {@link UndoBarView} and returns it.
     */
    protected UndoBarView getView(Window window) {
        ViewGroup decorView = (ViewGroup) window.getDecorView();

        // if we're operating within an Activity, limit ourselves to the content view.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        if (rootView == null) {
            rootView = decorView;
        }

        // if it's the first undo bar in this window or a different style, inflate a new instance
        UndoBarView undoBarView = (UndoBarView) rootView.findViewById(R.id.undoBar);
        if (undoBarView == null || undoBarView.getTag() != mStyle) {
            rootView.removeView(undoBarView); // remove potential undo bar w/ different style
            undoBarView = (UndoBarView) LayoutInflater.from(rootView.getContext())
                    .inflate(mStyle.getLayoutResId(), rootView, false);
            undoBarView.setTag(mStyle);
            rootView.addView(undoBarView);
        }

        return undoBarView;
    }

    public static class Builder {

        private Window mWindow = null;
        private final Context mCtx;

        private boolean mButtonVisible = true;
        private Typeface mTypeface;
        private CharSequence mUndoMessage;
        private CharSequence mButtonLabel;
        private Drawable mButtonDrawable;
        private Listener mUndoListener;
        private Parcelable mUndoToken;
        private int mDuration = DEFAULT_DURATION;
        private int mAnimationDuration = DEFAULT_ANIMATION_DURATION;
        private boolean mUseEnglishLocale;
        private Style mStyle;
        private int mBkgColor = -1;
        private int mUndoColor = Color.WHITE;
        private boolean mAlignParentBottom;

        private boolean isBackgroundColorCustomized = false;
        private boolean isButtonDrawableCustomized = false;
        private boolean isButtonLabelCustomized = false;
        private boolean isTypefaceCustomized = false;


        /**
         * Constructor using the {@link android.content.Context} in which the undo bar will be
         * displayed
         * in this mode, when context isn't instanceof Activity, only show in toast mode;
         */
        public Builder(Context context) {
            if (context instanceof Activity){
                mWindow = ((Activity) context).getWindow();
                mCtx = mWindow.getContext();
            }else{
                mCtx = context;
            }
        }

        /**
         * Constructor using the {@link android.app.Activity} in which the undo bar will be
         * displayed
         */
        public Builder(Activity activity) {
            mWindow = activity.getWindow();
            mCtx = mWindow.getContext();
        }

        /**
         * Constructor using the {@link android.app.Dialog} in which the undo bar will be
         * displayed
         */
        public Builder(Dialog dialog) {
            mWindow = dialog.getWindow();
            mCtx = mWindow.getContext();
        }

        /**
         * Constructor using the {@link Window} in which the undo bar will be
         * displayed
         */
        public Builder(Window window) {
            mWindow = window;
            mCtx = mWindow.getContext();
        }

        /**
         * Sets the message to be displayed on the left of the undo bar.
         */
        public Builder setMessage(int messageResId) {
            mUndoMessage = mCtx.getString(messageResId);
            return this;
        }

        /**
         * Sets the message to be displayed on the left of the undo bar.
         */
        public Builder setMessage(CharSequence message) {
            mUndoMessage = message;
            return this;
        }

        /**
         * Sets undo button's visibility;
         */
        public Builder setButtonVisible(boolean isVisible){
            mButtonVisible = isVisible;
            return this;
        }

        /**
         * Sets undo bar's typeface;
         */
        public Builder setTypeface(Typeface typeface){
            isTypefaceCustomized = true;
            mTypeface = typeface;
            return this;
        }

        /**
         * Sets the Undo message
         */
        public Builder setButtonLabel(CharSequence buttonLabel){
            isButtonLabelCustomized = true;
            mButtonLabel = buttonLabel;
            return this;
        }

        /**
         * Sets the Undo drawable
         * can be ignored due to style...
         */
        public Builder setButtonDrawable(Drawable buttonDrawable){
            isButtonDrawableCustomized = true;
            mButtonDrawable = buttonDrawable;
            return this;
        }
        /**
         * Sets the Undo drawable
         * can be ignored due to style...
         */
        public Builder setButtonDrawable(int buttonDrawableRes){
            isButtonDrawableCustomized = true;
            mButtonDrawable = mCtx.getResources().getDrawable(buttonDrawableRes);
            return this;
        }

        /**
         * Sets the {@link Listener UndoBar.Listener}.
         */
        public Builder setListener(Listener undoListener) {
            mUndoListener = undoListener;
            return this;
        }

        /**
         * Sets a {@link Parcelable} token to the undo bar which will be
         * returned in the {@link Listener UndoBar.Listener}.
         */
        public Builder setUndoToken(Parcelable undoToken) {
            mUndoToken = undoToken;
            return this;
        }

        /**
         * Sets the duration the undo bar will be shown.<br>
         * Default is {@link #DEFAULT_DURATION}.
         *
         * @param duration in milliseconds
         */
        public Builder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        /**
         * Sets the duration of the animation for showing and hiding the undo
         * bar.<br>
         * Default is {@link #DEFAULT_ANIMATION_DURATION}.
         *
         * @param animationDuration in milliseconds
         */
        public Builder setAnimationDuration(int animationDuration) {
            mAnimationDuration = animationDuration;
            return this;
        }

        /**
         * Forces the English {@link java.util.Locale Locale} to be used explicitly.<br>
         * This means that the undo bar label will always show <b>UNDO</b>
         * regardless of the device's current {@link java.util.Locale Locale}.
         */
        public Builder setUseEnglishLocale(boolean useEnglishLocale) {
            mUseEnglishLocale = useEnglishLocale;
            return this;
        }

        /**
         * Forces the style of the undo bar to match a certain API level.<br>
         * By default, it uses the style of the device's current API level.
         * <p/>
         * This is useful, for example, if you want to show a consistent
         * Lollipop style across all API levels.
         */
        public Builder setStyle(Style style) {
            mStyle = style;
            return this;
        }

        /**
         * Sets the background color of the Bar
         * The default color will vary by Style;
         * and
         */
        public Builder setBackgroundColor(int backgroundColor){
            isBackgroundColorCustomized = true;
            mBkgColor = backgroundColor;
            return this;
        }

        /**
         * Sets the text color of the undo button.<br>
         * The default color is white.<br>
         * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
         * style and ignored otherwise.
         */
        public Builder setUndoColor(int undoColor) {
            mUndoColor = undoColor;
            return this;
        }

        /**
         * Sets the text color resource id of the undo button.<br>
         * The default color is white.<br>
         * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
         * style and ignored otherwise.
         */
        public Builder setUndoColorResId(int undoColorResId) {
            mUndoColor = mCtx.getResources().getColor(undoColorResId);
            return this;
        }

        /**
         * If set to {@code true}, the undo bar will appear stuck at the bottom without any margins.<br>
         * The default is {@code false}.<br>
         * <b>Note:</b> This is only applied to the {@link UndoBar.Style#LOLLIPOP}
         * style on devices with a smallest width of less than 600dp and ignored otherwise.
         */
        public Builder setAlignParentBottom(boolean alignParentBottom) {
            mAlignParentBottom = alignParentBottom;
            return this;
        }

        /**
         * Creates an {@link UndoBar} instance with this Builder's
         * configuration.
         */
        public UndoBar create() {
            UndoBar undoBarController = null;
            if(mWindow != null){
                undoBarController = new UndoBar(mWindow, mStyle);
                
                undoBarController.setListener(mUndoListener);
                undoBarController.setUndoToken(mUndoToken);
                undoBarController.setMessage(mUndoMessage);
                undoBarController.setButtonVisible(mButtonVisible);
                undoBarController.setTypeface(mTypeface);
                undoBarController.setButtonLabel(mButtonLabel);
                undoBarController.setButtonDrawable(mButtonDrawable);
                undoBarController.setDuration(mDuration);
                undoBarController.setAnimationDuration(mAnimationDuration);
                undoBarController.setUseEnglishLocale(mUseEnglishLocale);
                undoBarController.setBackgroundColor(mBkgColor);
                undoBarController.setUndoColor(mUndoColor);
                undoBarController.setAlignParentBottom(mAlignParentBottom);

                undoBarController.setBackgroundColorCustomized(isBackgroundColorCustomized);
                undoBarController.setButtonDrawableCustomized(isButtonDrawableCustomized);
                undoBarController.setButtonLabelCustomized(isButtonLabelCustomized);
                undoBarController.setTypefaceCustomized(isTypefaceCustomized);
            }else{
                undoBarController = new UndoBar(mCtx, mStyle);

                undoBarController.setMessage(mUndoMessage);
                undoBarController.setTypeface(mTypeface);
                undoBarController.setDuration(mDuration);
                undoBarController.setBackgroundColor(mBkgColor);
                undoBarController.setAlignParentBottom(mAlignParentBottom);

                undoBarController.setBackgroundColorCustomized(isBackgroundColorCustomized);
                undoBarController.setTypefaceCustomized(isTypefaceCustomized);
            }

            return undoBarController;
        }

        /**
         * Calls {@link #show(boolean)} with {@code shouldAnimate = true}.
         */
        public void show() {
            show(true);
        }

        /**
         * Shows the {@link UndoBar} with this Builder's configuration.
         *
         * @param shouldAnimate whether the {@link UndoBar} should animate in and out.
         */
        @SuppressWarnings("SameParameterValue")
        public void show(boolean shouldAnimate) {
            create().show(shouldAnimate);
        }
    }

}
