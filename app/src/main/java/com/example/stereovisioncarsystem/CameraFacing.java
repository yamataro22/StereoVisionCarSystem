package com.example.stereovisioncarsystem;

public enum CameraFacing
{
    Front("Front"),
    Back("Back");

    String facing;

    CameraFacing(String facing) {
        this.facing = facing;
    }

    public static CameraFacing getCameraFacing(String camera)
    {
        return camera.equals("Front") ? CameraFacing.Front : CameraFacing.Back;
    }
}
