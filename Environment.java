import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

class Environment {
    HashMap<String, Boolean> values = new HashMap<>();

    public Environment() {
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

    public String toString() {
        String table = "";
        for (Entry<String, Boolean> entry : values.entrySet()) {
            table += entry.getKey() + " &rarr; " + entry.getValue() + "<br>\n";
        }
        return table;
    }
}

