package com.payUMoney.sdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payUMoney.sdk.SdkConstants;
import com.payUMoney.sdk.SdkHomeActivityNew;
import com.payUMoney.sdk.SdkLuhn;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.SdkSetupCardDetails;
import com.payUMoney.sdk.dialog.SdkCustomDatePicker;
import com.payUMoney.sdk.entity.SdkCard;
import com.payUMoney.sdk.utils.SdkHelper;
import com.payUMoney.sdk.utils.SdkLogger;

import org.json.JSONException;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by sagar on 20/5/15.
 */
public class SdkDebit extends View {

    MakePaymentListener mCallback = null;
    private int expiryMonth = 7,expiryYear = 2025;
    private String cardNumber = "",cvv = "";
    int mYear = 0,mMonth = 0,mDay = 0;
    SdkCustomDatePicker mDatePicker = null;
    Boolean isCardNumberValid = Boolean.FALSE,isExpired = Boolean.TRUE,isCvvValid = Boolean.FALSE,card_store_check = Boolean.TRUE;
    Drawable cardNumberDrawable = null,calenderDrawable = null,cvvDrawable = null;
    private CheckBox mCardStore = null;
    private EditText mCardLabel = null,cardNumberEditText = null,expiryDatePickerEditText = null,cvvEditText = null;
    View debitCardDetails = null;
    Context mContext;
    private boolean isMaestro = false;

    public SdkDebit(Context context) {
        super(context);
        mContext = context;
        onAttach((SdkHomeActivityNew)context);

    }

    // Container Activity must implement this interface
    public interface MakePaymentListener {
        public void goToPayment(String mode, HashMap<String, Object> data) throws JSONException;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final String mode) {
        SdkLogger.d(SdkConstants.TAG, "DebitCardFragment" + "onCreateView");
        // Inflate the layout for this fragment
        debitCardDetails = inflater.inflate(R.layout.sdk_fragment_card_details, container, false);
        mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH);
        mDay = Calendar.getInstance().get(Calendar.DATE);
        mCardLabel = (EditText) debitCardDetails.findViewById(R.id.label);
        mCardStore = (CheckBox) debitCardDetails.findViewById(R.id.store_card);
        cardNumberDrawable = mContext.getResources().getDrawable(R.drawable.card);
        calenderDrawable = mContext.getResources().getDrawable(R.drawable.calendar);
        cvvDrawable = mContext.getResources().getDrawable(R.drawable.lock);
        cardNumberDrawable.setAlpha(100);
        calenderDrawable.setAlpha(100);
        cvvDrawable.setAlpha(100);
        cardNumberEditText = (EditText) debitCardDetails.findViewById(R.id.cardNumberEditText);
        expiryDatePickerEditText = (EditText) debitCardDetails.findViewById(R.id.expiryDatePickerEditText);
        cvvEditText = (EditText) debitCardDetails.findViewById(R.id.cvvEditText);

