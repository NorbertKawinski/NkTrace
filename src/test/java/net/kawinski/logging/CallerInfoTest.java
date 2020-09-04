package net.kawinski.logging;

import org.junit.Test;
import org.slf4j.event.Level;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CallerInfoTest {

    private CallerInfo deep1() {
        return deep1Impl();
    }

    private CallerInfo deep1Impl() {
        return CallerInfo.getCaller();
    }

    @Test
    public void getCaller_should_return_caller() {
        final CallerInfo callerInfo = deep1();
        assertThat(callerInfo.toString(), is("CallerInfoTest.deep1:12"));
    }

    private static class InnerClass1 {
        private CallerInfo innerMethod() {
            return new InnerClass2().innerMethod1();
        }
    }

    private static class InnerClass2 {
        private CallerInfo innerMethod1() {
            return innerMethod2();
        }

        private CallerInfo innerMethod2() {
            final String fqcn = getClass().getName();
            return CallerInfo.getCaller(fqcn);
        }
    }

    @Test
    public void getCaller_with_FQCN_should_return_caller_after_FQCN() {
        final CallerInfo callerInfo = new InnerClass1().innerMethod();
        assertThat(callerInfo.toString(), is("CallerInfoTest$InnerClass1.innerMethod:27"));
    }

    @Test
    public void getCaller_with_absent_FQCN_should_return_unknown_caller() {
        final CallerInfo callerInfo = CallerInfo.getCaller("absent.fqcn.that.does.not.Exist");
        assertThat(callerInfo, is(CallerInfo.UNKNOWN));
    }
}
