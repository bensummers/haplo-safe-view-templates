package org.haplo.template.html;

class NodeTag extends Node {
    private String name;
    private String start;
    private Attribute attributesHead;

    public NodeTag(String name) {
        // TODO: store both name and 'start'?
        this.name = name;
        this.start = "<"+name;
    }

    public boolean allowedInURLContext() {
        return false;   // caught by Context.TEXT check as well
    }

    public String getName() {
        return this.name;
    }

    public void addAttribute(String attributeName, Node value, Context valueContext) {
        if(value instanceof NodeLiteral) {
            // Value is just a literal string, so can be optimised
            // Literal values should not be escaped, because the author is trusted
            // TODO: Linter should check literal values don't contain bad things
            String attributeValue = ((NodeLiteral)value).getLiteralString();
            if(canOmitQuotesForValue(attributeValue)) {
                this.start += " "+attributeName+"="+attributeValue;
            } else {
                this.start += " "+attributeName+"=\""+attributeValue+'"';
            }
            return;
        }
        Attribute attribute = new Attribute();
        attribute.name = attributeName;
        attribute.preparedNameEquals = " "+attributeName+"=\"";
        attribute.value = value;
        // If a URL, and the value is a single NodeValue element, it has to output as a URL path
        if((valueContext == Context.URL) && (value instanceof NodeValue)) {
            valueContext = Context.URL_PATH;
        }
        attribute.valueContext = valueContext;
        // Add to list
        Attribute tail = this.attributesHead;
        while(tail != null) {
            if(tail.nextAttribute == null) { break; }
            tail = tail.nextAttribute;
        }
        if(tail == null) {
            this.attributesHead = attribute;
        } else {
            tail.nextAttribute = attribute;
        }
    }

    private boolean canOmitQuotesForValue(CharSequence value) {
        int len = value.length();
        for(int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if(!(
                ((c >= 'a') && (c <= 'z')) ||
                ((c >= 'A') && (c <= 'Z')) ||
                ((c >= '0' && (c <= '9')))
            )) { return false; }
        }
        return true;
    }

    private static class Attribute {
        public Attribute nextAttribute;
        public String name;
        public String preparedNameEquals; // " name=\"" for rendering
        public Node value;
        public Context valueContext;
    }

    protected Node orSimplifiedNode() {
        if(this.attributesHead == null) {
            return new NodeLiteral(this.start+">");
        }
        return this;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        builder.append(this.start);
        Attribute attribute = this.attributesHead;
        while(attribute != null) {
            int attributeStart = builder.length();
            builder.append(attribute.preparedNameEquals);
            int valueStart = builder.length();
            attribute.value.render(builder, driver, view, attribute.valueContext);
            // If nothing was rendered, remove the attribute
            if(valueStart == builder.length()) {
                builder.setLength(attributeStart);
            } else {
                builder.append('"');
            }
            attribute = attribute.nextAttribute;
        }
        builder.append('>');
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("TAG ").append(this.start);
        if(this.attributesHead == null) {
            builder.append(">\n");  // although this case should be simplified to a literal
        } else {
            int count = 0;
            StringBuilder attributesBuilder = new StringBuilder(256);
            Attribute attribute = this.attributesHead;
            while(attribute != null) {
                count++;
                attributesBuilder.append(linePrefix+"  ").append(attribute.name).append("\n");
                attribute.value.dumpToBuilder(attributesBuilder, linePrefix+"    ");
                attribute = attribute.nextAttribute;
            }
            builder.append("> with "+count+" attributes:\n").
                    append(attributesBuilder);
        }
    }
}