        //((TextView) debitCardDetails.findViewById(R.id.enterCardDetailsTextView)).setText(R.string.enter_debit_card_details);
        cardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, cardNumberDrawable, null);
        expiryDatePickerEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, calenderDrawable, null);
        cvvEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, cvvDrawable, null);

        (cardNumberEditText).addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardNumber = ((EditText) debitCardDetails.findViewById(R.id.cardNumberEditText)).getText().toString();
                cardNumber = cardNumber.replace(" ", "");

                if (cardNumber.startsWith("34") || cardNumber.startsWith("37"))
                    ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                else
                    ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

                String tempIssuer = SdkSetupCardDetails.findIssuer(cardNumber, "DC");
                if (tempIssuer != null && tempIssuer.equals("MAES")) {
                    // disable cvv and expiry

                    if (cardNumber.length() > 11 && SdkLuhn.validate(cardNumber)) {
                       // Toast.makeText(mContext,"Entered",Toast.LENGTH_SHORT).show();
                        isCardNumberValid = Boolean.TRUE;
                        isMaestro = true;
                        valid(((EditText) debitCardDetails.findViewById(R.id.cardNumberEditText)), SdkSetupCardDetails.getCardDrawable(getResources(), cardNumber));
                    } else {
                      //  Toast.makeText(mContext,"Entered111",Toast.LENGTH_SHORT).show();
                        isCardNumberValid = Boolean.FALSE;
                        invalid(((EditText) debitCardDetails.findViewById(R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        //resetHeader();
                    }
                } else {
                    // enable cvv and expiry
                    isMaestro = false;
                    debitCardDetails.findViewById(R.id.expiryCvvLinearLayout).setVisibility(View.VISIBLE);

                    if (cardNumber.length() > 11 && SdkLuhn.validate(cardNumber)) {
                        isCardNumberValid = Boolean.TRUE;
                        valid(((EditText) debitCardDetails.findViewById(R.id.cardNumberEditText)), SdkSetupCardDetails.getCardDrawable(getResources(), cardNumber));
                    } else {
                        isCardNumberValid = Boolean.FALSE;
                        invalid(((EditText) debitCardDetails.findViewById(R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        //resetHeader();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 4) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }
        /*If User tries to change the card number CVV should be reset to revalidate */
                isCvvValid = Boolean.FALSE;
                ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).getText().clear();
                invalid(((EditText) debitCardDetails.findViewById(R.id.cvvEditText)), cvvDrawable);

            }
        });
        ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cvv = ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).getText().toString();
                if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
                    if (cvv.length() == 4) {
                        //valid
                        isCvvValid = Boolean.TRUE;
                        valid(((EditText) debitCardDetails.findViewById(R.id.cvvEditText)), cvvDrawable);
                    } else {
                        //invalid
                        isCvvValid = Boolean.FALSE;
                        invalid(((EditText) debitCardDetails.findViewById(R.id.cvvEditText)), cvvDrawable);
                    }
                } else {
                    if (cvv.length() == 3) {
                        //valid
                        isCvvValid = Boolean.TRUE;
                        valid(((EditText) debitCardDetails.findViewById(R.id.cvvEditText)), cvvDrawable);
                    } else {
                        //invalid
                        isCvvValid = Boolean.FALSE;
                        invalid(((EditText) debitCardDetails.findViewById(R.id.cvvEditText)), cvvDrawable);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        cardNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
        debitCardDetails.findViewById(R.id.cvvEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
        expiryDatePickerEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
        mCardStore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mCardStore.isChecked()) {
                    card_store_check = Boolean.TRUE;
                    mCardLabel.setVisibility(View.VISIBLE);
                } else {
                    card_store_check = Boolean.FALSE;
                    mCardLabel.setVisibility(View.GONE);
                }
            }
        });
        SharedPreferences mPref = mContext.getSharedPreferences(SdkConstants.SP_SP_NAME, Activity.MODE_PRIVATE);
        if(mPref.getBoolean(SdkConstants.ONE_TAP_PAYMENT,false))
            mCardStore.setText("Save this card with CVV");
        else
            mCardStore.setText("Save this card");

        expiryDatePickerEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mDatePicker = new SdkCustomDatePicker((SdkHomeActivityNew) mContext);
                    mDatePicker.build(mMonth, mYear, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //positive button
                            checkExpiry((EditText) debitCardDetails.findViewById(R.id.expiryDatePickerEditText), mDatePicker.getSelectedYear(), mDatePicker.getSelectedMonth(), 0);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //negative button
                            mDatePicker.dismissDialog();
                        }
                    });
                    mDatePicker.show();
                }
                return false;
            }
        });
        debitCardDetails.findViewById(R.id.makePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SdkHelper.checkNetwork(mContext)) {
                    Toast.makeText(mContext, R.string.disconnected_from_internet, Toast.LENGTH_SHORT).show();
                } else {
                    String cardNumber = ((TextView) cardNumberEditText).getText().toString();
                    final HashMap<String, Object> data = new HashMap<>();
                    try {
                        if (cvv.equals("") || cvv == null)
                            data.put(SdkConstants.CVV, "123");
                        else
                            data.put(SdkConstants.CVV, cvv);
                        data.put(SdkConstants.EXPIRY_MONTH, Integer.valueOf(expiryMonth));
                        data.put(SdkConstants.EXPIRY_YEAR, Integer.valueOf(expiryYear));
                        data.put(SdkConstants.NUMBER, cardNumber);
                        data.put("key", ((SdkHomeActivityNew) mContext).getPublicKey());
                        if (SdkCard.isAmex(cardNumber)) {
                            data.put("bankcode", SdkConstants.AMEX);
                        } else {
                            String tempIssuer = SdkSetupCardDetails.findIssuer(cardNumber, mode);
                            if (mode.contentEquals("CC") && (!tempIssuer.contentEquals("AMEX") || !tempIssuer.contentEquals("DINR") || !tempIssuer.contentEquals("CC") || !tempIssuer.contentEquals("CC-C") || !tempIssuer.contentEquals("CC-M") || !tempIssuer.contentEquals("CC-O")))
                                tempIssuer = "CC";
                            data.put("bankcode", tempIssuer);
                        }
                        if (card_store_check.booleanValue()) {
                            if (mCardLabel.getText().toString().trim().length() == 0) {
                                if (mode.equals("DC"))
                                    data.put(SdkConstants.LABEL, "PayUmoney Debit Card");
                                else
                                    data.put(SdkConstants.LABEL, "PayUmoney Credit Card");
                            } else {
                                data.put(SdkConstants.LABEL, mCardLabel.getText().toString().toUpperCase());

                            }
                            data.put(SdkConstants.STORE, "1");

                        }

                        mCallback.goToPayment(mode, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return debitCardDetails;
    }

    private void checkExpiry(EditText expiryDatePickerEditText, int i, int i2, int i3){
        expiryDatePickerEditText.setText("" + (i2 + 1) + " / " + i);
        expiryMonth = i2 + 1;
        expiryYear = i;
        if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
            isExpired = Boolean.FALSE;
            valid(expiryDatePickerEditText, calenderDrawable);
        } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
            isExpired = Boolean.FALSE;
            valid(expiryDatePickerEditText, calenderDrawable);
        } else {
            isExpired = Boolean.TRUE;
            invalid(expiryDatePickerEditText, calenderDrawable);
        }
    }

    private void valid(EditText editText, Drawable drawable) {
        drawable.setAlpha(255);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if(isMaestro && isCardNumberValid.booleanValue())
        {
            expiryDatePickerEditText.setHint("Optional");
            cvvEditText.setHint("Optional");
            hideKeyboardIfShown();
            debitCardDetails.findViewById(R.id.makePayment).setEnabled(true);
        }
        else if (isCardNumberValid.booleanValue() && !isExpired.booleanValue() && isCvvValid.booleanValue()) {
            hideKeyboardIfShown();
            debitCardDetails.findViewById(R.id.makePayment).setEnabled(true);

        } else {
            debitCardDetails.findViewById(R.id.makePayment).setEnabled(false);
        }
    }

    private void invalid(EditText editText, Drawable drawable) {
        if(expiryDatePickerEditText != null && !isMaestro)
            expiryDatePickerEditText.setHint(R.string.expires);
        if(cvvEditText != null && !isMaestro)
            cvvEditText.setHint(R.string.cvv);
        if(isMaestro){
            expiryDatePickerEditText.setHint("Optional");
            cvvEditText.setHint("Optional");
        }
        drawable.setAlpha(100);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if(isMaestro)
            debitCardDetails.findViewById(R.id.makePayment).setEnabled(true);
        else
            debitCardDetails.findViewById(R.id.makePayment).setEnabled(false);
    }

    private void makeInvalid() {
        if (!isCardNumberValid.booleanValue() && cardNumber.length() > 0 && !cardNumberEditText.isFocused())
            (cardNumberEditText).setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.error_icon), null);
        if (!isCvvValid.booleanValue() && cvv.length() > 0 && !debitCardDetails.findViewById(R.id.cvvEditText).isFocused())
            ((EditText) debitCardDetails.findViewById(R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.error_icon), null);
    }

    public void onAttach(Activity activity) {
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (MakePaymentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void onDestroy() {
        SdkLogger.e("ACT", "onDestroy called");
        debitCardDetails = null;
    }
    private void hideKeyboardIfShown() {

        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = ((SdkHomeActivityNew)mContext).getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mContext);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
