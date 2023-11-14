import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

class Environment {
    private HashMap<String, Boolean> values = new HashMap<>();

    public Environment() {
    }

    private Environment(Environment env) {
        this.values = new HashMap<>(env.values);
    }

    public void setVariable(String name, Boolean value) {
        values.put(name, value);
    }

    public Boolean getVariable(String name) {
        Boolean value = values.get(name);
        if (value == null) {
            System.err.println("Variable not defined: " + name);
            System.exit(-1);
        }
        return value;
    }

    public Boolean hasVariable(String name) {
        Boolean v = values.get(name);
        return (v != null);
    }

    /**
     * @return we return a shallow copy, and since it only contains primitive values it is the same as a deep copy.
     * We return a copy because we only use this for displaying the history.
     */
    Environment cloneThis() {
        return new Environment(this);
    }

    public String toString() {
        String table = "";
        for (Entry<String, Boolean> entry : values.entrySet()) {
            table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
        }
        return table;
    }

    public static String prettyString(List<Environment> envs, int cycles) {
        StringBuilder result = new StringBuilder();
        var first = envs.get(0);
        // loop through keys
        for (Entry<String, Boolean> entry : first.values.entrySet()) {
            for (int i = 0; i < cycles; i++) {
                var env = envs.get(i);
                var value = env.values.get(entry.getKey());
                if (value == null) {
                    result.append("X");
                } else {
                    result.append(value ? "1" : "0");
                }
            }
            result.append(' ');
            result.append(entry.getKey());
            result.append('\n');

        }
        return result.toString();
    }
}

