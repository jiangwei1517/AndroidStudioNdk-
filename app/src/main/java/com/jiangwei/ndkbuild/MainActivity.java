package com.jiangwei.ndkbuild;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private Button btnOneToTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final JniUtils jniUtils = new JniUtils();
        final String getStrFromC = jniUtils.testJni();
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, getStrFromC, Toast.LENGTH_SHORT).show();
            }
        });
        btnOneToTwo = (Button) findViewById(R.id.btn_one_to_two);
        btnOneToTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                // jniUtils.oneFileToTwo(localPath + File.separator + "text3.md", localPath + File.separator +
                // "text4.md",
                // localPath + File.separator + "text5.md");
                //
                // readFile(localPath, "text4.md");
                //
                // readFile(localPath, "text5.md");

                jniUtils.twoFileInOne(localPath + File.separator + "text4.md", localPath + File.separator + "text5.md",
                        localPath + File.separator + "text6.md");
                readFile(localPath, "text6.md");
            }
        });
    }

    private void readFile(String localPath, String name) {
        File firstFile = new File(localPath + File.separator + name);
        try {
            FileInputStream fs = new FileInputStream(firstFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
            String b = reader.readLine();
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str);
            }
            System.out.println(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
