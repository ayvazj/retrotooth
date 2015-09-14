package retrotooth;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;


class Platform {
    private static final Platform PLATFORM = findPlatform();

    static Platform get() {
        return PLATFORM;
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return new Android();
            }
        } catch (ClassNotFoundException ignored) {
        }

        return new Platform();
    }

    CallAdapter.Factory defaultCallAdapterFactory(Executor callbackExecutor) {
        throw new UnsupportedOperationException("not implemented");
    }


    /**
     * Provides sane defaults for operation on Android.
     */
    static class Android extends Platform {
        @Override
        CallAdapter.Factory defaultCallAdapterFactory(Executor callbackExecutor) {
            if (callbackExecutor == null) {
                callbackExecutor = new MainThreadExecutor();
            }
            return new ExecutorCallAdapterFactory(callbackExecutor);
        }

        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable r) {
                handler.post(r);
            }

            @Override
            public String toString() {
                return "MainThreadExecutor";
            }
        }
    }
}

