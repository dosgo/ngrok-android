package com.a16v16.ngrok_c;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {
    String exec_path="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv=(TextView) findViewById(R.id.tv);

        tv.setText("未启动");
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
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {





                    Thread t = new Thread(new Runnable(){
                        public void run(){
                            try {
                                DatagramSocket ds = new DatagramSocket();
                                byte[] buf = "{\"cmd\":\"exit\"}".getBytes();
                                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), 1885);//10000为定义的端口
                                ds.send(dp);
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
                    }, 1000);


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
            Log.e("########", line);
            Message message=new Message();
            Bundle bundle=new Bundle();
            bundle.putString("line", line);
            message.setData(bundle);//bundle传值，耗时，效率低
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
                    Bundle b = msg.getData();
                    String line = b.getString("line");
                    TextView out=(TextView) findViewById(R.id.out);;
                    out.setText(out.getText()+line+"\r\n");
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
}



