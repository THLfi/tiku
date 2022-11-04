package fi.thl.pivot.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class IntegerListPackerTest {

    private IntegerListPacker ilp = new IntegerListPacker();

    @Test
    public void shouldRepresentIntegerListAsBase64EncodedListOfDifferences() {
        assertEquals("12345", ilp.pack(Lists.newArrayList(1, 3, 6, 10, 15)));
    }

    @Test
    public void shouldRepresentRepeatingDifferencesAsAMultiplication() {
        assertEquals(";5.1", ilp.pack(Lists.newArrayList(1, 2, 3, 4, 5)));
    }

    @Test
    public void shouldUseSeparatorsBetweenTokensLongerThanOneCharacter() {
        assertEquals("2;3.1;10_1",
                ilp.pack(Lists.newArrayList(2, 3, 4, 5, 69, 70)));
    }

    @Test
    public void shouldWorkAsInverseA() {
        shouldWorkAsInverse(Lists.newArrayList(1, 3, 6, 10, 15));
    }

    @Test
    public void shouldWorkAsInverseB() {
        shouldWorkAsInverse(Lists.newArrayList(1, 2, 3, 4, 5));
    }

    @Test
    public void shouldWorkAsInverseC() {
        shouldWorkAsInverse(Lists.newArrayList(2, 3, 4, 5, 69, 70));
    }

    @Test
    public void shouldSupportZlib() {
        List<Integer> list = Lists.newArrayList(2, 3, 4, 5, 69, 70);
        assertEquals(list, ilp.unzipAndUnpack(ilp.packAndZip(list)));

    }

    private void shouldWorkAsInverse(List<Integer> list) {
        assertEquals(list, ilp.unpack(ilp.pack(list)));
    }

}
