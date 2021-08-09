package de.thu.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public ExchangeRateDatabase currencyDatabase;
    private CurrencyCoverterAdapter currencyCoverterAdapter;
    private ShareActionProvider shareActionProvider;

    private static final int JOB_ID = 101;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);
        return true;
    }

    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currencyListMenuItem:
                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            case R.id.updateRatesMenuItem:
                new Thread((Runnable) new ExchangeRateUpdateRunnable(this)).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(toolbar);
        currencyDatabase = new ExchangeRateDatabase();
        currencyCoverterAdapter = new CurrencyCoverterAdapter(currencyDatabase);

        Spinner from = (Spinner) findViewById(R.id.fromValue);
        from.setAdapter(currencyCoverterAdapter);

        Spinner to = (Spinner) findViewById(R.id.toValue);
        to.setAdapter(currencyCoverterAdapter);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        service();


    }

    public void conversionButtonClicked(View view) {

        EditText amountToConvert = findViewById(R.id.enterNumber);
        TextView convertedAmount = findViewById(R.id.convertedNumber);


        if(amountToConvert.getText().toString().length() == 0) {
            convertedAmount.setText(String.format(Locale.getDefault(),"%.2f", 0.00));
            return;
        }

        double resultValue = Double.parseDouble(amountToConvert.getText().toString());
        Spinner from = findViewById(R.id.fromValue);
        String currencyFrom = (String) from.getSelectedItem();
        Spinner to = findViewById(R.id.toValue);
        String currencyTo = (String) to.getSelectedItem();
        convertedAmount.setText(String.format(Locale.getDefault(),
                "%.2f", currencyDatabase.convert(resultValue, currencyFrom, currencyTo)));

    }

    @Override
    protected void onPause() {
        super.onPause();


        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        EditText editID = findViewById(R.id.enterNumber);
        String convertValue = editID.getText().toString();
        editor.putString("convertValue", convertValue);

        TextView textViewID = findViewById(R.id.convertedNumber);
        String convertedValue = textViewID.getText().toString();
        editor.putString("convertedValue", convertedValue);

        Spinner fromValue = findViewById(R.id.fromValue);
        int spinnerFrom = fromValue.getSelectedItemPosition();
        editor.putInt("spinnerFrom", spinnerFrom);

        Spinner toValue = findViewById(R.id.toValue);
        int spinnerTo = toValue.getSelectedItemPosition();
        editor.putInt("spinnerTo", spinnerTo);

        for (String currencyRate : currencyDatabase.getCurrencies()) {
            String newRate = Double.toString(currencyDatabase.getExchangeRate(currencyRate));
            editor.putString(currencyRate, newRate);
        }

        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        String convertValue = prefs.getString("convertValue", "");
        EditText editID = findViewById(R.id.enterNumber);
        String convertedValue = prefs.getString("convertedValue", "0.00");
        TextView textViewID = findViewById(R.id.convertedNumber);
        int spinnerFrom = prefs.getInt("spinnerFrom", 0);
        Spinner fromValue = findViewById(R.id.fromValue);
        int spinnerTo = prefs.getInt("spinnerTo", 1);
        Spinner toValue = findViewById(R.id.toValue);

        for (String currencyRate : currencyDatabase.getCurrencies()) {
            String newRate = prefs.getString(currencyRate, "0.00");

            if ("EUR".equals(currencyRate)) {
                newRate = "1.00";
            }

            if (!("0.00".equals(newRate))) {
                currencyDatabase.setExchangeRate(currencyRate, Double.parseDouble(newRate));
            }
        }
        editID.setText(convertValue);
        textViewID.setText(convertedValue);
        fromValue.setSelection(spinnerFrom);
        toValue.setSelection(spinnerTo);
    }

    public void service() {
        ComponentName serviceName = new ComponentName(this, CurrencyConverterJobService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .setPersisted(true)
                    .setPeriodic(86400000)
                    .build();

            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            if (scheduler.getPendingJob(JOB_ID) == null) {
                scheduler.schedule(jobInfo);
            }
        }
    }


}