package com.devproject.dpinUptime.DTO;
import com.devproject.dpinUptime.model.UrlStatus; // Adjust the package path if necessary

import java.util.Objects;
public class StatusStat {
    private final UrlStatus status;
    private final String label;
    private final long count;

    public StatusStat(UrlStatus status, String label, long count) {
        this.status = status;
        this.label = label;
        this.count = count;
    }
    public UrlStatus getStatus() {
        return status;
    }

    public String getLabel() {
        return label;
    }

    public long getCount() {
        return count;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusStat that = (StatusStat) o;
        return count == that.count &&
                Objects.equals(status, that.status) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, label, count);
    }

    // toString()
    @Override
    public String toString() {
        return "StatusStat{" +
                "status='" + status + '\'' +
                ", label='" + label + '\'' +
                ", count=" + count +
                '}';
    }
}

