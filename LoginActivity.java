package com.vistaar.vistaarbranchapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.vistaar.vistaarbranchapp.Config.SOAP_ACTIONLAN;
import static com.vistaar.vistaarbranchapp.Config.URLLAN;

public class LoginActivity extends AppCompatActivity {
    EditText ed_phnum;
    Button btnSend;
    String phnum, app_reff, cust_name, custName,otp;
    JSONArray jsonArray;

    private static final String METHOD_NAME = "getApplication";
    ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ed_phnum = (EditText) findViewById(R.id.ed_phnum);
        btnSend = (Button) findViewById(R.id.btn_send);
        connectionDetector = new ConnectionDetector(this);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_phnum.getText().toString().isEmpty() || ed_phnum.getText().toString().length() < 10) {
                    Toast.makeText(LoginActivity.this, "Please enter your registered Mobile Number", Toast.LENGTH_LONG).show();
                } else {
                    if (connectionDetector.isConnectingToInternet())

                        new checkMobileNumberInIndus().execute();

                    else {
                        Toast.makeText(LoginActivity.this, "Please check your Internet Connection", Toast.LENGTH_LONG).show();

                    }
                }


            }
        });


    }

    public class checkMobileNumberInIndus extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressBar.setVisibility(View.VISIBLE);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            String resTxt = null;
            // Create request
            SoapObject request = new SoapObject(Config.NAMESPACELMSFRAG, METHOD_NAME);
            // Property which holds input parameters
            PropertyInfo sayHelloPI = new PropertyInfo();
            // Set Name
            sayHelloPI.setName("mobileNumber");
            // Set Value
            sayHelloPI.setValue(ed_phnum.getText().toString());
            // Set dataType
            sayHelloPI.setType(String.class);
            // Add the property to request object
            request.addProperty(sayHelloPI);

            Log.i("requestlms", request.toString());
            // Create envelope
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            // Set output SOAP object
            envelope.setOutputSoapObject(request);
            // Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URLLAN);

            try {
                // Invoke web service
                androidHttpTransport.call(SOAP_ACTIONLAN, envelope);
                // Get the response
                SoapObject response = (SoapObject) envelope.getResponse();
                // Assign it to resTxt variable static variable
                resTxt = response.toString();
                Log.i("responseLMS", resTxt);
                try {
                    if (resTxt.equals("anyType{status=FAILURE; data=Somthing is incorrent please contact System Administrator; }")) {

                    } else {

                        JSONObject jsonObject = new JSONObject(String.valueOf(response.getProperty(1)));
                        Log.i("check_jobj", jsonObject.toString());
                        try {
                            app_reff = jsonObject.getString("applicationReference");
                            jsonArray = jsonObject.getJSONArray("applicationReference");

                            if (jsonArray.length() > 0) {

                                for (int i = 1; i < jsonArray.length(); i++) {
                                    cust_name = jsonArray.getString(i);
                                    jsonArray.remove(i).equals("Application Number ");
                                    jsonArray.remove(i).equals("Account Status ");

                                    Log.i("jsonArrayLMs", cust_name.toString());
                                    custName = cust_name.replaceAll(" Borrower Name :", "");
                                    Log.i("custname", custName);
                                }
                            }
                        } catch (Exception e) {
                            //Print error
                            e.printStackTrace();
                            Log.i("exeption", "inside catch for mobile reg");
                            //Assign error message to resTxt
                        }
                        //Return resTxt to calling object
                    }
                } catch (NullPointerException ne) {
                    ne.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return resTxt;

        }

        @Override
        protected void onPostExecute(String s) {
            // progressBar.setVisibility(View.GONE);
            try {
                if (s.equals("anyType{status=FAILURE; data=Somthing is incorrent please contact System Administrator; }")) {
                    Toast.makeText(LoginActivity.this, "There is an Error connecting the Server..", Toast.LENGTH_LONG).show();

                } else {
                    if (app_reff.equalsIgnoreCase("No response")) {

                        Toast.makeText(LoginActivity.this, "Your number is not registered", Toast.LENGTH_LONG).show();

                    } else {
                        getOtpasSMS();

                  /* Intent homeIntent = new Intent(LoginActivity.this,VerifyOtpActivity.class);
                    homeIntent.putExtra("user_ID",userId);
                    homeIntent.putExtra("Otp",otp);
                    startActivity(homeIntent);*/

                    }
                }

            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }
        }

        private void getOtpasSMS() {
            Log.i("inside","inside otp");

            phnum = ed_phnum.getText().toString();
            Log.i("phnum_entered", phnum);
            LinkedHashMap params = new LinkedHashMap<>();

            params.put("Mobile", phnum);
            params.put("Token","0xH0xH01U01S0x01N");

            final JSONObject jsonObject = new JSONObject(params);
            Log.v("Vistaar Branch App", "Request String In getOtp" + jsonObject.toString());

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.getOtp, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("Vistaar Branch App", "Response String In getOtp" + response);
                            //  Log.v(Constants.TAG, "inside response" + jsonObject.toString());
                            try {
                                // JSONObject jsonObject1 = response.getJSONObject("d");
                                otp = response.getString("d");

                                Log.v("Vistaar Branch App", "Response string In getOtp" + otp);

                                Log.i("Vistaar Branch App", "otp as string" + otp);
                                Intent homeIntent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
                                homeIntent.putExtra("user_ID", phnum);
                                homeIntent.putExtra("custName", custName);
                                homeIntent.putExtra("Otp", otp);
                                startActivity(homeIntent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.i("errorlogin",error.toString());
                    Toast.makeText(LoginActivity.this, "Server Communication Error!!", Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("User-agent", System.getProperty("http.agent"));

                    return headers;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
            requestQueue.add(jsonObjectRequest);


        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {

            new AlertDialog.Builder(this, R.style.MyDialogTheme)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")

                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                            startActivity(intent);
                            finishAffinity();                            //

                        }
                    }).create().show();


        }
        return super.onKeyDown(keyCode, event);
    }
    }

