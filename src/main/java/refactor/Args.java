package refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;

public class Args {

    private final Map<Character, SchemaElement> schemaElements = new HashMap<>();
    private final Map<Character, Object> parsedArgs = new HashMap<>();

    public Args(String schema, String[] args) throws ArgsException {
        parseSchema(schema);
        parseArguments(args);
    }

    private void parseSchema(String schema) throws ArgsException {
        for (String element : schema.split(",")) {
            if (!element.isEmpty()) {
                String trimmedElement = element.trim();
                char elementId = trimmedElement.charAt(0);
                if (Character.isLetter(elementId)) {
                    String elementTail = trimmedElement.substring(1);
                    schemaElements.put(elementId, SchemaElement.create(elementId, elementTail));
                } else {
                    throw new ArgsException("Bad character: %c in Args format: %s".formatted(elementId, schema));
                }
            }
        }
    }

    private void parseArguments(String[] args) throws ArgsException {
        // This is a bit hairy, but it covers extracting (argument, parameter) pairs in both the case where
        // parameters follow the arg character and cases where the args characters appear grouped together with
        // the parameters following. Examples:
        // -n 10 -b -s Foo
        // -nbs 10 Foo
        for (var argsIter = new ArrayList<>(asList(args)).iterator(); argsIter.hasNext(); ) {
            String arg = argsIter.next();
            if (arg.startsWith("-")) {
                for (char argChar : arg.substring(1).toCharArray()) {
                    var schemaElement = schemaElements.get(argChar);
                    if (schemaElement != null) {
                        if (schemaElement.requiresParameter()) {
                            try {
                                parsedArgs.put(argChar, schemaElement.parse(argsIter.next()));
                            } catch (NoSuchElementException e) {
                                throw new ArgsException("Could not find parameter for -%c.".formatted(argChar), e);
                            }
                        } else {
                            parsedArgs.put(argChar, schemaElement.parse(""));
                        }
                    } else {
                        throw new ArgsException("Argument -%c unexpected".formatted(argChar));
                    }
                }
            }
        }
    }

    public int cardinality() {
        return parsedArgs.size();
    }

    public String getString(char arg) {
        return (String) parsedArgs.getOrDefault(arg, "");
    }

    public int getInt(char arg) {
        return (Integer) parsedArgs.getOrDefault(arg, 0);
    }

    public boolean getBoolean(char arg) {
        return (Boolean) parsedArgs.getOrDefault(arg, false);
    }

    public boolean has(char arg) {
        return parsedArgs.containsKey(arg);
    }
}