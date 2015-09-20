package com.example.android.bluetoothlegatt;


import retrotooth.Call;
import retrotooth.ResponseData;
import retrotooth.annotations.NOTIFY;
import retrotooth.annotations.READ;
import retrotooth.annotations.WRITE;

public interface HeartRateService {
    @READ(service = "180d", characteristic = "2a38")
    Call<ResponseData> getBodySensorLocation();

    @NOTIFY(service = "180d", characteristic = "2a37")
    Call<String> getHeartRateMeasurement();

    @WRITE(service = "180d", characteristic = "2a39")
    Call<Void> setHeartRateControlPoint();
}
