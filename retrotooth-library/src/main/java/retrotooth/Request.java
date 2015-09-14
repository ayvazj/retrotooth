package retrotooth;


import java.util.UUID;

public class Request {
    private final UUID characteristicUuid;
    private final UUID serviceUuid;
    private final BluetoothOperation bluetoothOperation;
    private final Object tag;

    private Request(Request.Builder builder) {
        this.characteristicUuid = builder.characteristicUuid;
        this.serviceUuid = builder.serviceUuid;
        this.bluetoothOperation = builder.bluetoothOperation;
        this.tag = builder.tag != null ? builder.tag : this;
    }

    public UUID characteristic() {
        return this.characteristicUuid;
    }

    public UUID service() {
        return this.serviceUuid;
    }

    public BluetoothOperation bluetoothOperation() {
        return this.bluetoothOperation;
    }

    public Object tag() {
        return this.tag;
    }

    public String toString() {
        return "Request{service=" + this.serviceUuid + ", characteristic=" + this.characteristicUuid + ", bluetoothOperation=" + this.bluetoothOperation + ", tag=" + (this.tag != this ? this.tag : null) + '}';
    }

    public static class Builder {
        private UUID serviceUuid;
        private UUID characteristicUuid;
        private BluetoothOperation bluetoothOperation;
        private Object tag;

        public Builder() {
        }

        private Builder(Request request) {
            this.serviceUuid = request.serviceUuid;
            this.characteristicUuid = request.characteristicUuid;
            this.bluetoothOperation = request.bluetoothOperation;
            this.tag = request.tag;
        }

        public Request.Builder characteristic(UUID uuid) {
            this.characteristicUuid = uuid;
            return this;
        }

        public Request.Builder bluetoothOperation(BluetoothOperation bluetoothOperation) {
            this.bluetoothOperation = bluetoothOperation;
            return this;
        }

        public Request.Builder service(UUID uuid) {
            this.serviceUuid = uuid;
            return this;
        }

        public Request build() {
            if (this.serviceUuid == null) {
                throw new IllegalStateException("service == null");
            }

            if (this.characteristicUuid == null) {
                throw new IllegalStateException("characteristic == null");
            }

            if (this.bluetoothOperation == null) {
                throw new IllegalStateException("bluetoothOperation == null");
            }

            return new Request(this);
        }
    }
}
