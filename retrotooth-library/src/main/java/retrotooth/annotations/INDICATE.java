package retrotooth.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Read data from the characteristic matching the characteristic.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface INDICATE {
    String service();
    String characteristic();
}
