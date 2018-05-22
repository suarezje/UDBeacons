package co.edu.ingsw.udstrital.udbeacons.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.ingsw.udstrital.udbeacons.R;
import static android.Manifest.permission.READ_CONTACTS;

public class RegistryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordView2;
    private EditText mId;
    private EditText mExternalID;
    private EditText mName;
    private EditText mLastName;
    private Button registryButton;

    private View mProgressView;
    private View mRegistryFormView;
    private static final String WS_REGISTRY_URL = "http://35.231.239.50:8080/UDBeaconServices/services/registerService/register";
    private String registryErrorMessage;
    private Boolean isRegistred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getBaseContext();


        setContentView(R.layout.activity_registry);
        // Set up the registry form.
        mEmailView = (EditText) findViewById(R.id.email_registry);
        mPasswordView = (EditText) findViewById(R.id.password_registry);
        mPasswordView2 = (EditText) findViewById(R.id.password_registry2);
        mId = (EditText) findViewById(R.id.user_id_registry);
        mExternalID = (EditText) findViewById(R.id.user_external_code_registry);
        mName = (EditText) findViewById(R.id.name_registry);
        mLastName = (EditText) findViewById(R.id.lastname_registry);
        registryButton = (Button) findViewById(R.id.registry_action_button);

        registryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToRegistry();
            }
        });

        mRegistryFormView = findViewById(R.id.registry_form);
        mProgressView = findViewById(R.id.registry_progress);
        isRegistred = Boolean.FALSE;
        populateAutoComplete();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    /**
     * Shows the progress UI and hides the registry form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistryFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), RegistryActivity.ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(RegistryActivity.ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegistryActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
    }

    private void attemptToRegistry() {
        //Validate fields
        // Store values at the time of the registry attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password2 = mPasswordView2.getText().toString();
        String name = mName.getText().toString();
        String lastName = mLastName.getText().toString();
        String id = mId.getText().toString();
        String externalID = mExternalID.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.equals(password, password2)) {
            mPasswordView.setError(getString(R.string.error_different_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //Check for non empty names and IDs
        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_empty_name));
            focusView = mName;
            cancel = true;
        }
        if (TextUtils.isEmpty(lastName)) {
            mLastName.setError(getString(R.string.error_empty_lastname));
            focusView = mLastName;
            cancel = true;
        }
        if (TextUtils.isEmpty(id)) {
            mId.setError(getString(R.string.error_empty_id));
            focusView = mId;
            cancel = true;
        }
        if (TextUtils.isEmpty(externalID)) {
            mExternalID.setError(getString(R.string.error_empty_externalID));
            focusView = mExternalID;
            cancel = true;
        }

        //Check for valid IDs
        if (!TextUtils.isDigitsOnly(id)) {
            mId.setError(getString(R.string.error_invalid_id));
            focusView = mId;
            cancel = true;
        }
        if (!TextUtils.isDigitsOnly(externalID)) {
            mExternalID.setError(getString(R.string.error_invalid_externalID));
            focusView = mExternalID;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt registry and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registry attempt.
            showProgress(true);

            JSONObject js = new JSONObject();
            try {
                js.put("idUser", mId.getText().toString());
                js.put("externalCodeUser", mExternalID.getText().toString());
                js.put("firstName", mName.getText().toString());
                js.put("lastName", mLastName.getText().toString());
                js.put("passwd", mPasswordView.getText().toString());
                js.put("userName", mEmailView.getText().toString());
                js.put("idUser", mId.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, WS_REGISTRY_URL, js, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.getString("code").equals("00")){
                                    isRegistred = Boolean.TRUE;
                                }else{
                                    isRegistred = Boolean.FALSE;
                                    registryErrorMessage = response.getString("message");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                isRegistred = Boolean.TRUE;
                                registryErrorMessage = getString(R.string.registry_general_error);
                            } finally {
                                showProgress(false);
                                if(isRegistred){
                                    Toast.makeText(context, getString(R.string.registry_ok), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(context, getString(R.string.registry_fail) + registryErrorMessage, Toast.LENGTH_LONG).show();
                                    mEmailView.requestFocus();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            isRegistred = Boolean.FALSE;
                            registryErrorMessage = getString(R.string.failed_com_to_server);
                            Toast.makeText(context, getString(R.string.registry_fail) + registryErrorMessage, Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("idUser", mId.getText().toString());
                    params.put("externalCodeUser", mExternalID.getText().toString());
                    params.put("firstName", mName.getText().toString());
                    params.put("lastName", mLastName.getText().toString());
                    params.put("passwd", mPasswordView.getText().toString());
                    params.put("userName", mEmailView.getText().toString());
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

}
