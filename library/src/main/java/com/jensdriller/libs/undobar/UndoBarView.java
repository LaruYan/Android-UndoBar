package com.jensdriller.libs.undobar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

class UndoBarView extends MaxWidthRelativeLayout {

	public UndoBarView(Context context) {
		super(context);
	}

	public UndoBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UndoBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UndoBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private TextView mMessage;
	private TextView mButton;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mMessage = (TextView) findViewById(R.id.message);
		mButton = (TextView) findViewById(R.id.button);
	}

	@Deprecated
	void setButtonVisible(boolean isVisible){
		View divider = findViewById(R.id.divider);
		if(divider != null){
			divider.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		}
		mButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	void setTypeface(Typeface typeface){
		mMessage.setTypeface(typeface);
		mButton.setTypeface(typeface);
	}

	void setMessage(CharSequence message) {
		mMessage.setText(message);
	}

	void setButtonLabel(CharSequence buttonLabel) {
		mButton.setText(buttonLabel);
	}

    void setButtonLabel(int buttonLabelResId) {
        mButton.setText(buttonLabelResId);
    }

	void setButtonDrawable(Drawable buttonDrawable){
		mButton.setCompoundDrawables(buttonDrawable,null,null,null);
	}

    void setUndoColor(int color) {
        mButton.setTextColor(color);
    }

	void setOnUndoClickListener(OnClickListener onClickListener) {
		mButton.setOnClickListener(onClickListener);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.message = mMessage.getText().toString();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setMessage(ss.message);
	}

	@SuppressWarnings("NullableProblems")
    private static class SavedState extends BaseSavedState {

		String message;

		SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(message);
		}

		private SavedState(Parcel in) {
			super(in);
			message = in.readString();
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
