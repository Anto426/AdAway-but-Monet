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
 * This class is a {@link AbstractListFragment} to display and manage redirected hosts.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class RedirectedHostsFragment extends AbstractListFragment {
    @Override
    protected boolean isTwoRowsItem() {
        return true;
    }

    @Override
    protected LiveData<PagingData<HostListItem>> getData() {
        return this.mViewModel.getRedirectedListItems();
    }

    @Override
    public void addItem() {
        RedirectDialogView dialogView = createDialogView();
        EditText hostnameEditText = dialogView.hostnameEditText;
        EditText ipEditText = dialogView.ipEditText;
        // Create dialog
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this.mActivity)
                .setCancelable(true)
                .setTitle(R.string.list_add_dialog_redirect)
                .setView(dialogView.root)
                // Setup buttons
                .setPositiveButton(
                        R.string.button_add,
                        (dialog, which) -> {
                            // Close dialog
                            dialog.dismiss();
                            // Check if hostname and IP are valid
                            String hostname = hostnameEditText.getText().toString();
                            String ip = ipEditText.getText().toString();
                            if (RegexUtils.isValidHostname(hostname) && RegexUtils.isValidIP(ip)) {
                                // Insert host to redirection list
                                this.mViewModel.addListItem(ListType.REDIRECTED, hostname, ip);
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
        AlertDialogValidator validator = new AlertDialogValidator(
                alertDialog,
                input -> {
                    String hostname = hostnameEditText.getText().toString();
                    String ip = ipEditText.getText().toString();
                    return RegexUtils.isValidHostname(hostname) && RegexUtils.isValidIP(ip);
                },
                false
        );
        hostnameEditText.addTextChangedListener(validator);
        ipEditText.addTextChangedListener(validator);
    }

    @Override
    protected void editItem(HostListItem item) {
        RedirectDialogView dialogView = createDialogView();
        EditText hostnameEditText = dialogView.hostnameEditText;
        EditText ipEditText = dialogView.ipEditText;
        // Set hostname and IP
        hostnameEditText.setText(item.getHost());
        ipEditText.setText(item.getRedirection());
        // Move cursor to end of EditText
        Editable hostnameEditContent = hostnameEditText.getText();
        hostnameEditText.setSelection(hostnameEditContent.length());
        // Create dialog
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this.mActivity)
                .setCancelable(true)
                .setTitle(getString(R.string.list_edit_dialog_redirect))
                .setView(dialogView.root)
                // Set buttons
                .setPositiveButton(R.string.button_save,
                        (dialog, which) -> {
                            // Close dialog
                            dialog.dismiss();
                            // Check hostname and IP validity
                            String hostname = hostnameEditText.getText().toString();
                            String ip = ipEditText.getText().toString();
                            if (RegexUtils.isValidHostname(hostname) && RegexUtils.isValidIP(ip)) {
                                // Update list item
                                this.mViewModel.updateListItem(item, hostname, ip);
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
        AlertDialogValidator validator = new AlertDialogValidator(
                alertDialog,
                input -> {
                    String hostname = hostnameEditText.getText().toString();
                    String ip = ipEditText.getText().toString();
                    return RegexUtils.isValidHostname(hostname) && RegexUtils.isValidIP(ip);
                },
                true
        );
        hostnameEditText.addTextChangedListener(validator);
        ipEditText.addTextChangedListener(validator);
    }

    private RedirectDialogView createDialogView() {
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_inner_padding);
        LinearLayout layout = new LinearLayout(this.mActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);

        TextView hostnameTitle = new TextView(this.mActivity);
        hostnameTitle.setText(R.string.list_dialog_hostname);
        hostnameTitle.setTextAppearance(android.R.style.TextAppearance_Medium);
        layout.addView(hostnameTitle);

        EditText hostnameEditText = new EditText(this.mActivity);
        hostnameEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        hostnameEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        hostnameEditText.setHorizontallyScrolling(true);
        hostnameEditText.setSingleLine(true);
        layout.addView(hostnameEditText);

        TextView ipTitle = new TextView(this.mActivity);
        ipTitle.setText(R.string.list_dialog_ip);
        ipTitle.setTextAppearance(android.R.style.TextAppearance_Medium);
        layout.addView(ipTitle);

        EditText ipEditText = new EditText(this.mActivity);
        ipEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        ipEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        ipEditText.setHorizontallyScrolling(true);
        ipEditText.setSingleLine(true);
        layout.addView(ipEditText);

        return new RedirectDialogView(layout, hostnameEditText, ipEditText);
    }

    private static class RedirectDialogView {
        final View root;
        final EditText hostnameEditText;
        final EditText ipEditText;

        RedirectDialogView(View root, EditText hostnameEditText, EditText ipEditText) {
            this.root = root;
            this.hostnameEditText = hostnameEditText;
            this.ipEditText = ipEditText;
        }
    }
}
