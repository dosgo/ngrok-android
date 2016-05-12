package com.a16v16.ngrok_c;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv=(TextView) findViewById(R.id.tv);

        tv.setText( Build.CPU_ABI+":"+ MainActivity.this.getFilesDir());
        try{
           String exec_path=MainActivity.this.getFilesDir()+"/ngrokc";

            Log.i("jni","0exec_path:"+exec_path);
            if(!fileIsExists(exec_path)) {
                copyBigDataToSD(Build.CPU_ABI + "/ngrok-c", exec_path);
            }
            File exe_file = new File(exec_path);
            exe_file.setExecutable(true, true);
            execCmd(exec_path+" -SER[Shost:tunnel.qydev.com,Sport:4443] -AddTun[Type:http,Lhost:192.168.2.116,Lport:80,Sdname:anddosgo]");
            //execCmd(exec_path);
        }catch (Exception  e){

            Log.i("jni","Exception："+e.getMessage());
        }

    }
    public boolean fileIsExists(String path){
        try{
            File f=new File(path);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    private void execCmd(String cmd) throws IOException {
        Log.i("jni","start ngrok-c");
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        Log.i("jni","start1 ngrok-c");
        while (null != (line = br.readLine())) {
            Log.i("jni","line:"+line);
            Log.e("########", line);
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        Log.i("jni","end ngrok-c");

    }

    private void copyBigDataToSD(String src,String strOutFileName) throws IOException
    {
        InputStream myInput=null;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        try{
         myInput = this.getAssets().open(src);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while(length > 0)
            {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();

         } catch (IOException e) {
             e.printStackTrace();

        }
    }
}



