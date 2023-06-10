package refactor;

final class StringSchemaElement extends SchemaElement {

    static final String TOKEN = "*";

    StringSchemaElement(char argCharacter) {
        super(argCharacter);
    }

    @Override
    String parse(String parameter) {
        return parameter;
    }
}
