package fi.thl.pivot.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PivotCellImpl implements PivotCell {

    private final Pattern NUMBER = Pattern.compile("^-?\\d*([,.]\\d+)?$");
    
    private String value;
    private int rowNumber;
    private int columnNumber;
    private DimensionNode measure;
    private int[] key;
    private List<List<Integer>> indices;
    private long hashKey;
    private String ciLower;
    private String ciUpper;
    private String sampleSize;
    private int[] fullKey;

    public PivotCellImpl(String value) {
        this.value = value;
    }

    public String getValue() {
        if (isNumber() && null != measure) {
            NumberFormat nf = new DecimalFormat("0.#");
            nf.setMaximumFractionDigits(measure.getDecimals());
            nf.setRoundingMode(RoundingMode.HALF_UP);
            return nf.format(getNumberValue());
        }
        return value;
    }

    public String getI18nValue() {
        if (isNumber() && null != measure) {
            NumberFormat nf = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(new Locale("fi")));
            nf.setMaximumFractionDigits(measure.getDecimals());
            nf.setRoundingMode(RoundingMode.HALF_UP);
            return nf.format(getNumberValue());
        }
        return value;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public boolean isNumber() {
        return null != value && NUMBER.matcher(value).matches();
    }

    @Override
    public double getNumberValue() {
        return Double.parseDouble(value.replaceAll(",", "."));
    }

    public void setMeasure(DimensionNode measure) {
        this.measure = measure;
    }

    @Override
    public DimensionNode getMeasure() {
        return measure;
    }

    @Override
    public int compareTo(PivotCell o) {
        if (null == value) {
            return -1;
        }
        if (isNumber() && !o.isNumber()) {
            return 1;
        }
        if (isNumber()) {
            return new Double(getNumberValue()).compareTo(o.getNumberValue());
        }
        if (o.isNumber()) {
            return -1;
        }
        return value.compareTo(o.getValue());
    }

    public void setValue(String string) {
        value = string;
    }

    public void setKey(int[] key) {
        this.key = key;
    }

    public void setFullKey(int[] fullKey) {
        this.fullKey = fullKey;;
    }
    
    public int[] getKey() {
        return key;
    }

    public int getPosition() {
        int position = 0;
        for (int i = 0; i < fullKey.length; ++i) {
            int factor = 1;
            int index = indices.get(i).indexOf(fullKey[i]);
            if (index == 0) {
                continue;
            }
            for (int j = i + 1; j < indices.size(); ++j) {
                factor *= indices.get(j).size();
            }
            position += index * factor;
        }
        return position;
    }

    public void setIndices(List<List<Integer>> indices) {
        this.indices = indices;
    }

    public void setCode(long hashKey) {
        this.hashKey = hashKey;
    }

    public long getHashKey() {
        return hashKey;
    }

    public void setConfidenceLowerLimit(String value) {
        this.ciLower = value;
    }

    public void setConfidenceUpperLimit(String value) {
        this.ciUpper = value;
    }

    public void setSampleSize(String value) {
        this.sampleSize = value;
    }

    public String getConfidenceLowerLimit() {
        return ciLower;
    }

    public String getConfidenceUpperLimit() {
        return ciUpper;
    }

    public String getSampleSize() {
        return sampleSize;
    }

    @Override
    public String toString() {
        return "PivotCellImpl [value=" + value + ", rowNumber=" + rowNumber + ", columnNumber=" + columnNumber + ", ciLower=" + ciLower + ", ciUpper=" + ciUpper
                + ", sampleSize=" + sampleSize + "]";
    }

 

}
