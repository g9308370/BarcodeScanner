package com.genki.user.barcodescanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class FragmentBluetooth extends Fragment {

    private static Handler mhandler;
    private EditText ed_input;
    private TextView tv_record;
    private Button btn_start;
    private Button btn_stop;
    private Context context;
    private boolean threadSwitch;

    private Thread checkThread;
    private HttpRequestToSMSServer httpRequestToSMSServer;

    private String scanText;
    private int loopcount;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        findAllView();
        initFragment();
        setAllListener();
        start_bluetooth_scan();
        }

    private void findAllView(){
        ed_input = (EditText) getActivity().findViewById(R.id.et_fragment_bluetooth_input);
        btn_start = (Button) getActivity().findViewById(R.id.btn_fragment_bluetooth_start);
        btn_stop = (Button) getActivity().findViewById(R.id.btn_fragment_bluetooth_stop);
        tv_record = (TextView) getActivity().findViewById(R.id.tv_fragment_bluetooth_test);


    }
    private void initFragment(){

        context=getActivity();

        mhandler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                //這裡執行收到伺服器回應後的動作
                /*
                tv_record.setText(tv_record.getText().toString()+"\r\n"
                        +httpRequestToSMSServer.getName()
                        +"\r\n時間 :"+httpRequestToSMSServer.getArrived_at());*/

                BigToast t=new BigToast(context,httpRequestToSMSServer.getName(),httpRequestToSMSServer.getArrived_at());
                t.show();


            }

        };
        tv_record.setText("");
        ed_input.setText("");
        ed_input.setEnabled(false);
        btn_stop.setEnabled(false);
        threadSwitch = false;
        loopcount=0;
    }
    private void setAllListener(){

        btn_start.setOnClickListener(start_btn_listener);
        btn_stop.setOnClickListener(stop_btn_listener);

    }
    private void scanTextAnimation() {

        if (loopcount >= 3) {
            loopcount = 0;
            scanText = "";
        } else {
            loopcount++;
            scanText += ".";
        }
        tv_record.setText("Scanning" + scanText);
        if (threadSwitch == false)
            tv_record.setText("");
    }
    private void start_bluetooth_scan(){
        //starting entering
        Log.d("BluetoothInput App==>", "Start listening enter words");

        threadSwitch = true;

        ed_input.setEnabled(true);
        ed_input.requestFocus();
        btn_start.setEnabled(false);
        btn_stop.setEnabled(true);
        tv_record.setText("Scanning");
        loopcount=0;
        scanText="";
        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {

                    do {

                        Thread.sleep(1000);
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scanTextAnimation();
                            }
                        });
                        if (isFormal(getEditTextString()))
                            mhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInputIsCorrect();
                                }
                            });
                    } while (threadSwitch);

                    Log.d("BluetoothInput App==>", "Thread already Stop!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        checkThread.start();

    }
    private View.OnClickListener start_btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            start_bluetooth_scan();
        }
    };
    private View.OnClickListener stop_btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //stopping entering
            Log.d("BluetoothInput App==>", "Stop listening");
            threadSwitch = false;
            btn_stop.setEnabled(false);
            btn_start.setEnabled(true);
            ed_input.setEnabled(false);
            tv_record.setText("");
        }
    };
    private String getEditTextString() {
        if (ed_input.getText().toString() == null)
            return "";
        return ed_input.getText().toString();

    }
    private boolean isFormal(String str) {
        if (str.length() != 7 || !(str.contains("cc"))) {
            //Log.d("BluetoothInput App==>","錯誤的輸入"+str);
            return false;
        } else
            return true;
    }
    private void onInputIsCorrect() {

        //HttpRequestToSMSServer為自建Class 傳入(Context , Handler , ScanCode)    Handler必須複寫handlemessge(msg);
        //收到來自伺服器的回應時，自動調用Handler.handleMessage();

        httpRequestToSMSServer=new HttpRequestToSMSServer(context,mhandler,getEditTextString());
        httpRequestToSMSServer.httpRequest();

        ed_input.setText("");
        ed_input.requestFocus();


    }

    @Override
    public void onDetach() {
        super.onDetach();
        threadSwitch=false;
    }

    @Override
    public void onStop() {
        super.onStop();
        //確保Thread不再繼續執行
        threadSwitch=false;
    }
}
