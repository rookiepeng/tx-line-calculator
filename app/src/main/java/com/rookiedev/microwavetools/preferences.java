package com.rookiedev.microwavetools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class preferences extends ActionBarActivity {
    private static final String SHARED_PREFS_NAME = "com.rookiedev.microwavetools_preferences";
    // accurate of the result
    // EditText dialogInput = new EditText(this);
    private preferences preference;
    private ListView listView;
    private String[] titles = {"Accuracy", "Feedback"};
    private String[] texts = {"the number of decimal digits", "contact with the developer"};
    private String[] datas = {"", ""};
    private DialogInterface.OnClickListener warnningdialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    break;

                // case DialogInterface.BUTTON_NEGATIVE:
                // No button clicked
                // break;
            }
        }
    };
    private String DecimalLength; // the length of the Decimal,

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        readSharedPref();
        datas[0] = DecimalLength;
        listView = (ListView) this.findViewById(R.id.listPrefences);
        listView.setAdapter(new ListViewAdapter(titles, texts, datas));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                // Object o = listView.getItemAtPosition(position);
                switch (position) {
                    case 0:
                        showDialog(0);
                        break;
                    case 1:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"rookie.dev@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "");
                        i.putExtra(Intent.EXTRA_TEXT, "");
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(preference, getResources().getString(R.string.noEmail), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // do something what you want
        super.onBackPressed();
    }

    private void readSharedPref() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
                Activity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // universal parameters
        DecimalLength = prefs.getString("DecimalLength", "2");
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                final SharedPreferences prefs = getSharedPreferences(
                        SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
                final EditText dialogInput = new EditText(this);
                dialogInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                dialogInput.requestFocus(1);
                // dialogInput.setText(prefs.getString("DecimalLength", "2"));
                // dialogInput.selectAll();
                return new AlertDialog.Builder(this)
                        .setTitle("Accuracy")
                        .setView(dialogInput)
                        .setMessage("the number of decimal digits")
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        // finish();
                                    }
                                })
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        int temp = Integer.parseInt(dialogInput
                                                .getText().toString());
                                        dialogInput.clearFocus();
                                        if (temp >= 0 && temp <= 10) {

                                            SharedPreferences.Editor editor = prefs
                                                    .edit();
                                            DecimalLength = Integer.toString(temp);
                                            editor.putString("DecimalLength",
                                                    DecimalLength);
                                            editor.commit();
                                            datas[0] = DecimalLength;
                                            listView.setAdapter(new ListViewAdapter(
                                                    titles, texts, datas));
                                        } else {
                                            showDialog(1);
                                            // finish();
                                        }
                                    }
                                }).create();
            case 1:
                return new AlertDialog.Builder(this).setTitle("Error")
                        .setMessage("Please input a number between 0 ~ 10")
                        .setPositiveButton(R.string.ok, warnningdialogListener)
                        .create();
            default:
                return null;
        }
    }

    public class ListViewAdapter extends BaseAdapter {
        View[] itemViews;

        public ListViewAdapter(String[] itemTitles, String[] itemTexts,
                               String[] itemData) {
            itemViews = new View[itemTitles.length];
            for (int i = 0; i < itemViews.length; i++) {
                itemViews[i] = makeItemView(itemTitles[i], itemTexts[i],
                        itemData[i]);
            }
        }

        @Override
        public int getCount() {
            return itemViews.length;
        }

        @Override
        public View getItem(int position) {
            return itemViews[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View makeItemView(String strTitle, String strText,
                                  String strData) {
            LayoutInflater inflater = (LayoutInflater) preferences.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View itemView = inflater.inflate(R.layout.pref_item, null);

            TextView title = (TextView) itemView.findViewById(R.id.itemTitle);
            title.setText(strTitle);
            TextView text = (TextView) itemView.findViewById(R.id.itemText);
            text.setText(strText);
            TextView data = (TextView) itemView.findViewById(R.id.itemData);
            data.setText(strData);
            return itemView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                return itemViews[position];
            return convertView;
        }
    }
}
