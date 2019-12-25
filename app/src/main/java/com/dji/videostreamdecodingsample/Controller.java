package com.dji.videostreamdecodingsample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;



public class Controller extends Activity implements View.OnClickListener {

    private static final String TAG = Controller.class.getSimpleName();
    private FlightController mFlightController;
    private Gimbal mGimbal;
    private Timer mSendVirtualStickDataTimer;
    private Timer mSendGimbalDataTimer;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;
    private SendGimbalDataTask mSendGimbalDataTask;
    private static final int MSG_WHAT_SHOW_TOAST = 0;
    private static final int MSG_WHAT_UPDATE_TITLE = 1;
    private TextView titleTv;
    private float gPitch;
    private float gRoll;
    private float gYaw;

    private volatile float mPitch;
    private volatile float mRoll;
    private volatile float mYaw;
    private volatile float mThrottle;

    //控制信号
    public Handler mainHandler = new Handler(Looper.getMainLooper()) {//显示消息
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_UPDATE_TITLE:
                    if (titleTv != null) {
                        titleTv.setText((String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private Button btn_camera_up;
    private Button btn_camera_down;
    private Button btn_camera_left;
    private Button btn_camera_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        mYaw = 0;
        mPitch = 0;
        mRoll = 0;
        mThrottle = 0;
        gPitch = 0;
        initUI();//这个可以看
    }

    @Override
    public void onResume() {//停止后继续，需要初始化
        Log.e(TAG, "onResume");
        super.onResume();
        initFlightController();
    }

    //----------------------------------------------------------------------------------
    //初始化控制
    private void initFlightController() {
        Aircraft aircraft = (Aircraft) VideoDecodingApplication.getProductInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            showToast("Disconnected");
            mFlightController = null;
            mGimbal = null;
            return;
        } else {
            mFlightController = aircraft.getFlightController();//输入控制信号
            mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            mFlightController.getSimulator().setStateCallback(new SimulatorState.Callback() {//仿真器
                @Override
                public void onUpdate(final SimulatorState stateData) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            String yaw = String.format("%.2f", stateData.getYaw());
                            String pitch = String.format("%.2f", stateData.getPitch());
                            String roll = String.format("%.2f", stateData.getRoll());
                            String positionX = String.format("%.2f", stateData.getPositionX());
                            String positionY = String.format("%.2f", stateData.getPositionY());
                            String positionZ = String.format("%.2f", stateData.getPositionZ());

                            showToast("Yaw : " + yaw + ", Pitch : " + pitch + ", Roll : " + roll + "\n" + ", PosX : " + positionX +
                                    ", PosY : " + positionY +
                                    ", PosZ : " + positionZ);
                        }
                    });
                }
            });

