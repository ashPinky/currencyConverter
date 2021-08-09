package de.thu.currencyconverter;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CurrencyConverterJobService extends JobService {
    CurrencyConverterAsyncTask converterAsyncTask = new CurrencyConverterAsyncTask(this);



    @Override
    public boolean onStartJob(JobParameters params) {
        converterAsyncTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private static class CurrencyConverterAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {

        private final JobService jobService;
        public CurrencyConverterAsyncTask(JobService jobService) {
            this.jobService = jobService;
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            ExchangeRateDatabase currencyDatabase = new ExchangeRateDatabase();
            String query = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
            try {
                URL url = new URL(query);

                URLConnection connection = url.openConnection();

                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(connection.getInputStream(),connection.getContentEncoding());
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if ("Cube".equals(parser.getName()) && parser.getAttributeCount() == 2) {
                            String currencyName = parser.getAttributeValue(null, "currency");
                            String currencyExchangeRate = parser.getAttributeValue(null, "rate");
                            double rate = Double.parseDouble(currencyExchangeRate);
                            currencyDatabase.setExchangeRate(currencyName, rate);
                        }
                    }
                    eventType = parser.next();
                }

            } catch (MalformedURLException e) {
                Log.e("MalformedURLException", "MalformedURLException");
            } catch (IOException e) {
                Log.e("IOException", "IOException");
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                Log.e("XmlPullParserException", "XmlPullParserException");
            }

            SharedPreferences prefs = jobService.getSharedPreferences("Update",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            for (String currency : currencyDatabase.getCurrencies()) {
                String currencyRate = Double.toString(currencyDatabase.getExchangeRate(currency));
                editor.putString(currency, currencyRate);
            }

            editor.apply();

            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobService.jobFinished(jobParameters, false);
        }

    }
}
