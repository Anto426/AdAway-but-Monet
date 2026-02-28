/*
 * Copyright (C) 2011-2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This file is part of AdAway.
 *
 * AdAway is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AdAway is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AdAway.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.adaway.ui.lists.type;


import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.adaway.R;
import org.adaway.db.entity.HostListItem;
import org.adaway.db.entity.ListType;
import org.adaway.ui.dialog.AlertDialogValidator;
import org.adaway.util.RegexUtils;

/**
 * This class is a {@link AbstractListFragment} to display and manage allowed hosts.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class AllowedHostsFragment extends AbstractListFragment {
    @Override
    protected LiveData<PagingData<HostListItem>> getData() {
        return this.mViewModel.getAllowedListItems();
    }

    @Override
    public void addItem() {
        HostnameDialogView dialogView = createDialogView();
        EditText inputEditText = dialogView.inputEditText;
        // Create dialog
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this.mActivity)
                .setCancelable(true)
                .setTitle(R.string.list_add_dialog_white)
                .setView(dialogView.root)
                // Setup buttons
                .setPositiveButton(
                        R.string.button_add,
                        (dialog, which) -> {
                            // Close dialog
                            dialog.dismiss();
                            // Check hostname validity
                            String hostname = inputEditText.getText().toString();
                            if (RegexUtils.isValidWildcardHostname(hostname)) {
                                // Insert host to whitelist
                                this.mViewModel.addListItem(ListType.ALLOWED, hostname, null);
                            }
                        }
                )
                .setNegativeButton(
                        R.string.button_cancel,
                        (dialog, which) -> dialog.dismiss()
                )
                .create();
        // Show dialog
        alertDialog.show();
        // Set button validation behavior
        inputEditText.addTextChangedListener(
                new AlertDialogValidator(alertDialog, RegexUtils::isValidWildcardHostname, false)
        );
    }

    @Override
    protected void editItem(HostListItem item) {
        HostnameDialogView dialogView = createDialogView();
        EditText inputEditText = dialogView.inputEditText;
        // Set hostname
        inputEditText.setText(item.getHost());
        // Move cursor to end of EditText
        Editable inputEditContent = inputEditText.getText();
        inputEditText.setSelection(inputEditContent.length());
        // Create dialog builder
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this.mActivity)
                .setCancelable(true)
                .setTitle(R.string.list_edit_dialog_white)
                .setView(dialogView.root)
                // Setup buttons
                .setPositiveButton(
                        R.string.button_save,
                        (dialog, which) -> {
                            // Close dialog
                            dialog.dismiss();
                            // Check hostname validity
                            String hostname = inputEditText.getText().toString();
                            if (RegexUtils.isValidWildcardHostname(hostname)) {
                                // Update list item
                                this.mViewModel.updateListItem(item, hostname, null);
                            }
                        }
                )
                .setNegativeButton(
                        R.string.button_cancel,
                        (dialog, which) -> dialog.dismiss()
                )
                .create();
        // Show dialog
        alertDialog.show();
        // Set button validation behavior
        inputEditText.addTextChangedListener(
                new AlertDialogValidator(alertDialog, RegexUtils::isValidWildcardHostname, true)
        );
    }

    private HostnameDialogView createDialogView() {
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_inner_padding);
        LinearLayout layout = new LinearLayout(this.mActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this.mActivity);
        title.setText(R.string.list_dialog_hostname);
        title.setTextAppearance(android.R.style.TextAppearance_Medium);
        layout.addView(title);

        EditText inputEditText = new EditText(this.mActivity);
        inputEditText.setHint(R.string.list_dialog_hostname_hint);
        inputEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        inputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        inputEditText.setHorizontallyScrolling(true);
        inputEditText.setSingleLine(true);
        layout.addView(inputEditText);

        TextView wildcardHint = new TextView(this.mActivity);
        wildcardHint.setText(R.string.list_dialog_wildcard);
        layout.addView(wildcardHint);

        return new HostnameDialogView(layout, inputEditText);
    }

    private static class HostnameDialogView {
        final View root;
        final EditText inputEditText;

        HostnameDialogView(View root, EditText inputEditText) {
            this.root = root;
            this.inputEditText = inputEditText;
        }
    }
}