            mGimbal = aircraft.getGimbal();


        }
    }


    //----------------------------------------------------------------------------------
    //按键自定义内容
    @Override
    public void onClick(View v){//按钮集体定义
        switch (v.getId()){
            case R.id.controller_button_L_EN:{
                if(btn_enable.isSelected()){
                    btn_enable.setText("ENABLE");
                    btn_enable.setSelected(false);
                    mYaw = 0;
                    mPitch = 0;
                    mRoll = 0;
                    mThrottle = 0;
                    gPitch = 0;
                    mSendVirtualStickDataTimer.cancel();
                    mSendGimbalDataTimer.cancel();
                    if (mFlightController != null){
                        mFlightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null) {
                                    showToast(djiError.getDescription());
                                } else {
                                    showToast("Disable Virtual Stick Success");
                                }
                            }
                        });

                    }

                } else{
                    btn_enable.setText("DISABLE");
                    btn_enable.setSelected(true);
                    if (mFlightController != null){

                        mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null){
                                    showToast(djiError.getDescription());
                                }else
                                {
                                    showToast("Enable Virtual Stick Success");
                                }
                            }
                        });

                        //定时器用来定时，200ms发送一次数据
                        if (null == mSendVirtualStickDataTimer) {
                            showToast("redo mSendVirtualStickDataTimer");
                            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                            mSendVirtualStickDataTimer = new Timer();
                            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 100);
                        }
                        if (mSendGimbalDataTimer == null) {
                            mSendGimbalDataTimer= new Timer();
                            mSendGimbalDataTask = new SendGimbalDataTask();
                            mSendGimbalDataTimer.schedule(mSendGimbalDataTask, 100, 100);
                        }

                    }
                }

                break;
            }
            case R.id.controller_button_TAKEOFF:{

                if (mFlightController != null){
                    mFlightController.startTakeoff(
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        showToast(djiError.getDescription());
                                    } else {
                                        showToast("Take off Success");
                                    }
                                }
                            }
                    );
                }

                break;
            }

            case R.id.controller_button_LAND:{
                if (mFlightController != null){

                    mFlightController.startLanding(
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        showToast(djiError.getDescription());
                                    } else {
                                        showToast("Start Landing");
                                    }
                                }
                            }
                    );

                }
                break;
            }

            case R.id.controller_button_L_UP:
                mYaw = 0;
                mPitch = 0;
                mRoll = 0;
                mThrottle+=0.2;
                showToast("高度"+mThrottle);

                break;
            case R.id.controller_button_L_DOWN:
                mYaw = 0;
                mPitch = 0;
                mRoll = 0;
                mThrottle-=0.2;
                showToast("高度"+mThrottle);

                break;
            case R.id.controller_button_L_RIGHT:
                mYaw++;
                mPitch = 0;
                mRoll = 0;
                mThrottle = 0;
                showToast("转角"+mYaw);
                break;
            case R.id.controller_button_L_LEFT:
                mYaw--;
                mPitch = 0;
                mRoll = 0;
                mThrottle = 0;
                showToast("转角"+mYaw);
                break;
            case R.id.controller_button_R_UP:
                mYaw = 0;
                mPitch = 0;
                mRoll+=0.2;
                mThrottle = 0;
                showToast("侧倾"+mRoll);
                break;

            case R.id.controller_button_R_DOWN:
                mYaw = 0;
                mPitch = 0;
                mRoll-=0.2;
                mThrottle = 0;
                showToast("侧倾"+mRoll);
                break;

            case R.id.controller_button_R_LEFT:
                mYaw = 0;
                mPitch-=0.2;
                mRoll = 0;
                mThrottle = 0;
                showToast("前后倾"+mPitch);
                break;
            case R.id.controller_button_R_RIGHT:
                mYaw = 0;
                mPitch+=0.2;
                mRoll = 0;
                mThrottle = 0;
                showToast("前后倾"+mPitch);
                break;
                // 云台控制
            case R.id.controller_button_Camera_UP:
                gPitch++;
                showToast(""+gPitch);
                break;
            case R.id.controller_button_Camera_DOWN:
                gPitch--;
                showToast(""+gPitch);
                break;
            case R.id.controller_button_Camera_RIGHT:
                gPitch++;
                showToast(""+gPitch);
                break;
            case R.id.controller_button_Camera_LEFT:
                gPitch--;
                showToast(""+gPitch);
                break;
            default:
                showToast("Other pressed");


        }
    }

    //----------------------------------------------------------------------------------
    //展示信息
    private void showToast(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_SHOW_TOAST, s)
        );
    }




    //----------------------------------------------------------------------------------
    //元素绑定
    private Button btn_enable;
    private Button btn_L_up,btn_L_down,btn_L_right,btn_L_left;
    private Button btn_R_up,btn_R_down,btn_R_right,btn_R_left;
    private Button btn_takeoff,btn_landing;
    private void initUI() {
        btn_enable =(Button)findViewById(R.id.controller_button_L_EN);
        btn_enable.setOnClickListener(this);
        btn_enable.setSelected(false);

        btn_L_up = (Button)findViewById(R.id.controller_button_L_UP);
        btn_L_up.setOnClickListener(this);

        btn_L_down = (Button)findViewById(R.id.controller_button_L_DOWN);
        btn_L_down.setOnClickListener(this);

        btn_L_right = (Button)findViewById(R.id.controller_button_L_RIGHT);
        btn_L_right.setOnClickListener(this);

        btn_L_left = (Button)findViewById(R.id.controller_button_L_LEFT);
        btn_L_left.setOnClickListener(this);

        btn_R_left = (Button)findViewById(R.id.controller_button_R_LEFT);
        btn_R_left.setOnClickListener(this);

        btn_R_up = (Button)findViewById(R.id.controller_button_R_UP);
        btn_R_up.setOnClickListener(this);

        btn_R_down = (Button)findViewById(R.id.controller_button_R_DOWN);
        btn_R_down.setOnClickListener(this);

        btn_R_right= (Button)findViewById(R.id.controller_button_R_RIGHT);
        btn_R_right.setOnClickListener(this);

        btn_landing = (Button)findViewById(R.id.controller_button_LAND);
        btn_landing.setOnClickListener(this);

        btn_takeoff = (Button)findViewById(R.id.controller_button_TAKEOFF);
        btn_takeoff.setOnClickListener(this);

        btn_camera_up = (Button)findViewById(R.id.controller_button_Camera_UP);
        btn_camera_up.setOnClickListener(this);

        btn_camera_down = (Button)findViewById(R.id.controller_button_Camera_DOWN);
        btn_camera_down.setOnClickListener(this);

        btn_camera_left = (Button)findViewById(R.id.controller_button_Camera_LEFT);
        btn_camera_left.setOnClickListener(this);

        btn_camera_right = (Button)findViewById(R.id.controller_button_Camera_RIGHT);
        btn_camera_right.setOnClickListener(this);


    }


    //----------------------------------------------------------------------------------
    //两个传数
    class SendVirtualStickDataTask extends TimerTask {
        //作为指令格式发送给无人机
        //Pitch:前后翻动
        //Roll:左右翻动
        //Yaw:围绕无人机中心水平转动
        //Throttle:直上直下
        @Override
        public void run() {

            if (mFlightController != null) {
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        }
                );
            }
        }
    }
    class SendGimbalDataTask extends TimerTask{
        @Override
        public void run() {
            if(mGimbal!=null){
                mGimbal.rotate(new Rotation.Builder()
                                .pitch(gPitch)
                                .mode(RotationMode.ABSOLUTE_ANGLE)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(0)
                                .build(),
                        new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
            }

        }
    }
}
