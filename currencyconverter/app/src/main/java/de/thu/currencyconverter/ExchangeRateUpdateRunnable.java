package de.thu.currencyconverter;

import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateRunnable implements Runnable{

    private MainActivity mainActivity;

    public ExchangeRateUpdateRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        mainActivity.currencyDatabase = new ExchangeRateDatabase();

        String queryString = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        try {
            URL url = new URL(queryString);

            URLConnection connection = url.openConnection();

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(connection.getInputStream(),connection.getContentEncoding());

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("Cube".equals(parser.getName()) && parser.getAttributeCount() == 2) {
                        String currencyName = parser.getAttributeValue(null, "currency");
                        String currencyRate = parser.getAttributeValue(null, "rate");
                        double rate = Double.parseDouble(currencyRate);

                        mainActivity.currencyDatabase.setExchangeRate(currencyName, rate);
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

        //Toast

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast t = Toast.makeText(mainActivity.getApplicationContext(),
                        "Update successful!", Toast.LENGTH_LONG);
                t.show();
            }
        });
    }
}
