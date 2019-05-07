package com.example.stereovisioncarsystem.Calibrators;


public abstract class Calibrator
{
    abstract public void performUndisortion() throws SingleCameraCalibrator.ChessboardsNotOnAllPhotosException, SingleCameraCalibrator.NotEnoughChessboardsException;

    public class ChessboardsNotOnAllPhotosException extends Exception
    {

    }

    public class NotEnoughChessboardsException extends Exception
    {

    }

    public class CalibFailed extends Exception
    {

    }
}
