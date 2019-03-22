package com.example.shop;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SQLiteDatabase db;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                showAddOptions();
            }
        });

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                clearTable();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                showOptions(listItems.get(position));
            }
        });


        db = openOrCreateDatabase("shop", MODE_PRIVATE, null);
        setTitle("Shop Items");
        createTable();
        initializeContents();
    }

    void createTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS shop2(Name VARCHAR UNIQUE, Price INT);");
    }

    void initializeContents() {
        Cursor resultSet = db.rawQuery("Select * from shop2", null);
        while(resultSet.moveToNext()) {
            String res = resultSet.getString(0) + " - " + resultSet.getString(1);
            listItems.add(res);
            Log.d("DBValues", res);
        }
        adapter.notifyDataSetChanged();
        resultSet.close();
    }

    void clearTable() {
        db.execSQL("DELETE FROM shop2;");
        listItems.clear();
        initializeContents();
    }

    void insertIntoDb(String name, Number price) {
        try {
            String check = "SELECT * FROM shop2 WHERE Name = '" + name + "';";
            Cursor resultSet = db.rawQuery(check, null);
            if (resultSet.moveToNext()) {
                Toast t = Toast.makeText(MainActivity.this, "Name already exists", Toast.LENGTH_LONG);
                t.show();
                return;
            }
            resultSet.close();
            String tmp = "INSERT INTO shop2 values ('" + name + "', " + price.toString() + ");";
            db.execSQL(tmp);
        } catch (Error err) {}
    }

    void showOptions(String value) {
        final String itemName;
        final String price;

        String[] parts = value.split("-");
        itemName = parts[0].substring(0, parts[0].length() - 1);
        price = parts[1].substring(1, parts[1].length());


        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Item Options")
                .setMessage("What do you want to do with this entry?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showUpdateOptions(itemName, price);
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeEntry(itemName, price);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("Close", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    void showAddOptions() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.add_dialog);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.name);
        final EditText price = dialog.findViewById(R.id.price);

        Button add = dialog.findViewById(R.id.add);
        Button close = dialog.findViewById(R.id.close);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String itemName = name.getText().toString();
                String itemPrice = price.getText().toString();

                insertIntoDb(itemName, Integer.parseInt(itemPrice));
                listItems.clear();
                initializeContents();
                dialog.dismiss();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                dialog.dismiss();
            }
        });
    }

    void showUpdateOptions( final String oItemName, final String oItemPrice) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.update_dialog);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.name);
        final EditText price = dialog.findViewById(R.id.price);

        name.setText(oItemName);
        price.setText(oItemPrice);

        Button update = dialog.findViewById(R.id.update);
        Button close = dialog.findViewById(R.id.close);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String itemName = name.getText().toString();
                String itemPrice = price.getText().toString();

                removeEntry(oItemName, oItemPrice);
                insertIntoDb(itemName, Integer.parseInt(itemPrice));
                listItems.clear();
                initializeContents();
                dialog.dismiss();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                dialog.dismiss();
            }
        });
    }

    void removeEntry(String itemName, String price) {
        db.execSQL("DELETE FROM shop2 WHERE Name ='" + itemName + "';");
        listItems.clear();
        initializeContents();
    }

    void sortElements() {
        listItems.clear();
        Cursor resultSet = db.rawQuery("Select * from shop2 ORDER BY Price", null);
        while(resultSet.moveToNext()) {
            String res = resultSet.getString(0) + " - " + resultSet.getString(1);
            listItems.add(res);
            Log.d("DBValues", res);
        }
        adapter.notifyDataSetChanged();
        resultSet.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            sortElements();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected( MenuItem item ) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
