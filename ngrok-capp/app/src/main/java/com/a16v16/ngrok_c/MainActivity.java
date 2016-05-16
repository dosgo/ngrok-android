package com.a16v16.ngrok_c;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Handler;

import java.io.FileInputStream;
import java.lang.Runnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.SharedPreferences.Editor;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    String exec_path="";
    String privatepath="";
    TextView out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv=(TextView) findViewById(R.id.tv);
        out=(TextView) findViewById(R.id.out);
        EditText cmd=(EditText) findViewById(R.id.cmd);
        final ToggleButton autostart=(ToggleButton) findViewById(R.id.autostart);
        out.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
        tv.setText("未启动");
        privatepath=getFilesDir()+"";
        Intent intent=getIntent();
        if(intent.getBooleanExtra("autostart",false)){
            final  String exec_path=privatepath+"/ngrokc";
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE); //私有数据
            final  String cmdstr=sharedPreferences.getString("cmd","");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        MyLog.writeLogtoFile1("ngrokc.log","ngrokc","autostart...");
                        MyLog.writeLogtoFile1("ngrokc.log","exec_path",exec_path);
                        MyLog.writeLogtoFile1("ngrokc.log","cmdstr",cmdstr);
                        execCmd(exec_path + " " + cmdstr);

                    } catch (Exception e) {
                        Log.i("jni", "Exception：" + e.getMessage());
                        MyLog.writeLogtoFile1("ngrokc.log","ngrokc","autostart.Exception");
                    }
                }
            }).start();
            moveTaskToBack(true);
        }



        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.this.getPackageName(), Context.MODE_PRIVATE); //私有数据
        String cmdstr=sharedPreferences.getString("cmd","");
        if(cmdstr.length()>0){
            cmd.setText(cmdstr);
        }
        autostart.setChecked(sharedPreferences.getBoolean("autostart",false));



        try{
            exec_path=MainActivity.this.getFilesDir()+"/ngrokc";
            if(!fileIsExists(exec_path)) {
                copyBigDataToSD(Build.CPU_ABI + "/ngrok-c", exec_path);
            }
            File exe_file = new File(exec_path);
            exe_file.setExecutable(true, true);
            //execCmd(exec_path);
        }catch (Exception  e){

            Log.i("jni","Exception："+e.getMessage());
        }
        Button start=(Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText cmd=(EditText) findViewById(R.id.cmd);

                    //execute the task
                    Thread t1 = new Thread(new Runnable(){
                        public void run(){
                            try {
                                execCmd(exec_path + " " + cmd.getText());
                            } catch (Exception  e){
                                e.printStackTrace();
                                Log.i("jni","Exception："+e.getMessage());
                            }
                        }});
                    t1.start();

                    TextView tv=(TextView) findViewById(R.id.tv);;
                    tv.setText("已启动");

            }
        });

        Button reboot=(Button) findViewById(R.id.reboot);
        reboot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    TextView out=(TextView) findViewById(R.id.out);;
                    out.setText("");
                    Thread t = new Thread(new Runnable(){
                        public void run(){
                            try {
                                DatagramSocket ds = new DatagramSocket();
                                byte[] buf = "{\"cmd\":\"exit\"}".getBytes();
                                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 1885);//10000为定义的端口
                                ds.send(dp);
                                Log.i("jni","send...");
                                ds.close();
                            } catch (Exception  e){
                                    e.printStackTrace();
                                    Log.i("jni","Exception："+e.getMessage());
                                }
                        }
                    });
                    t.start();

                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            final EditText cmd=(EditText) findViewById(R.id.cmd);
                            TextView tv=(TextView) findViewById(R.id.tv);;

                                //execute the task
                                Thread t1 = new Thread(new Runnable(){
                                    public void run(){
                                        try {
                                        execCmd(exec_path + " " + cmd.getText());
                                        } catch (Exception  e){
                                            e.printStackTrace();
                                            Log.i("jni","Exception："+e.getMessage());
                                        }
                                    }});
                                t1.start();
                                tv.setText("已启动");

                        }
                    }, 3000);


            }
        });

        final Button set=(Button) findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText cmd=(EditText) findViewById(R.id.cmd);
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.this.getPackageName(), Context.MODE_PRIVATE); //私有数据
                Editor editor = sharedPreferences.edit();//获取编辑器
                editor.putString("cmd", cmd.getText()+"");
                editor.commit();//提交修改
            }
        });
        autostart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.this.getPackageName(), Context.MODE_PRIVATE); //私有数据
                Editor editor = sharedPreferences.edit();//获取编辑器
                editor.putBoolean("autostart",autostart.isChecked());
                editor.commit();//提交修改
        }});

        final Button clear=(Button) findViewById(R.id.set);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyLog.ConsoleLogFileClear(privatepath,"ngrok.out");
                Message message=new Message();
                message.what=1;//标志是哪个线程传数据
                myHandler.sendMessage(message);//发送message信息
            }
        });



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

    private void ReloadConsole(){
        String ConsoleStr=ReadTxtFile(privatepath+"/ngrok.out");
        if(ConsoleStr.length()>0){
            out.setText(ConsoleStr);
        }
    }

    private void execCmd(String cmd) throws IOException {
        //清空日志
        MyLog.ConsoleLogFileClear(privatepath,"ngrok.out");
        Log.i("jni","start ngrok-c");
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        Log.i("jni","start1 ngrok-c");
        while (null != (line = br.readLine())) {
            Log.e("########", line);
            MyLog.writeLogtoFile1("ngrokc.log","",line);
            MyLog.ConsoleLogtoFile(privatepath,"ngrok.out",line);
            Message message=new Message();
            message.what=1;//标志是哪个线程传数据
            myHandler.sendMessage(message);//发送message信息
        }
        /*
        try {
          //  process.waitFor();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }*/
    }


    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ReloadConsole();
                    break;
            }
            super.handleMessage(msg);
        }
    };

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

    public static String ReadTxtFile(String strFilePath)
    {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory())
        {
            Log.d("TestFile", "The File doesn't not exist.");
        }
        else
        {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            }
            catch (java.io.FileNotFoundException e)
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            catch (IOException e)
            {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReloadConsole();
    }
}



