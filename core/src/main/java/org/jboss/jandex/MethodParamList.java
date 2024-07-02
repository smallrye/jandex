package org.jboss.jandex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// only used during class indexing to remember:
// - parameter names
// - information about synthetic/mandated parameters
final class MethodParamList {
    private final MethodInfo method;

    // from the `MethodParameters` attribute
    private final List<ParamData> proper = new ArrayList<>();
    // from the local variable table
    private final List<ParamData> debug = new ArrayList<>();

    // these variables are initialized in `finish`
    private ParamData[] possiblyNamed;
    private boolean firstIsEnclosingInstance;

    MethodParamList(MethodInfo method) {
        this.method = method;
    }

    void appendProper(byte[] parameterName, boolean synthetic) {
        proper.add(new ParamData(parameterName, synthetic));
    }

    void appendDebug(byte[] parameterName, boolean synthetic) {
        debug.add(new ParamData(parameterName, synthetic));
    }

    void finish() {
        List<ParamData> proper = this.proper;
        List<ParamData> debug = this.debug;

        if (proper.isEmpty() && debug.isEmpty()) {
            possiblyNamed = ParamData.EMPTY_ARRAY;
            return;
        }

        // parameters from the local variable table are always:
        // - optionally, single synth param which is the enclosing instance
        // - then, all params are possibly named
        // (see also `Indexer.processLocalVariableTable`)
        if (proper.isEmpty()) {
            List<ParamData> list = debug;
            if (debug.get(0).syntheticOrMandated) {
                list = debug.subList(1, debug.size());
                firstIsEnclosingInstance = true;
            }
            possiblyNamed = list.toArray(ParamData.EMPTY_ARRAY);
            return;
        }

        // need to synchronize the shapes of `proper` and `debug`
        // (fortunately, there are very few situations when they are out of sync)
        if (method.isConstructor()) {
            if (method.declaringClass().isRecord()) {
                // keep record compact constructor parameters intact
            } else if (method.declaringClass().isEnum()) {
                // remove the first 2 synth params of an enum constructor
                proper = proper.subList(2, method.parametersCount());
            } else {
                // `method.declaringClass().nestingType()` is not necessarily correct at the moment,
                // so we can't use it to detect if this constructor possibly belongs to an inner class
                //
                // in any case, we need to:
                // 1. remember if there's an enclosing instance synth param at the beginning
                // 2. remove all synth params
                if (proper.get(0).syntheticOrMandated && !debug.isEmpty() && debug.get(0).syntheticOrMandated) {
                    debug.remove(0);
                    firstIsEnclosingInstance = true;
                }
                for (Iterator<ParamData> it = proper.iterator(); it.hasNext();) {
                    ParamData param = it.next();
                    if (param.syntheticOrMandated) {
                        it.remove();
                    }
                }
            }
        }

        // if there's no local variable table, the adjusted `proper` list is the best we've got
        if (debug.isEmpty()) {
            possiblyNamed = proper.toArray(ParamData.EMPTY_ARRAY);
            return;
        }

        // `proper` should have the same shape as `debug` now, just being extra careful here
        int size = Math.min(proper.size(), debug.size());
        ParamData[] result = new ParamData[size];
        for (int i = 0; i < size; i++) {
            ParamData properParameter = proper.get(i);
            ParamData debugParameter = debug.get(i);
            ParamData resultParameter = new ParamData(
                    properParameter.name != null ? properParameter.name : debugParameter.name,
                    properParameter.syntheticOrMandated || debugParameter.syntheticOrMandated);
            result[i] = resultParameter;
        }
        possiblyNamed = result;
    }

    boolean firstIsEnclosingInstance() {
        return firstIsEnclosingInstance;
    }

    byte[][] getNames() {
        ParamData[] params = possiblyNamed;

        // iterate backwards to ignore parameters at the end that don't have a name
        int count = 0;
        boolean seenNamed = false;
        for (int i = params.length - 1; i >= 0; i--) {
            if (!seenNamed && params[i].name == null) {
                continue;
            }

            seenNamed = true;
            count++;
        }
        if (count == 0) {
            return null;
        }

        // copy parameter names into the result array
        byte[][] result = new byte[count][];
        int pos = 0;
        for (int i = 0; i < params.length; i++) {
            result[pos] = params[i].name;
            pos++;
            if (pos >= result.length) { // shouldn't happen, just extra care
                break;
            }
        }

        // many places in Jandex assume that list of parameter names:
        // - may be shorter than the full list of parameters (that is, it may be a prefix)
        // - doesn't contain any unknown (`null`) values
        for (byte[] name : result) {
            if (name == null) {
                // if there actually is a `null` value, we rather pretend
                // that we don't have parameter names at all
                return null;
            }
        }
        return result;
    }

    static final class ParamData {
        static final ParamData[] EMPTY_ARRAY = new ParamData[0];

        final byte[] name;
        final boolean syntheticOrMandated;

        ParamData(byte[] name, boolean syntheticOrMandated) {
            this.name = name;
            this.syntheticOrMandated = syntheticOrMandated;
        }

        // only for debugging, not supposed to be used otherwise
        @Override
        public String toString() {
            return (name == null ? "<null>" : Utils.fromUTF8(name)) + (syntheticOrMandated ? "<synth>" : "");
        }
    }
}
