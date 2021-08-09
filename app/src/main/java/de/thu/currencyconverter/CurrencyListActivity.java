package de.thu.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class CurrencyListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        final ExchangeRateDatabase database = new ExchangeRateDatabase();
        final CurrencyCoverterAdapter adapter = new CurrencyCoverterAdapter(database);

        ListView listView = findViewById(R.id.currencyListView);
        listView.invalidate();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String name = (String) adapter.getItem(i);
                String city = database.getCapital(name);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0`?q=" + city));
                startActivity(mapIntent);
            }
        });

        listView.setAdapter(adapter);

    }
}