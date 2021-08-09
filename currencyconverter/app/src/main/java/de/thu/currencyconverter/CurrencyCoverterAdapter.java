package de.thu.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrencyCoverterAdapter extends BaseAdapter {

    private ExchangeRateDatabase currencyDatabase;

    public CurrencyCoverterAdapter(ExchangeRateDatabase currencyDatabase) {
        this.currencyDatabase = currencyDatabase;
    }

    @Override
    public int getCount() {
        return currencyDatabase.getCurrencies().length;
    }

    @Override
    public Object getItem(int position) {
        return currencyDatabase.getCurrencies()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Context context = parent.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_currency_list, null, false);
        }


        String currencyExchangeName = currencyDatabase.getCurrencies()[position];
        TextView textView = view.findViewById(R.id.currencyNameView);
        textView.setText(currencyExchangeName);
        ImageView imageView = view.findViewById(R.id.flagImageView);

        int flagImageId = context.getResources().getIdentifier("flag_" + currencyExchangeName.toLowerCase(),
                "drawable", context.getPackageName());
        imageView.setImageResource(flagImageId);
        TextView rateView = view.findViewById(R.id.currencyRateView);

        double currencyRate = currencyDatabase.getExchangeRate(currencyExchangeName);
        String exchangeRateText = Double.toString(currencyRate);
        rateView.setText(exchangeRateText);


        return view;
    }
}
