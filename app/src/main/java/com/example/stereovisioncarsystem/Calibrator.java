package com.example.stereovisioncarsystem;

public abstract class Calibrator
{
    abstract public void performUndisortion() throws SingleCameraCalibrator.ChessboardsNotOnAllPhotosException, SingleCameraCalibrator.NotEnoughChessboardsException;

    public class ChessboardsNotOnAllPhotosException extends Exception
    {

    }

    public class NotEnoughChessboardsException extends Exception
    {

    }
}
