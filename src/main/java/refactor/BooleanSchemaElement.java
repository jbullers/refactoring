package refactor;

final class BooleanSchemaElement extends SchemaElement {

    static final String TOKEN = "";

    BooleanSchemaElement(char argCharacter) {
        super(argCharacter);
    }

    @Override
    boolean requiresParameter() {
        return false;
    }

    @Override
    Boolean parse(String parameter) {
        return true;
    }
}
