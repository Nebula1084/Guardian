package hku.cs.smp.guardian.common;

import io.reactivex.Flowable;
import org.junit.Test;

public class RxTest {

    @Test
    public void test() {
        Flowable.just("Hello World").subscribe(System.out::println);

    }
}
