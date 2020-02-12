package com.example.trackmyexpense;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText editPurpose, editExpense;
    Button add, view, del;
    Switch desc;
    DBHelper mdb;
    public TextView tot, last30tot;

    public void disp_total() {
        int x = mdb.last30sum();
        int y = mdb.sum();
        if(x == 0) last30tot.setText(R.string.no_num);
        else last30tot.setText(getString(R.string.tot30days, x));
        tot.setText(getString(R.string.main_total, y));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mdb = DBHelper.getInstance(this);

        editPurpose = findViewById(R.id.input_purpose);
        editExpense = findViewById(R.id.input_expense);

        del = findViewById(R.id.btn_delete);
        add = findViewById(R.id.btn_add);
        view = findViewById(R.id.btn_view);

        desc = findViewById(R.id.desc_switch);

        tot = findViewById(R.id.total_view);
        last30tot = findViewById(R.id.totview30);

        //Creating definition for new thread to set TextView
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                disp_total();
            }
        });
        t1.start();
            //New Thread to retrieve Total on run-time and setTextView to display the same
        AddData();
        ViewData();
        DelData();

    }

    public void AddData() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(editPurpose.getText().toString().isEmpty() || editExpense.getText().toString().isEmpty())
                            Toast.makeText(MainActivity.this, "Inputs cannot be empty", Toast.LENGTH_SHORT).show();
                        else {
                            boolean get_status = mdb.check_update(editPurpose.getText().toString());
                            if(get_status) {
                                boolean isUp = mdb.update(editPurpose.getText().toString(), Integer.parseInt(editExpense.getText().toString()));
                                if (isUp) {
                                    editPurpose.setText("");
                                    editExpense.setText("");
                                    Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(MainActivity.this, "Unable to update data", Toast.LENGTH_LONG).show();
                            }
                            else {
                                boolean isIn = mdb.insert(editPurpose.getText().toString(), Integer.parseInt(editExpense.getText().toString()));
                                if (isIn) {
                                    editPurpose.setText("");
                                    editExpense.setText("");
                                    Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(MainActivity.this, "Data not inserted", Toast.LENGTH_LONG).show();
                            }
                        }
                        disp_total();
                    }
                });

            }
        });
    }

    public void ViewData() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(desc.isChecked()) {
                            Cursor r = mdb.get_desc();

                            if (r.getCount() == 0) {
                                showMessage("Error", "No Data");
                                return;
                            }
                            //StringBuilder builder = new StringBuilder();
                            StringBuffer buffer = new StringBuffer();
                            int f1 = mdb.sum();
                            while (r.moveToNext()) {
                                //SpannableString id = new SpannableString(r.getString(0));
                                //id.setSpan(new StyleSpan(Typeface.BOLD), 0, id.length(), 0);
                                //buffer.append("ID: " + id);
                                buffer.append("ID: " + r.getString(0) + "\n");
                                buffer.append("Reason: " + r.getString(1) + "\n");

                                Double per = Double.parseDouble(r.getString(2))/f1 * 100;  //calculate percentage of total expenditure for current entry
                                DecimalFormat df = new DecimalFormat("#.##");
                                buffer.append("Rs: " + r.getString(2) + " (" + df.format(per) + "%)\n\n");
                            }
                            r.close(); //close cursor to prevent leaks
                            showMessage("Total Rs. " + f1, buffer.toString());
                        }
                        else {          //get non sorted values if the switch if toggled off (default)
                            Cursor rs = mdb.get();
                            if (rs.getCount() == 0) {
                                showMessage("Error", "No Data");
                                return;
                            }

                            StringBuffer buffer = new StringBuffer();
                            int f1 = mdb.sum();
                            while (rs.moveToNext()) {
                                buffer.append("No: " + rs.getString(0) + "\n");
                                buffer.append("Reason: " + rs.getString(1) + "\n");

                                Double per = Double.parseDouble(rs.getString(2))/f1 * 100;  //calculate percentage
                                DecimalFormat df = new DecimalFormat("#.##");       //round off to 2 decimal places
                                buffer.append("Rs: " + rs.getString(2) + " (" + df.format(per) + "%)\n\n");
                            }

                            rs.close();     //cursor close
                            showMessage("Total Rs. " + f1, buffer.toString());
                        }
                        disp_total();
                    }
                });
            }
        });
    }

    public void DelData() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Delete Data");

                        final EditText num = new EditText(MainActivity.this);
                        num.setInputType(InputType.TYPE_CLASS_NUMBER);
                        num.setHint("Enter the ID to be deleted");
                        builder.setView(num);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ip = num.getText().toString();
                                if(!ip.isEmpty()) {
                                    Integer x = mdb.remove(ip);
                                    if(x > 0)
                                        Toast.makeText(MainActivity.this, "Data deleted", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(MainActivity.this, "Data not deleted", Toast.LENGTH_SHORT).show();
                                }

                                else {
                                    Toast.makeText(MainActivity.this, "ID cannot be null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
            }
        });
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}
