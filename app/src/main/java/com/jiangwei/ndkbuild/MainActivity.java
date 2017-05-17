package com.jiangwei.ndkbuild;

import android.content.Intent;
import android.net.Uri;
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
    private Button patch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final JniUtils jniUtils = new JniUtils();
        final String getStrFromC = jniUtils.testJni();
        btn = (Button) findViewById(R.id.btn);
        patch = (Button) findViewById(R.id.patch);
        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String oldPath = localPath + File.separator + "old.apk";
                String newPath = localPath + File.separator + "new1.apk";
                String patchPath = localPath + File.separator + "old-to-new.apk";
                File oldF = new File(oldPath);
                File patchF = new File(patchPath);
                if (!oldF.exists()) {
                    Toast.makeText(MainActivity.this, "没有文件old.apk", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!patchF.exists()) {
                    Toast.makeText(MainActivity.this, "没有文件patch.apk", Toast.LENGTH_SHORT).show();
                    return;
                }
                jniUtils.patchApk(oldPath, newPath, patchPath);
                File newF = new File(newPath);
                if (!newF.exists()) {
                    Toast.makeText(MainActivity.this, "没有文件new.apk", Toast.LENGTH_SHORT).show();
                    return;
                }
                install(newPath);
            }
        });
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
                /*
                 * 文件拆分
                 */
                jniUtils.oneFileToTwo(localPath + File.separator + "text3.md", localPath + File.separator + "text4.md",
                        localPath + File.separator + "text5.md");

                readFile(localPath, "text4.md");

                readFile(localPath, "text5.md");

                /*
                 * 文件合并
                 */
                // jniUtils.twoFileInOne(localPath + File.separator + "text4.md", localPath + File.separator +
                // "text5.md",
                // localPath + File.separator + "text6.md");
                // readFile(localPath, "text6.md");
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

    public void install(String apkPath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        startActivity(i);
    }
}
