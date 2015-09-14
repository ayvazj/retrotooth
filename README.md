Retrotooth
========

Simplified Bluetooth LE communications inspired by Square's [Retrofit library][1].


Usage
-----


Define an interface for each service and characteristic you are interested in:

    public interface HeartRateService {
        @READ(service = "180d", characteristic = "2a38")
        Call<String> getBodySensorLocation();

        @NOTIFY(service = "180d", characteristic = "2a37")
        Call<String> getHeartRateMeasurement();

        @WRITE(service = "180d", characteristic = "2a39")
        Call<Void> setHeartRateControlPoint();
    }


Create an adapter and start using BLE

    Retrotooth retrotooth = new Retrotooth.Builder()
        .with(this)
        .device(bluetoothDevice)
        .build();

    HeartRateService heartRateService = retrotooth.create(HeartRateService.class);
    Call<String> call = heartRateService.getBodySensorLocation();
    call.enqueue(new Callback<String>() {
        @Override
        public void onResponse(Response<String> response) {
            Toast.makeText(DeviceControlActivity.this, "onResponse", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Throwable t) {
            Toast.makeText(DeviceControlActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
        }
     });

 [1]: http://square.github.io/retrofit/
