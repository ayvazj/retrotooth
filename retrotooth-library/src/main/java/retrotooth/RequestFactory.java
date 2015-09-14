package retrotooth;

import java.util.UUID;

final class RequestFactory {
    private final UUID serviceUuid;
    private final UUID characteristicUuid;
    private final BluetoothOperation bluetoothOperation;
    private final boolean hasBody;
//    private final RequestBuilderAction[] requestBuilderActions;

    RequestFactory(UUID serviceUuid, UUID characteristicUuid, BluetoothOperation bluetoothOperation, boolean hasBody,
                   RequestBuilderAction[] requestBuilderActions) {
        this.serviceUuid = serviceUuid;
        this.bluetoothOperation = bluetoothOperation;
        this.characteristicUuid = characteristicUuid;
        this.hasBody = hasBody;
//        this.requestBuilderActions = requestBuilderActions;
    }

    Request create(Object... args) {
        RequestBuilder requestBuilder =
                new RequestBuilder(this.serviceUuid, this.characteristicUuid, this.bluetoothOperation, this.hasBody);

        if (args != null) {
//            RequestBuilderAction[] actions = requestBuilderActions;
//            if (actions.length != args.length) {
//                throw new IllegalArgumentException("Argument count ("
//                        + args.length
//                        + ") doesn't match action count ("
//                        + actions.length
//                        + ")");
//            }
//            for (int i = 0, count = args.length; i < count; i++) {
//                actions[i].perform(requestBuilder, args[i]);
//            }
        }

        return requestBuilder.build();
    }
}
