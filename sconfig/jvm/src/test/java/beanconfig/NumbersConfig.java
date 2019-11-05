package beanconfig;

// Java object returns are fully specified and should be reverted
// in the future. See https://github.com/lampepfl/dotty/issues/7312
public class NumbersConfig {


    private int intVal;
    private Integer intObj;
    private long longVal;
    private Long longObj;
    private double doubleVal;
    private Double doubleObj;


    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public Integer getIntObj() {
        return intObj;
    }

    public void setIntObj(Integer intObj) {
        this.intObj = intObj;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public java.lang.Long getLongObj() { return longObj; }

    public void setLongObj(Long longObj) {
        this.longObj = longObj;
    }

    public double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public java.lang.Double getDoubleObj() {
        return doubleObj;
    }

    public void setDoubleObj(Double doubleObj) {
        this.doubleObj = doubleObj;
    }
}
