package org.companyos.dev.cos_common;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tianshuo on 16/7/13.
 *
 */
public class COSReturnTest {
    @Test
    public void tomIsOk() throws Exception {
        COSReturn<Integer> v_success = COSReturn.success(3);
        assertEquals(v_success.isOk(), true);

        COSReturn<Integer> v_error_1 = COSReturn.error("error message");
        assertEquals(v_error_1.isOk(), false);

        COSReturn<Integer> v_error_2 = COSReturn.error();
        assertEquals(v_error_2.isOk(), false);
    }


    @Test
    public void tsm_success() throws Exception {
        COSReturn<Integer> v_int_1 = COSReturn.success();
        v_int_1.setR(3);
        assertEquals(v_int_1.getR().intValue(), 3);

        COSReturn<Integer> v_int_2 = COSReturn.success(3);
        assertEquals(v_int_2.getR().intValue(), 3);

        COSReturn<Integer> v_int_3 = COSReturn.success(-3);
        assertEquals(v_int_3.getR().intValue(), -3);

        COSReturn<Boolean> v_boolean_1 = COSReturn.success(false);
        assertEquals(v_boolean_1.getR(), false);


        COSReturn<Boolean> v_mix = COSReturn.<Boolean>success().setR(true).setM("ok").setD("debug");
        assertEquals(v_mix.getR(), true);
    }

}