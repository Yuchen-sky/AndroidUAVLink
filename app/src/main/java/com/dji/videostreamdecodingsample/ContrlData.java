package com.dji.videostreamdecodingsample;
//----------------------------------------------------------------------------------
//控制信息类
public class ContrlData {
    private float gPitch;
    private float gRoll;
    private float gYaw;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;

    public void setgPitch(float gPitch) {
        this.gPitch = gPitch;
    }

    public void setgRoll(float gRoll) {
        this.gRoll = gRoll;
    }

    public void setgYaw(float gYaw) {
        this.gYaw = gYaw;
    }

    public void setmPitch(float mPitch) {
        this.mPitch = mPitch;
    }

    public void setmRoll(float mRoll) {
        this.mRoll = mRoll;
    }

    public void setmYaw(float mYaw) {
        this.mYaw = mYaw;
    }

    public void setmThrottle(float mThrottle) {
        this.mThrottle = mThrottle;
    }

    public float getgPitch() {
        return gPitch;
    }

    public float getgRoll() {
        return gRoll;
    }

    public float getgYaw() {
        return gYaw;
    }

    public float getmPitch() {
        return mPitch;
    }

    public float getmRoll() {
        return mRoll;
    }

    public float getmYaw() {
        return mYaw;
    }

    public float getmThrottle() {
        return mThrottle;
    }

    public ContrlData(float gPitch, float gRoll, float gYaw, float mPitch, float mRoll, float mYaw, float mThrottle) {
        this.gPitch = gPitch;
        this.gRoll = gRoll;
        this.gYaw = gYaw;
        this.mPitch = mPitch;
        this.mRoll = mRoll;
        this.mYaw = mYaw;
        this.mThrottle = mThrottle;
    }
}
