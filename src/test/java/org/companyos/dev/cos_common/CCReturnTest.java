package org.companyos.dev.cos_common;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tianshuo on 16/7/13.
 *
 */
public class CCReturnTest {
    @Test
    public void tomIsOk() throws Exception {
        CCReturn<Integer> v_success = CCReturn.success(3);
        assertEquals(v_success.isSuccess(), true);

        CCReturn<Integer> v_error_1 = CCReturn.error("error message");
        assertEquals(v_error_1.isSuccess(), false);

        CCReturn<Integer> v_error_2 = CCReturn.error();
        assertEquals(v_error_2.isSuccess(), false);
    }


    @Test
    public void tsm_success() throws Exception {
        CCReturn<Integer> v_int_1 = CCReturn.success();
        v_int_1.setV(3);
        assertEquals(v_int_1.getV().intValue(), 3);

        CCReturn<Integer> v_int_2 = CCReturn.success(3);
        assertEquals(v_int_2.getV().intValue(), 3);

        CCReturn<Integer> v_int_3 = CCReturn.success(-3);
        assertEquals(v_int_3.getV().intValue(), -3);

        CCReturn<Boolean> v_boolean_1 = CCReturn.success(false);
        assertEquals(v_boolean_1.getV(), false);


        CCReturn<Boolean> v_mix = CCReturn.<Boolean>success().setV(true).setM("ok");
        assertEquals(v_mix.getV(), true);
    }

}