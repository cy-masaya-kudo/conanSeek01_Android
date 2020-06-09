/**
 * http://qiita.com/GeneralD/items/89278fc0f5513abe0bb3
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.cybird.android.utils.CustomRelativeLayout;

/**
 * DialogFragmentをカスタマイズ.
 * <p>Before API level 11 (Android 3.0/Honeycomb)
 * Dialogに設定するViewは以下を参照</p>
 * <p/>
 * <p>layout_dialog.xml</p>
 * <p>
 * Title TextView resource id:<br>
 * dialog_title_textview<br>
 * <br>
 * Main Content LinearLayout resource id:
 * dialog_main_content_linearlayout<br>
 * <br>
 * Action(Button) Area LinearLayout resource id:<br>
 * daialog_action_area<br>
 * <br>
 * Positive Button resource id:<br>
 * dialog_positive_button<br>
 * <br>
 * Negative Button resource id:<br>
 * dialog_negative_button<br>
 * <br>
 * Neutral Button resource id:<br>
 * dialog_neutral_button<br>
 * <br></p>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CustomDialogFragment extends DialogFragment implements View.OnClickListener {
    // flags
    public static final int NO_POSITIVE_BUTTON = 1 << 0;
    public static final int NO_NEGATIVE_BUTTON = 1 << 1;
    public static final int ADD_NEUTRAL_BUTTON = 1 << 2;
    public static final int DISABLE_POSITIVE_BUTTON = 1 << 3;
    public static final int DISABLE_NEGATIVE_BUTTON = 1 << 4;
    public static final int DISABLE_NEUTRAL_BUTTON = 1 << 5;

    public static final int BUTTON_POSITIVE = -1;
    public static final int BUTTON_NEGATIVE = -2;
    public static final int BUTTON_NEUTRAL = -3;

    private static final String TAG = "cdf";

    private DialogFragmentListener listener;
    private int listenerId;

    private boolean clickGuard = false;
    private boolean dismissFlag = false;


    /**
     * Use this method as A constructor!!
     *
     * @return new instance
     */
    public static final CustomDialogFragment newInstance() {

        return newInstance(true);
    }

    /**
     * Use this method as A constructor!!
     *
     * @param cancelable whether the shown Dialog is cancelable.
     * @return new instance
     */
    public static final CustomDialogFragment newInstance(boolean cancelable) {

        return newInstance(0, cancelable);
    }

    /**
     * Use this method as A constructor!!
     *
     * @param flags 0 ok, unless especially
     * @return new instance
     * @see #NO_POSITIVE_BUTTON
     * @see #NO_NEGATIVE_BUTTON
     */
    public static final CustomDialogFragment newInstance(int flags) {

        return newInstance(flags, true);
    }

    /**
     * Use this method as A constructor!!
     *
     * @param flags      0 ok, unless especially
     * @param cancelable whether the shown Dialog is cancelable.
     * @return new instance
     * @see #NO_POSITIVE_BUTTON
     * @see #NO_NEGATIVE_BUTTON
     */
    public static final CustomDialogFragment newInstance(int flags, boolean cancelable) {

        Bundle args = new Bundle();
        args.putInt("flags", flags);

        CustomDialogFragment fragment = new CustomDialogFragment();
        fragment.setArguments(args);
        fragment.setCancelable(cancelable);
        return fragment;
    }

    /**
     * New instance with force ok button. (cancelable = false)
     *
     * @param flags the flags (0 ok)
     * @return new instance
     * @see #NO_POSITIVE_BUTTON
     * @see #NO_NEGATIVE_BUTTON
     */
    public static final CustomDialogFragment newInstanceForceOkDialog(int flags) {

        return newInstance(NO_NEGATIVE_BUTTON | flags, false);
    }

    /**
     * New instance with no button. (cancelable = false)
     *
     * @param flags the flags (0 ok)
     * @return new instance
     * @see #NO_POSITIVE_BUTTON
     * @see #NO_NEGATIVE_BUTTON
     */
    public static final CustomDialogFragment newInstanceNoButton(int flags) {

        return newInstance(NO_POSITIVE_BUTTON | NO_NEGATIVE_BUTTON
                | flags, false);
    }

    public static final CustomDialogFragment newInstanceForceCancelDialog(int flags) {

        return newInstance(NO_POSITIVE_BUTTON | flags, false);
    }

    /**
     * Keep this constructor!!
     */
    public CustomDialogFragment() {

        // Don't do anything here!!
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // ダイアログの大きさを決める
        Dialog dialog = getDialog();

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int dialogWidth = (int) (metrics.widthPixels * 0.9);
        int dialogHeight = (int) (metrics.heightPixels * 0.9);

        lp.width = dialogWidth;
        lp.height = dialogHeight;
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int flags = args.getInt("flags");
        boolean noPosBtn = (flags & NO_POSITIVE_BUTTON) != 0;
        boolean noNegBtn = (flags & NO_NEGATIVE_BUTTON) != 0;
        boolean addNtrlBtn = (flags & ADD_NEUTRAL_BUTTON) != 0;
        boolean disablePosBtn = false;
        boolean disableNegBtn = false;
        boolean disableNtrlBtn = false;
        if (!noPosBtn && (flags & DISABLE_POSITIVE_BUTTON) != 0) {
            disablePosBtn = true;
        }
        if (!noNegBtn && (flags & DISABLE_NEGATIVE_BUTTON) != 0) {
            disableNegBtn = true;
        }
        if (addNtrlBtn && (flags & DISABLE_NEUTRAL_BUTTON) != 0) {
            disableNtrlBtn = true;
        }

        convertResIdToStringInArgs("titleResId", "title");
        convertResIdToStringInArgs("posbtnTextResId", "posbtnText");
        convertResIdToStringInArgs("negbtnTextResId", "negbtnText");
        convertResIdToStringInArgs("ntrlbtnTextResId", "ntrlbtnText");

        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(getResourceId("layout_dialog", "layout"));
        dialog.setCanceledOnTouchOutside(isCancelable());

        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view;

        // background
        view = dialog.findViewById(getResourceId("dialog_layout_base", "id"));
        if (view != null) {
            if (args.containsKey("backgroundColor")) {
                view.setBackgroundColor(args.getInt("backgroundColor"));
            } else if (args.containsKey("backgroundResId")) {
                view.setBackgroundResource(args.getInt("backgroundResId"));
            }
        }

        // title
        view = dialog.findViewById(getResourceId("dialog_title_textview", "id"));
        if (view instanceof TextView) {
            if (!args.containsKey("title")) view.setVisibility(View.GONE);
            else ((TextView) view).setText(args.getString("title"));
        }

        // view部分
        view = dialog.findViewById(getResourceId("dialog_main_content_linearlayout", "id"));
        if (view instanceof LinearLayout) {
            if (!args.containsKey("view")) {
                view.setVisibility(View.GONE);
            } else {
                if (args.getSerializable("view") instanceof CustomRelativeLayout) {
                    CustomRelativeLayout crl = (CustomRelativeLayout) args.getSerializable("view");
                    ViewGroup parent = (ViewGroup) crl.getParent();
                    if (parent != null) {
                        parent.removeView(crl);
                    }
                    LinearLayout llView = (LinearLayout) view;
                    llView.addView(crl);
                }
            }
        }

        // layout_dialog.xmlのaction_areaを使用
        View action_area_view = dialog.findViewById(getResourceId("daialog_action_area", "id"));
        if (action_area_view != null) {

            // positive button
            view = action_area_view.findViewById(getResourceId("dialog_positive_button", "id"));
            if (view != null) {
                if (noPosBtn) view.setVisibility(View.GONE);
                else {
                    view.setOnClickListener(this);
                    if (view instanceof TextView) {
                        TextView button = (TextView) view;
                        if (args.containsKey("posbtnText"))
                            button.setText(args.getString("posbtnText"));

                        if (args.containsKey("buttonBackgroundColor"))
                            button.setBackgroundColor(args.getInt("buttonBackgroundColor"));
                        else if (args.containsKey("buttonBbackgroundResId"))
                            button.setBackgroundResource(args.getInt("buttonBbackgroundResId"));

                        if (disablePosBtn) button.setEnabled(false);
                    }
                }
            }

            // negative button
            view = action_area_view.findViewById(getResourceId("dialog_negative_button", "id"));
            if (view != null) {
                if (noNegBtn) view.setVisibility(View.GONE);
                else {
                    view.setOnClickListener(this);
                    if (view instanceof TextView) {
                        TextView button = (TextView) view;
                        if (args.containsKey("negbtnText"))
                            button.setText(args.getString("negbtnText"));

                        if (args.containsKey("buttonBackgroundColor"))
                            button.setBackgroundColor(args.getInt("buttonBackgroundColor"));
                        else if (args.containsKey("buttonBbackgroundResId"))
                            button.setBackgroundResource(args.getInt("buttonBbackgroundResId"));

                        if (disableNegBtn) button.setEnabled(false);
                    }
                }
            }

            // neutral button
            view = action_area_view.findViewById(getResourceId("dialog_neutral_button", "id"));
            if (view != null) {
                if (addNtrlBtn) {
                    view.setVisibility(View.VISIBLE);
                    view.setOnClickListener(this);
                    if (view instanceof TextView) {
                        TextView button = (TextView) view;
                        if (args.containsKey("ntrlbtnText"))
                            button.setText(args.getString("ntrlbtnText"));

                        if (args.containsKey("buttonBackgroundColor"))
                            button.setBackgroundColor(args.getInt("buttonBackgroundColor"));
                        else if (args.containsKey("buttonBbackgroundResId"))
                            button.setBackgroundResource(args.getInt("buttonBbackgroundResId"));

                        if (disableNtrlBtn) button.setEnabled(false);
                    }
                }
            }
        }
        return dialog;
    }

    @Override
    public void onResume() {

        if (dismissFlag) { // dismiss A dialog
            Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismiss();
                getFragmentManager().beginTransaction().remove(fragment).commit();
                dismissFlag = false;
            }
        }
        super.onResume();
    }

    /**
     * @deprecated Use {@link #show(FragmentManager)}!!
     */
    @Override
    @Deprecated
    public final void show(FragmentManager manager, String tag) {

        show(manager);
    }

    /**
     * Display the dialog, adding the fragment to the given FragmentManager.
     * This is A convenience for explicitly creating A transaction, adding the fragment to it with the given tag,
     * and committing it. This does <em>not</em> add the transaction to the back stack.
     * When the fragment is dismissed, A new transaction will be executed to remove it from the activity.
     *
     * @param manager The FragmentManager this fragment will be added to.
     */
    public final void show(FragmentManager manager) {

        deleteDialogFragment(manager);
        clickGuard = false;

        // super.show(manager, TAG);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(this, TAG);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void dismiss() {
        if (isResumed()) super.dismiss(); // dismiss now
        else dismissFlag = true; // dismiss on onResume
    }

    public void cancel() {
        if (isResumed() && getDialog() != null) {
            getDialog().cancel();
        }
    }

    /**
     * ダイアログが表示されているかどうか
     *
     * @return
     */
    public boolean isShowing() {
        boolean ret = false;
        if (isResumed() && getDialog() != null) {
            ret = getDialog().isShowing();
        }
        return ret;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
        if (listener != null) listener.onEvent(listenerId, DialogFragmentListener.ON_DISMISS, null);
    }

    @Override
    public void onCancel(DialogInterface dialog) {

        super.onCancel(dialog);
        if (listener != null) listener.onEvent(listenerId, DialogFragmentListener.ON_CANCEL, null);
    }


    /**
     * Sets dialog title.
     *
     * @param title dialog title
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setTitle(String title) {

        getArguments().putString("title", title);
        getArguments().remove("titleResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View view = dialog.findViewById(getResourceId("dialog_title_textview", "id"));
        if (view instanceof TextView) ((TextView) view).setText(title);
        return this;
    }

    /**
     * Sets dialog title.
     *
     * @param resId resource id
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setTitle(int resId) {

        if (isAdded()) setTitle(getString(resId));
        else {
            getArguments().putInt("titleResId", resId);
            getArguments().remove("title");
        }
        return this;
    }

    /**
     * Sets dialog message.
     *
     * @param view
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setView(CustomRelativeLayout view) {

        if (getArguments().containsKey("view")) {
            getArguments().remove("view");
        }
        getArguments().putSerializable("view", view);
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View tmpView = dialog.findViewById(getResourceId("dialog_main_content_linearlayout", "id"));
        if (tmpView instanceof LinearLayout) {
            LinearLayout llView = (LinearLayout) tmpView;
            llView.removeAllViews();
            llView.addView(view);
        }
        return this;
    }

    /**
     * Sets text on positive button.
     *
     * @param text the text
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setPositiveButtonText(String text) {

        getArguments().putString("posbtnText", text);
        getArguments().remove("posbtnTextResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View view = dialog.findViewById(getResourceId("dialog_positive_button", "id"));
        if (view instanceof TextView) ((TextView) view).setText(text);
        return this;
    }

    /**
     * Sets text on positive button.
     *
     * @param resId resource id
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setPositiveButtonText(int resId) {

        if (isAdded()) setPositiveButtonText(getString(resId));
        else {
            getArguments().putInt("posbtnTextResId", resId);
            getArguments().remove("posbtnText");
        }
        return this;
    }

    /**
     * Sets text on negative button.
     *
     * @param text the text
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setNegativeButtonText(String text) {

        getArguments().putString("negbtnText", text);
        getArguments().remove("negbtnTextResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View view = dialog.findViewById(getResourceId("dialog_negative_button", "id"));
        if (view instanceof TextView) ((TextView) view).setText(text);
        return this;
    }

    /**
     * Sets text on negative button.
     *
     * @param resId resource id
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setNegativeButtonText(int resId) {

        if (isAdded()) setNegativeButtonText(getString(resId));
        else {
            getArguments().putInt("negbtnTextResId", resId);
            getArguments().remove("negbtnText");
        }
        return this;
    }

    /**
     * Sets text on neutral button.
     *
     * @param text the text
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setNeutralButtonText(String text) {

        getArguments().putString("ntrlbtnText", text);
        getArguments().remove("ntrlbtnTextResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View view = dialog.findViewById(getResourceId("dialog_neutral_button", "id"));
        if (view instanceof TextView) ((TextView) view).setText(text);
        return this;
    }

    /**
     * Sets text on neutral button.
     *
     * @param resId resource id
     * @return this {@link CustomDialogFragment} instance
     */
    public CustomDialogFragment setNeutralButtonText(int resId) {

        if (isAdded()) setNeutralButtonText(getString(resId));
        else {
            getArguments().putInt("ntrlbtnTextResId", resId);
            getArguments().remove("ntrlbtnText");
        }
        return this;
    }

    private void convertResIdToStringInArgs(String resIdkey, String destKey) {

        Bundle args = getArguments();
        if (isAdded()) {
            if (args.containsKey(resIdkey)) {
                String string = getString(args.getInt(resIdkey));
                args.putString(destKey, string);
                args.remove(resIdkey);
            }
        }
    }

    /**
     * @param manager {@link FragmentManager}
     */
    private void deleteDialogFragment(final FragmentManager manager) {

        CustomDialogFragment previous = (CustomDialogFragment) manager.findFragmentByTag(TAG);
        if (previous == null) return;

        Dialog dialog = previous.getDialog();
        if (dialog == null) return;

        if (!dialog.isShowing()) return;

        previous.onDismissExclusiveDialog();
        previous.dismiss();
    }

    protected void onDismissExclusiveDialog() {

    }

    /**
     * イベントフック用リスナーを設定する
     *
     * @param id
     * @param listener
     * @return CustomDialogFragmentインスタンス
     */
    public CustomDialogFragment setListener(int id, DialogFragmentListener listener) {
        this.listenerId = id;
        this.listener = listener;
        return this;
    }

    /**
     * ダイアログのボタンインスタンスを取得
     *
     * @param type BUTTON_POSITIVE,BUTTON_NEGATIVE,BUTTON_NEUTRAL
     * @return typeで指定されたボタンインスタンス
     */
    public View getButton(int type) {
        View button = null;

        int flags = getArguments().getInt("flags");
        boolean noPosBtn = (flags & NO_POSITIVE_BUTTON) != 0;
        boolean noNegBtn = (flags & NO_NEGATIVE_BUTTON) != 0;
        boolean addNtrlBtn = (flags & ADD_NEUTRAL_BUTTON) != 0;

        final Dialog dialog = getDialog();
        if (dialog != null) {
            View action_area_view = dialog.findViewById(getResourceId("daialog_action_area", "id"));
            if (action_area_view != null) {
                if (type == BUTTON_POSITIVE && !noPosBtn) {
                    // positive button
                    button = action_area_view.findViewById(getResourceId("dialog_positive_button", "id"));
                } else if (type == BUTTON_NEGATIVE && !noNegBtn) {
                    // negative button
                    button = action_area_view.findViewById(getResourceId("dialog_negative_button", "id"));
                } else if (type == BUTTON_NEUTRAL && addNtrlBtn) {
                    // neutral button
                    button = action_area_view.findViewById(getResourceId("dialog_neutral_button", "id"));
                }
            }
        }
        return button;
    }

    @Override
    public void onClick(View view) {
        int resid = view.getId();
        if (resid == getResourceId("dialog_positive_button", "id")) {
            if (listener != null)
                listener.onEvent(listenerId, DialogFragmentListener.ON_POSITIVE_BUTTON_CLICKED, this);
        } else if (resid == getResourceId("dialog_negative_button", "id")) {
            if (listener != null)
                listener.onEvent(listenerId, DialogFragmentListener.ON_NEGATIVE_BUTTON_CLICKED, this);
        } else {
            if (listener != null)
                listener.onEvent(listenerId, DialogFragmentListener.ON_NEUTRAL_BUTTON_CLICKED, this);
        }
    }

    @SuppressWarnings("deprecation")
    public CustomDialogFragment setBackgroundDrawable(Drawable drawable) {
        View layout_base = null;
        final Dialog dialog = getDialog();
        if (dialog != null) {
            layout_base = dialog.findViewById(getResourceId("dialog_layout_base", "id"));
            if (layout_base != null) {
                layout_base.setBackgroundDrawable(drawable);
            }
        }
        return this;
    }

    public CustomDialogFragment setBackgroundColor(int color) {
        getArguments().putInt("backgroundColor", color);
        getArguments().remove("backgroundResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View layout_base = null;
        layout_base = dialog.findViewById(getResourceId("dialog_layout_base", "id"));
        if (layout_base != null) {
            layout_base.setBackgroundColor(color);
        }
        return this;
    }

    public CustomDialogFragment setBackgroundResource(int resId) {
        getArguments().putInt("backgroundResId", resId);
        getArguments().remove("backgroundColor");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View layout_base = null;
        layout_base = dialog.findViewById(getResourceId("dialog_layout_base", "id"));
        if (layout_base != null) {
            layout_base.setBackgroundResource(resId);
        }
        return this;
    }


    /**
     * 一括でボタンの背景を指定
     *
     * @param drawable
     */
    @SuppressWarnings("deprecation")
    public CustomDialogFragment setButtonBackgroundDrawable(Drawable drawable) {
        View button = null;
        int flags = getArguments().getInt("flags");
        boolean noPosBtn = (flags & NO_POSITIVE_BUTTON) != 0;
        boolean noNegBtn = (flags & NO_NEGATIVE_BUTTON) != 0;
        boolean addNtrlBtn = (flags & ADD_NEUTRAL_BUTTON) != 0;

        final Dialog dialog = getDialog();
        if (dialog != null) {
            View action_area_view = dialog.findViewById(getResourceId("daialog_action_area", "id"));
            if (action_area_view != null) {
                if (!noPosBtn) {
                    // positive button
                    button = action_area_view.findViewById(getResourceId("dialog_positive_button", "id"));
                    button.setBackgroundDrawable(drawable);
                }
                if (!noNegBtn) {
                    // negative button
                    button = action_area_view.findViewById(getResourceId("dialog_negative_button", "id"));
                    button.setBackgroundDrawable(drawable);
                }
                if (addNtrlBtn) {
                    // neutral button
                    button = action_area_view.findViewById(getResourceId("dialog_neutral_button", "id"));
                    button.setBackgroundDrawable(drawable);
                }
            }
        }

        return this;
    }

    /**
     * 一括でボタンの背景を指定
     *
     * @param color
     */
    public CustomDialogFragment setButtonBackgroundColor(int color) {
        View button = null;
        int flags = getArguments().getInt("flags");
        boolean noPosBtn = (flags & NO_POSITIVE_BUTTON) != 0;
        boolean noNegBtn = (flags & NO_NEGATIVE_BUTTON) != 0;
        boolean addNtrlBtn = (flags & ADD_NEUTRAL_BUTTON) != 0;

        getArguments().putInt("buttonBackgroundColor", color);
        getArguments().remove("buttonBbackgroundResId");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View action_area_view = dialog.findViewById(getResourceId("daialog_action_area", "id"));
        if (action_area_view != null) {
            if (!noPosBtn) {
                // positive button
                button = action_area_view.findViewById(getResourceId("dialog_positive_button", "id"));
                button.setBackgroundColor(color);
            }
            if (!noNegBtn) {
                // negative button
                button = action_area_view.findViewById(getResourceId("dialog_negative_button", "id"));
                button.setBackgroundColor(color);
            }
            if (addNtrlBtn) {
                // neutral button
                button = action_area_view.findViewById(getResourceId("dialog_neutral_button", "id"));
                button.setBackgroundColor(color);
            }
        }
        return this;
    }

    /**
     * 一括でボタンの背景を指定
     *
     * @param resId
     */
    public CustomDialogFragment setButtonBackgroundResource(int resId) {
        View button = null;
        int flags = getArguments().getInt("flags");
        boolean noPosBtn = (flags & NO_POSITIVE_BUTTON) != 0;
        boolean noNegBtn = (flags & NO_NEGATIVE_BUTTON) != 0;
        boolean addNtrlBtn = (flags & ADD_NEUTRAL_BUTTON) != 0;

        getArguments().putInt("buttonBbackgroundResId", resId);
        getArguments().remove("buttonBackgroundColor");
        Dialog dialog = getDialog();
        if (dialog == null) return this;
        View action_area_view = dialog.findViewById(getResourceId("daialog_action_area", "id"));
        if (action_area_view != null) {
            if (!noPosBtn) {
                // positive button
                button = action_area_view.findViewById(getResourceId("dialog_positive_button", "id"));
                button.setBackgroundResource(resId);
            }
            if (!noNegBtn) {
                // negative button
                button = action_area_view.findViewById(getResourceId("dialog_negative_button", "id"));
                button.setBackgroundResource(resId);
            }
            if (addNtrlBtn) {
                // neutral button
                button = action_area_view.findViewById(getResourceId("dialog_neutral_button", "id"));
                button.setBackgroundResource(resId);
            }
        }
        return this;
    }

    /**
     * DialogFragmentを生成するために必要な値設定に使用するParamクラス
     */
    public static class CustomDialogParams {
        public String mTitle = null;
        public String mPositiveButtonTitle = null;
        public String mNegativeButtonTitle = null;
        public String mNeutralButtonTitle = null;

        public int mBackgroundColor = 0;
        public int mBackgroundResId = 0;

        public int mButtonBackgroundColor = 0;
        public int mButtonBackgroundResId = 0;

        public boolean mCancelable = false;
        public int mFlags = 0;

        public DialogFragmentListener mDialogFragmentListener = null;
        public int mLintenerId = 0;
        public CustomRelativeLayout mView = null;

        public CustomDialogParams() {
        }
    }

    /**
     * DialogFragmentを生成するために値を指定するBuilderクラス
     */
    public static class Builder {

        CustomDialogParams P;

        public Builder() {
            this.P = new CustomDialogParams();
        }

        public Builder setPositiveButtonTitle(String title) {
            this.P.mPositiveButtonTitle = title;
            return this;
        }

        public Builder setNeutralButtonTitle(String title) {
            this.P.mNeutralButtonTitle = title;
            return this;
        }

        public Builder setNegativeButtonTitle(String title) {
            this.P.mNegativeButtonTitle = title;
            return this;
        }

        public Builder setTitle(String title) {
            this.P.mTitle = title;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.P.mCancelable = cancelable;
            return this;
        }

        public Builder setFlags(int flags) {
            this.P.mFlags = flags;
            return this;
        }

        public Builder setDialogFragmentListener(int id, DialogFragmentListener dialogFragmentListener) {
            this.P.mLintenerId = id;
            this.P.mDialogFragmentListener = dialogFragmentListener;
            return this;
        }

        public Builder setView(CustomRelativeLayout view) {
            this.P.mView = view;
            return this;
        }

        public Builder setBackgroundColor(int color) {
            this.P.mBackgroundColor = color;
            return this;
        }

        public Builder setBackgroundResource(int resId) {
            this.P.mBackgroundResId = resId;
            return this;
        }

        public Builder setButtonBackgroundColor(int color) {
            this.P.mButtonBackgroundColor = color;
            return this;
        }

        public Builder setButtonBackgroundResource(int resId) {
            this.P.mButtonBackgroundResId = resId;
            return this;
        }

        public CustomDialogFragment create() {
            return setInstance(CustomDialogFragment.newInstance(P.mFlags, P.mCancelable));
        }

        public CustomDialogFragment createForceOkDialog() {
            return setInstance(CustomDialogFragment.newInstanceForceOkDialog(0));
        }

        public CustomDialogFragment createNoButton() {
            return setInstance(CustomDialogFragment.newInstanceNoButton(0));
        }

        public CustomDialogFragment createForceCancelDialog() {
            return setInstance(CustomDialogFragment.newInstanceForceCancelDialog(0));
        }

        private CustomDialogFragment setInstance(CustomDialogFragment cdf) {
            if (P.mTitle != null) {
                cdf = cdf.setTitle(P.mTitle);
            }
            if (P.mPositiveButtonTitle != null) {
                cdf = cdf.setPositiveButtonText(P.mPositiveButtonTitle);
            }
            if (P.mNeutralButtonTitle != null) {
                cdf = cdf.setNeutralButtonText(P.mNeutralButtonTitle);
            }
            if (P.mNegativeButtonTitle != null) {
                cdf = cdf.setNegativeButtonText(P.mNegativeButtonTitle);
            }
            if (P.mDialogFragmentListener != null) {
                cdf = cdf.setListener(P.mLintenerId, P.mDialogFragmentListener);
            }
            if (P.mBackgroundColor != 0) {
                cdf = cdf.setBackgroundColor(P.mBackgroundColor);
            }
            if (P.mBackgroundResId != 0) {
                cdf = cdf.setBackgroundResource(P.mBackgroundResId);
            }
            if (P.mButtonBackgroundColor != 0) {
                cdf = cdf.setButtonBackgroundColor(P.mButtonBackgroundColor);
            }
            if (P.mButtonBackgroundResId != 0) {
                cdf = cdf.setButtonBackgroundResource(P.mButtonBackgroundResId);
            }
            if (P.mView != null) {
                cdf = cdf.setView(P.mView);
            }
            return cdf;
        }

        private CustomDialogFragment cNoButton() {
            return CustomDialogFragment.newInstanceNoButton(0);
        }
    }

    protected int getResourceId(String name, String defType) {
        int ret = getResources().getIdentifier(name, defType, getActivity().getPackageName());
        if (ret == 0) {
            ret = getResources().getIdentifier(name, defType, this.getClass().getPackage().getName());
        }
        return ret;
    }
}

