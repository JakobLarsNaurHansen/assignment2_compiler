import java.util.HashMap;
import java.util.Map.Entry;

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
            System.err.println("Variable is undefined or incorrectly used: " + name);
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

