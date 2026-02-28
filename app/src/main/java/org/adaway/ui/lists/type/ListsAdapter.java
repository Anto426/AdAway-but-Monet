package org.adaway.ui.lists.type;

import android.content.Context;
import android.text.TextUtils;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.adaway.db.entity.HostListItem;
import org.adaway.ui.lists.ListsViewCallback;

import static org.adaway.db.entity.HostsSource.USER_SOURCE_ID;

/**
 * This class is a the {@link RecyclerView.Adapter} for the hosts list view.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
class ListsAdapter extends PagingDataAdapter<HostListItem, ListsAdapter.ViewHolder> {
    /**
     * This callback is use to compare hosts sources.
     */
    private static final DiffUtil.ItemCallback<HostListItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HostListItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull HostListItem oldItem, @NonNull HostListItem newItem) {
                    return (oldItem.getHost().equals(newItem.getHost()));
                }

                @Override
                public boolean areContentsTheSame(@NonNull HostListItem oldItem, @NonNull HostListItem newItem) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldItem.equals(newItem);
                }
            };

    /**
     * This callback is use to call view actions.
     */
    @NonNull
    private final ListsViewCallback viewCallback;
    /**
     * Whether the list item needs two rows or not.
     */
    private final boolean twoRows;

    /**
     * Constructor.
     *
     * @param viewCallback The view callback.
     * @param twoRows      Whether the list items need two rows or not.
     */
    ListsAdapter(@NonNull ListsViewCallback viewCallback, boolean twoRows) {
        super(DIFF_CALLBACK);
        this.viewCallback = viewCallback;
        this.twoRows = twoRows;
    }

    @NonNull
    @Override
    public ListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        rowLayout.setLayoutParams(
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        rowLayout.setMinimumHeight(dp(context, this.twoRows ? 72 : 48));

        CheckBox enabledCheckBox = new CheckBox(context);
        enabledCheckBox.setFocusable(false);
        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        checkBoxParams.leftMargin = dp(context, 12);
        checkBoxParams.rightMargin = dp(context, 8);
        rowLayout.addView(enabledCheckBox, checkBoxParams);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColumnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textColumnParams.rightMargin = dp(context, 16);
        rowLayout.addView(textColumn, textColumnParams);

        TextView hostTextView = new TextView(context);
        hostTextView.setSingleLine(true);
        hostTextView.setEllipsize(TextUtils.TruncateAt.END);
        hostTextView.setTypeface(hostTextView.getTypeface(), Typeface.BOLD);
        hostTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        LinearLayout.LayoutParams hostParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hostParams.topMargin = dp(context, this.twoRows ? 12 : 0);
        hostParams.bottomMargin = dp(context, this.twoRows ? 0 : 12);
        textColumn.addView(hostTextView, hostParams);

        TextView redirectionTextView = null;
        if (this.twoRows) {
            redirectionTextView = new TextView(context);
            redirectionTextView.setSingleLine(true);
            redirectionTextView.setEllipsize(TextUtils.TruncateAt.END);
            redirectionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            LinearLayout.LayoutParams subTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subTextParams.bottomMargin = dp(context, 12);
            textColumn.addView(redirectionTextView, subTextParams);
        }

        return new ViewHolder(rowLayout, enabledCheckBox, hostTextView, redirectionTextView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HostListItem item = getItem(position);
        if (item == null) { // Data might be null if not loaded yet
            holder.clear();
            return;
        }
        boolean editable = item.getSourceId() == USER_SOURCE_ID;
        holder.enabledCheckBox.setEnabled(editable);
        holder.enabledCheckBox.setChecked(item.isEnabled());
        holder.enabledCheckBox.setOnClickListener(editable ? view -> this.viewCallback.toggleItemEnabled(item) : null);
        holder.hostTextView.setText(item.getHost());
        if (this.twoRows) {
            holder.redirectionTextView.setText(item.getRedirection());
        }
        holder.itemView.setOnLongClickListener(editable ?
                view -> this.viewCallback.startAction(item, holder.itemView) :
                view -> this.viewCallback.copyHostToClipboard(item));
    }

    /**
     * This class is a the {@link RecyclerView.ViewHolder} for the hosts list view.
     *
     * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox enabledCheckBox;
        final TextView hostTextView;
        final TextView redirectionTextView;

        /**
         * Constructor.
         *
         * @param itemView The hosts sources view.
         */
        ViewHolder(
                View itemView,
                CheckBox enabledCheckBox,
                TextView hostTextView,
                TextView redirectionTextView
        ) {
            super(itemView);
            this.enabledCheckBox = enabledCheckBox;
            this.hostTextView = hostTextView;
            this.redirectionTextView = redirectionTextView;
        }

        void clear() {
            this.enabledCheckBox.setChecked(true);
            this.enabledCheckBox.setEnabled(false);
            this.enabledCheckBox.setOnClickListener(null);
            this.hostTextView.setText("");
            if (this.redirectionTextView != null) {
                this.redirectionTextView.setText("");
            }
            this.itemView.setOnLongClickListener(null);
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        context.getResources().getDisplayMetrics()
                )
        );
    }
}
