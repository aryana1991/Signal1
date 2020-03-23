package org.thoughtcrime.securesms.util.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ViewUtil;

/**
 * AdaptiveActionsToolbar behaves like a normal {@link Toolbar} except in that it ignores the
 * showAsAlways attributes of menu items added via menu inflation, opting for an adaptive algorithm
 * instead. This algorithm will display as many icons as it can up to a specific percentage of the
 * screen.
 *
 * Each ActionView icon is expected to occupy 48dp of space, including padding. Items are stacked one
 * after the next with no margins.
 *
 * This view can be customized via attributes:
 *
 * aat_max_shown           -- controls the max number of items to display.
 * aat_percent_for_actions -- controls the max percent of screen width the buttons can occupy.
 */
public class AdaptiveActionsToolbar extends Toolbar {

  private static final int   NAVIGATION_SP          = 48;
  private static final int   ACTION_VIEW_WIDTH_SP   = 42;
  private static final int   OVERFLOW_VIEW_WIDTH_SP = 35;

  private int   maxShown;

  public AdaptiveActionsToolbar(@NonNull Context context) {
    this(context, null);
  }

  public AdaptiveActionsToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.toolbarStyle);
  }

  public AdaptiveActionsToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AdaptiveActionsToolbar);

    maxShown = array.getInteger(R.styleable.AdaptiveActionsToolbar_aat_max_shown, 100);

    array.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    adjustMenuActions(getMenu(), maxShown, getMeasuredWidth());
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  public static void adjustMenuActions(@NonNull Menu menu, int maxToShow, int toolbarWidthPx) {
    int menuSize = menu.size();

    int widthAllowed = toolbarWidthPx - ViewUtil.spToPx(NAVIGATION_SP);
    int nItemsToShow = Math.min(maxToShow, Math.round(widthAllowed / ViewUtil.spToPx(ACTION_VIEW_WIDTH_SP)));

    if (nItemsToShow < menuSize) {
      widthAllowed -= ViewUtil.spToPx(OVERFLOW_VIEW_WIDTH_SP);
    }

    nItemsToShow = Math.min(maxToShow, Math.round(widthAllowed / ViewUtil.spToPx(ACTION_VIEW_WIDTH_SP)));

    for (int i = 0; i < menu.size(); i++) {
      MenuItem item = menu.getItem(i);
      if (nItemsToShow > 0) {
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      } else {
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      }
      nItemsToShow--;
    }
  }

}
