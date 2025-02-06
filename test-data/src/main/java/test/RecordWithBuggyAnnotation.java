package test;

import java.util.List;

public record RecordWithBuggyAnnotation(List<@Nullable String> list) {
    // if this compact constructor is present, the type annotation on the constructor has a wrong type target
    // the bug is gone when this compact constructor is removed
    public RecordWithBuggyAnnotation {
    }
}
