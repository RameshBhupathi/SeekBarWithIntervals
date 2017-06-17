package uk.co.informaticscentre.utils.controls;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.informaticscentre.utils.R;

public class SeekbarWithIntervals extends LinearLayout {
    private RelativeLayout RelativeLayout = null;
    private SeekBar Seekbar = null;

    private int WidthMeasureSpec = 0;
    private int HeightMeasureSpec = 0;
    private boolean isAlignmentResetOnLayoutChange;


    public SeekbarWithIntervals(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.seekbar_with_intervals, this);
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

       /* getActivity().getLayoutInflater()
                .inflate(R.layout.seekbar_with_intervals, this);*/
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed) {
            if (!isAlignmentResetOnLayoutChange) {
                alignIntervals();

                // We've changed the intervals layout, we need to refresh.
                RelativeLayout.measure(WidthMeasureSpec, HeightMeasureSpec);
                RelativeLayout.layout(RelativeLayout.getLeft(), RelativeLayout.getTop(), RelativeLayout.getRight(), RelativeLayout.getBottom());
            }
        }
    }


    private void alignIntervals() {

        if (getSeekbar() != null) {
            int widthOfSeekbarThumb = getSeekbarThumbWidth();
            int thumbOffset = widthOfSeekbarThumb / 2;

            int widthOfSeekbar = getSeekbar().getWidth();
            int firstIntervalWidth = getRelativeLayout().getChildAt(0).getWidth();
            int remainingPaddableWidth = widthOfSeekbar - firstIntervalWidth - widthOfSeekbarThumb;

            int numberOfIntervals = getSeekbar().getMax();
            int maximumWidthOfEachInterval = remainingPaddableWidth / numberOfIntervals;

            alignFirstInterval(thumbOffset);
            alignIntervalsInBetween(maximumWidthOfEachInterval);
            alignLastInterval(thumbOffset, maximumWidthOfEachInterval);
            isAlignmentResetOnLayoutChange = true;
        }
    }

    private int getSeekbarThumbWidth() {
        return getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_width);
    }

    private void alignFirstInterval(int offset) {
        TextView firstInterval = (TextView) getRelativeLayout().getChildAt(0);
        firstInterval.setPadding(offset, 0, 0, 0);
    }

    private void alignIntervalsInBetween(int maximumWidthOfEachInterval) {
        int widthOfPreviousIntervalsText = 0;

        // Don't align the first or last interval.
        for (int index = 1; index < (getRelativeLayout().getChildCount() - 1); index++) {
            TextView textViewInterval = (TextView) getRelativeLayout().getChildAt(index);
            int widthOfText = textViewInterval.getWidth();

            // This works out how much left padding is needed to center the current interval.
            int leftPadding = Math.round(maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2));
            textViewInterval.setPadding(leftPadding, 0, 0, 0);

            widthOfPreviousIntervalsText = widthOfText;
        }
    }

    private void alignLastInterval(int offset, int maximumWidthOfEachInterval) {
        int lastIndex = getRelativeLayout().getChildCount() - 1;

        TextView lastInterval = (TextView) getRelativeLayout().getChildAt(lastIndex);
        int widthOfText = lastInterval.getWidth();

        int leftPadding = Math.round(maximumWidthOfEachInterval - widthOfText - offset);
        lastInterval.setPadding(leftPadding, 0, 0, 0);
    }

    protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        WidthMeasureSpec = widthMeasureSpec;
        HeightMeasureSpec = heightMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getProgress() {
        return getSeekbar().getProgress();
    }

    public void setIntervals(List<String> intervals) {
        displayIntervals(intervals);
        getSeekbar().setMax(intervals.size() - 1);
    }

    public void setProgress(int progress) {
        getSeekbar().setProgress(progress);
    }

    private void displayIntervals(List<String> intervals) {
        int idOfPreviousInterval = 0;

        if (getRelativeLayout().getChildCount() == 0) {
            for (String interval : intervals) {
                TextView textViewInterval = createInterval(interval);
                alignTextViewToRightOfPreviousInterval(textViewInterval, idOfPreviousInterval);

                idOfPreviousInterval = textViewInterval.getId();

                getRelativeLayout().addView(textViewInterval);
            }
        }
    }

    private TextView createInterval(String interval) {
        View textBoxView = (View) LayoutInflater.from(getContext())
                .inflate(R.layout.seekbar_with_intervals_labels, null);

        TextView textView = (TextView) textBoxView
                .findViewById(R.id.textViewInterval);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            textView.setId(View.generateViewId());
        else
            textBoxView.setId(generateViewId());

        textView.setText(interval);

        return textView;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public void setAlignmentResetOnLayoutChange(){
        alignIntervals();

        // We've changed the intervals layout, we need to refresh.
        RelativeLayout.measure(WidthMeasureSpec, HeightMeasureSpec);
        RelativeLayout.layout(RelativeLayout.getLeft(), RelativeLayout.getTop(), RelativeLayout.getRight(), RelativeLayout.getBottom());

    }

    private void alignTextViewToRightOfPreviousInterval(TextView textView, int idOfPreviousInterval) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (idOfPreviousInterval > 0) {
            params.addRule(RelativeLayout.RIGHT_OF, idOfPreviousInterval);
        }

        textView.setLayoutParams(params);
    }

    public void setOnSeekBarChangeListener(final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {

        getSeekbar().setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                for (int i = 0; i < getRelativeLayout().getChildCount(); i++) {
                    TextView tv = (TextView) getRelativeLayout().getChildAt(i);
                    if (i == seekBar.getProgress())
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    else
                        tv.setTextColor(getResources().getColor(R.color.black));
                }
                onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });


    }

    private RelativeLayout getRelativeLayout() {
        if (RelativeLayout == null) {
            RelativeLayout = (RelativeLayout) findViewById(R.id.intervals);
        }

        return RelativeLayout;
    }

    private SeekBar getSeekbar() {
        if (Seekbar == null) {
            Seekbar = (SeekBar) findViewById(R.id.seekbar);
        }

        return Seekbar;
    }
}