package fi.thl.summary.model;

public class MeasureItem {

    public static enum Type {
        REFERENCE, LABEL
    }

    private final Type type;
    private final String code;
    private Integer surrogateId;

    public MeasureItem(Type type, String code) {
        this.type = type;
        this.code = code;
    }

    public Type getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

	public Integer getSurrogateId() {
		return surrogateId;
	}

	public void setSurrogateId(Integer surrogateId) {
		this.surrogateId = surrogateId;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MeasureItem other = (MeasureItem) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MeasureItem [type=" + type + ", code=" + code + "]";
    }

}
