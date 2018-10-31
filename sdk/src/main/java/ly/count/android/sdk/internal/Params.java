package ly.count.android.sdk.internal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.count.sdk.internal.JSONable;

/**
 * Object for application/x-www-form-urlencoded string building and manipulation
 */

class Params {
    static final String PARAM_DEVICE_ID = "device_id";
    static final String PARAM_OLD_DEVICE_ID = "old_device_id";

    private static final Log.Module L = Log.module("Params");

    private StringBuilder params;

    static final class Obj {
        private final String key;
        private final JSONObject json;
        private final Params params;

        Obj(String key, Params params) {
            this.params = params;
            this.key = key;
            this.json = new JSONObject();
        }

        public Obj put(String key, Object value) {
            try {
                json.put(key, value);
            } catch (JSONException e) {
                L.wtf("Cannot put property into Params.Obj", e);
            }
            return this;
        }

        public Params add(){
            params.add(key, json.toString());
            return params;
        }
    }

    static final class Arr {
        private final String key;
        private final Collection<String> json;
        private final Params params;

        Arr(String key, Params params) {
            this.params = params;
            this.key = key;
            this.json = new ArrayList<>();
        }

        public Arr put(JSONable value) {
            json.add(value.toJSON());
            return this;
        }

        public Arr put(Collection collection) {
            for (Object value : collection) if (value instanceof JSONable) {
                json.add(((JSONable)value).toJSON());
            }
            return this;
        }

        public Params add() {
            if (json.size() > 0) {
                params.add(key, "[" + Utils.join(json, ",") + "]");
            } else {
                params.add(key, "[]");
            }
            return params;
        }
    }

    Params(Object... objects) {
        params = new StringBuilder();
        if (objects != null && objects.length == 1 && (objects[0] instanceof Object[])) {
            addObjects((Object[]) objects[0]);
        } else if (objects != null && objects.length == 1 && (objects[0] instanceof Params)) {
            params.append(objects[0].toString());
        } else if (objects != null && objects.length == 1 && (objects[0] instanceof String)) {
            params.append(objects[0].toString());
        } else {
            addObjects(objects);
        }
    }

    Params(String params) {
        this.params = new StringBuilder(params);
    }

    Params() {
        this.params = new StringBuilder();
    }

    Params add(Object... objects) {
        return addObjects(objects);
    }

    Params add(String key, Object value) {
        if (params.length() > 0) {
            params.append("&");
        }
        params.append(key).append("=");
        if (value != null) {
            params.append(Utils.urlencode(value.toString()));
        }
        return this;
    }

    Params add(Params params) {
        if (params == null || params.length() == 0) {
            return this;
        }
        if (this.params.length() > 0) {
            this.params.append("&");
        }
        this.params.append(params.toString());
        return this;
    }

    Params add(String string) {
        if (params != null) {
            this.params.append(string);
        }
        return this;
    }

    Obj obj(String key) {
       return new Obj(key, this);
    }

    Arr arr(String key) {
       return new Arr(key, this);
    }

    String remove(String key) {
        List<String> pairs = new ArrayList<>(Arrays.asList(params.toString().split("&")));
        for (String pair : pairs) {
            String comps[] = pair.split("=");
            if (comps.length == 2 && comps[0].equals(key)) {
                pairs.remove(pair);
                this.params = new StringBuilder(Utils.join(pairs, "&"));
                return Utils.urldecode(comps[1]);
            }
        }
        return null;
    }

    Map<String, String> map() {
        Map<String, String> map = new HashMap<>();
        List<String> pairs = new ArrayList<>(Arrays.asList(params.toString().split("&")));
        for (String pair : pairs) {
            String comps[] = pair.split("=");
            if (comps.length == 2) {
                map.put(comps[0], Utils.urldecode(comps[1]));
            }
        }
        return map;
    }

    String get(String key) {
        if (!has(key)) {
            return null;
        }
        String[] pairs = params.toString().split("&");
        for (String pair : pairs) {
            String comps[] = pair.split("=");
            if (comps.length == 2 && comps[0].equals(key)) {
                return Utils.urldecode(comps[1]);
            }
        }
        return null;
    }

    boolean has(String key) {
        return params.indexOf("&" + key + "=") != -1 || params.indexOf(key + "=") == 0;
    }

    private Params addObjects(Object[] objects) {
        if (objects.length % 2 != 0) {
            L.wtf("Bad number of parameters");
        } else {
            for (int i = 0; i < objects.length; i += 2) {
                add(objects[i] == null ? ("unknown" + i) : objects[i].toString(), objects.length > i + 1 ? objects[i + 1] : null);
            }
        }
        return this;
    }

    int length() {
        return params.length();
    }

    void clear() {
        params = new StringBuilder();
    }

    public String toString(){
        return params.toString();
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Params)) {
            return false;
        }
        Params p = (Params)obj;

        return p.params.toString().equals(params.toString());
    }
}